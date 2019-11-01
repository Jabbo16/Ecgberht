package ecgberht.Agents;

import bwapi.*;
import bwem.Base;
import ecgberht.Simulation.SimInfo;
import ecgberht.UnitInfo;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import ecgberht.Util.UtilMicro;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static ecgberht.Ecgberht.getGs;

public class VultureAgent extends Agent implements Comparable<Unit> {

    private int mines = 3;
    private UnitType type = UnitType.Terran_Vulture;
    private int lastPatrolFrame = 0;
    private SimInfo mySim;

    public VultureAgent(Unit unit) {
        super();
        this.unitInfo = getGs().unitStorage.getAllyUnits().get(unit);
        this.myUnit = unit;
    }

    public void placeMine(Position pos) {
        if (mines > 0) myUnit.useTech(TechType.Spider_Mines, pos);
    }

    @Override
    public boolean runAgent() {
        try {
            if (!myUnit.exists() || unitInfo == null) return true;
            if (myUnit.getHitPoints() <= 20) {
                MutablePair<Base, Unit> cc = getGs().mainCC;
                if (cc != null && cc.second != null) {
                    Position ccPos = cc.second.getPosition();
                    if (ccPos.isValid(getGs().bw)) {
                        myUnit.move(ccPos);
                        getGs().myArmy.add(unitInfo);
                        return true;
                    }
                }
                myUnit.move(getGs().getPlayer().getStartLocation().toPosition());
                getGs().myArmy.add(unitInfo);
                return true;
            }
            mySim = getGs().sim.getSimulation(unitInfo, SimInfo.SimType.GROUND);
            actualFrame = getGs().frameCount;
            frameLastOrder = myUnit.getLastCommandFrame();
            if (frameLastOrder == actualFrame) return false;
            //Status old = status;
            getNewStatus();
            //if (old == status && status != Status.COMBAT && status != Status.ATTACK) return false;
            if (status != Status.COMBAT && status != Status.PATROL) attackUnit = null;
            if ((status == Status.ATTACK || status == Status.IDLE) && (myUnit.isIdle() || myUnit.getOrder() == Order.PlayerGuard)) {
                Position pos = Util.chooseAttackPosition(myUnit.getPosition(), false);
                if (pos == null || !pos.isValid(getGs().bw)) return false;
                UtilMicro.move(myUnit, pos);
                status = Status.ATTACK;
                return false;

            }
            switch (status) {
                case ATTACK:
                    attack();
                    break;
                case COMBAT:
                    combat();
                    break;
                case KITE:
                    kite();
                    break;
                case RETREAT:
                    retreat();
                    break;
                case PATROL:
                    patrol();
                    break;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Exception VultureAgent");
            e.printStackTrace();
        }
        return false;
    }

    private void patrol() {
        if (myUnit.getOrder() == Order.Patrol) return;
        if (attackUnit != null && myUnit.getOrder() != Order.Patrol) {
            Position pos = Util.choosePatrolPositionVulture(myUnit, attackUnit);
            if (pos != null && pos.isValid(getGs().bw)) {
                myUnit.patrol(pos);
                attackUnit = null;
                lastPatrolFrame = actualFrame;
                return;
            }
        }
        attackUnit = null;
    }

    private void combat() {
        UnitInfo toAttack = Util.getRangedTarget(unitInfo, mySim.enemies);
        if (toAttack != null) {
            if (attackUnit != null && attackUnit.equals(toAttack)) return;
            UtilMicro.attack(unitInfo, toAttack);
            attackUnit = toAttack;
        }
    }

    private Position selectNewAttack() {
        Position p = Util.chooseAttackPosition(myUnit.getPosition(), false);
        if (p != null && p.isValid(getGs().bw)) return p;
        if (getGs().enemyMainBase != null) return getGs().enemyMainBase.getLocation().toPosition();
        return null;
    }

    private UnitInfo getUnitToAttack(Set<UnitInfo> enemies) {
        UnitInfo chosen = null;
        double distB = Double.MAX_VALUE;
        for (UnitInfo u : enemies) {
            if (u.flying || u.unit.isCloaked()) continue;
            double distA = unitInfo.getDistance(u);
            if (chosen == null || distA < distB) {
                chosen = u;
                distB = distA;
            }
        }
        return chosen;
    }

    private void retreat() {
        Position CC = getGs().getNearestCC(myUnit.getPosition(), true);
        if (CC != null) UtilMicro.move(myUnit, CC);
        else UtilMicro.move(myUnit, getGs().getPlayer().getStartLocation().toPosition());
        attackPos = null;
        attackUnit = null;
    }

    private void getNewStatus() {
        if (mySim.enemies.isEmpty()) {
            status = Status.ATTACK;
        } else {
            boolean meleeOnly = checkOnlyMelees();
            if (!meleeOnly && getGs().sim.getSimulation(unitInfo, SimInfo.SimType.GROUND).lose) {
                status = Util.isInOurBases(unitInfo) ? Status.KITE : Status.RETREAT;
                return;
            }
            if (status == Status.PATROL && actualFrame - lastPatrolFrame > 5) {
                status = Status.COMBAT;
                return;
            }
            int cd = myUnit.getGroundWeaponCooldown();
            UnitInfo closestAttacker = Util.getClosestUnit(unitInfo, mySim.enemies, true);
            if (closestAttacker != null && (cd != 0 || unitInfo.getDistance(closestAttacker) < unitInfo.groundRange * 0.6)) {
                status = Status.KITE;
                return;
            }
            if (status == Status.COMBAT || status == Status.ATTACK) {
                /*if (attackUnit != null) {
                    int weaponRange = attackUnit instanceof GroundAttacker ? ((GroundAttacker) attackUnit).getGroundWeaponMaxRange() : 0;
                    if (weaponRange > type.groundWeapon().maxRange()) return;
                }*/
                if (cd > 0) {
                    if (attackUnit != null) {
                        Position predictedPosition = UtilMicro.predictUnitPosition(attackUnit, 2);
                        if (predictedPosition != null && predictedPosition.isValid(getGs().bw)) {
                            double distPredicted = myUnit.getDistance(predictedPosition);
                            double distCurrent = unitInfo.getDistance(attackUnit);
                            if (distPredicted > distCurrent) {
                                status = Status.COMBAT;
                                return;
                            } else {
                                attackUnit = null;
                                attackPos = null;
                                status = Status.KITE;
                                return;
                            }
                        }
                    }
                    if (mySim.enemies.isEmpty()) {
                        status = Status.ATTACK;
                        return;
                    }
                    status = Status.COMBAT;
                    return;
                }
            }
            if (status == Status.KITE) {
                UnitInfo closest = getUnitToAttack(mySim.enemies);
                if (closest != null) {
                    double dist = unitInfo.getDistance(closest);
                    double speed = type.topSpeed();
                    double timeToEnter = 0.0;
                    if (speed > .00001) timeToEnter = Math.max(0.0, dist - type.groundWeapon().maxRange()) / speed;
                    if (timeToEnter >= cd) {
                        //status = Status.COMBAT;
                        status = Status.PATROL;
                        attackUnit = closest;
                        return;
                    }
                } else {
                    status = Status.ATTACK;
                    return;
                }
                if (cd == 0) status = Status.COMBAT;
            }
        }
    }

    private boolean checkOnlyMelees() {
        for (UnitInfo e : mySim.enemies) {
            int weaponRange = (int) e.groundRange;
            if ((weaponRange > 32 || e.unitType == UnitType.Terran_Bunker) && e.unit.getDistance(myUnit) < weaponRange)
                return false;
        }
        return true;
    }

    private void kite() {
        //Position kite = UtilMicro.kiteAway(unit, closeEnemies);
        Optional<UnitInfo> closestUnit = mySim.enemies.stream().min(Comparator.comparing(u -> u.unit.getDistance(myUnit)));
        Position kite = closestUnit.map(unit1 -> UtilMicro.kiteAwayAlt(myUnit.getPosition(), unit1.position)).orElse(null);
        if (kite == null || !kite.isValid(getGs().bw)) {
            kite = UtilMicro.kiteAway(myUnit, mySim.enemies);
            if (kite == null || !kite.isValid(getGs().bw)) {
                retreat();
                return;
            }
        }
        UtilMicro.move(myUnit, kite);
    }

    private void attack() {
        if (myUnit.isAttackFrame()) return;
        attackPos = selectNewAttack();
        if (attackPos == null || !attackPos.isValid(getGs().bw)) {
            attackUnit = null;
            attackPos = null;
            return;
        }
        UtilMicro.attack(myUnit, attackPos);
        attackUnit = null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof VultureAgent)) return false;
        VultureAgent vulture = (VultureAgent) o;
        return myUnit.equals(vulture.myUnit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myUnit.getID());
    }

    @Override
    public int compareTo(Unit v1) {
        return this.myUnit.getID() - v1.getID();
    }
}
