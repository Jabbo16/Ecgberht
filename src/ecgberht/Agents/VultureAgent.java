package ecgberht.Agents;

import bwem.Base;
import ecgberht.Simulation.SimInfo;
import ecgberht.UnitInfo;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import ecgberht.Util.UtilMicro;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Bunker;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.unit.Vulture;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static ecgberht.Ecgberht.getGs;

public class VultureAgent extends Agent implements Comparable<Unit> {

    public Vulture unit;
    private int mines = 3;
    private UnitType type = UnitType.Terran_Vulture;
    private int lastPatrolFrame = 0;
    private SimInfo mySim;

    public VultureAgent(Unit unit) {
        super(unit);
        this.unit = (Vulture) unit;
    }

    public void placeMine(Position pos) {
        if (mines > 0) unit.spiderMine(pos);
    }

    @Override
    public boolean runAgent() {
        try {
            if (!unit.exists() || unitInfo == null) return true;
            if (unit.getHitPoints() <= 20) {
                MutablePair<Base, Unit> cc = getGs().mainCC;
                if (cc != null && cc.second != null) {
                    Position ccPos = cc.second.getPosition();
                    if (getGs().getGame().getBWMap().isValidPosition(ccPos)) {
                        unit.move(ccPos);
                        getGs().myArmy.add(unitInfo);
                        return true;
                    }
                }
                unit.move(getGs().getPlayer().getStartLocation().toPosition());
                getGs().myArmy.add(unitInfo);
                return true;
            }
            mySim = getGs().sim.getSimulation(unitInfo, SimInfo.SimType.GROUND);
            actualFrame = getGs().frameCount;
            frameLastOrder = unit.getLastCommandFrame();
            if (frameLastOrder == actualFrame) return false;
            //Status old = status;
            getNewStatus();
            //if (old == status && status != Status.COMBAT && status != Status.ATTACK) return false;
            if (status != Status.COMBAT && status != Status.PATROL) attackUnit = null;
            if ((status == Status.ATTACK || status == Status.IDLE) && (unit.isIdle() || unit.getOrder() == Order.PlayerGuard)) {
                Position pos = Util.chooseAttackPosition(unit.getPosition(), false);
                if (pos == null || !getGs().getGame().getBWMap().isValidPosition(pos)) return false;
                UtilMicro.move(unit, pos);
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
        if (unit.getOrder() == Order.Patrol) return;
        if (attackUnit != null && unit.getOrder() != Order.Patrol) {
            Position pos = Util.choosePatrolPositionVulture(unit, attackUnit);
            if (pos != null && getGs().getGame().getBWMap().isValidPosition(pos)) {
                unit.patrol(pos);
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
        if (p != null && getGs().getGame().getBWMap().isValidPosition(p)) return p;
        if (getGs().enemyMainBase != null) return getGs().enemyMainBase.getLocation().toPosition();
        return null;
    }

    private UnitInfo getUnitToAttack(Set<UnitInfo> enemies) {
        UnitInfo chosen = null;
        double distB = Double.MAX_VALUE;
        for (UnitInfo u : enemies) {
            if (u.flying || u.unit.isCloaked()) continue;
            double distA = unitInfo.toUnitInfoDistance().getDistance(u);
            if (chosen == null || distA < distB) {
                chosen = u;
                distB = distA;
            }
        }
        return chosen;
    }

    private void retreat() {
        Position CC = getGs().getNearestCC(myUnit.getPosition(), true);
        if (CC != null) UtilMicro.move(unit, CC);
        else UtilMicro.move(unit, getGs().getPlayer().getStartLocation().toPosition());
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
            int cd = unit.getGroundWeapon().cooldown();
            UnitInfo closestAttacker = Util.getClosestUnit(unitInfo, mySim.enemies, true);
            if (closestAttacker != null && (cd != 0 || unitInfo.toUnitInfoDistance().getDistance(closestAttacker) < unitInfo.groundRange * 0.6)) {
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
                        if (predictedPosition != null && getGs().getGame().getBWMap().isValidPosition(predictedPosition)) {
                            double distPredicted = unit.getDistance(predictedPosition);
                            double distCurrent = unitInfo.toUnitInfoDistance().getDistance(attackUnit);
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
                    double dist = unitInfo.toUnitInfoDistance().getDistance(closest);
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
            if ((weaponRange > 32 || e.unit instanceof Bunker) && e.unit.getDistance(unit) < weaponRange)
                return false;
        }
        return true;
    }

    private void kite() {
        //Position kite = UtilMicro.kiteAway(unit, closeEnemies);
        Optional<UnitInfo> closestUnit = mySim.enemies.stream().min(Comparator.comparing(u -> unitInfo.toUnitInfoDistance().getDistance(u)));
        Position kite = closestUnit.map(unit1 -> UtilMicro.kiteAwayAlt(unit.getPosition(), unit1.position)).orElse(null);
        if (kite == null || !getGs().getGame().getBWMap().isValidPosition(kite)) {
            kite = UtilMicro.kiteAway(unit, mySim.enemies);
            if (kite == null || !getGs().getGame().getBWMap().isValidPosition(kite)) {
                retreat();
                return;
            }
        }
        UtilMicro.move(unit, kite);
    }

    private void attack() {
        if (unit.isAttackFrame()) return;
        attackPos = selectNewAttack();
        if (attackPos == null || !getGs().bw.getBWMap().isValidPosition(attackPos)) {
            attackUnit = null;
            attackPos = null;
            return;
        }
        UtilMicro.attack(unit, attackPos);
        attackUnit = null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof VultureAgent)) return false;
        VultureAgent vulture = (VultureAgent) o;
        return unit.equals(vulture.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit.getId());
    }

    @Override
    public int compareTo(Unit v1) {
        return this.unit.getId() - v1.getId();
    }
}
