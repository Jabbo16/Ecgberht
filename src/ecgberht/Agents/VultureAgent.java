package ecgberht.Agents;

import bwem.Base;
import ecgberht.Simulation.SimInfo;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import ecgberht.Util.UtilMicro;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.type.WeaponType;
import org.openbw.bwapi4j.unit.*;

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
        super();
        this.unit = (Vulture) unit;
        this.myUnit = unit;
    }

    public void placeMine(Position pos) {
        if (mines > 0) unit.spiderMine(pos);
    }

    @Override
    public boolean runAgent() {
        try {
            if (unit.getHitPoints() <= 20) {
                MutablePair<Base, Unit> cc = getGs().mainCC;
                if (cc != null && cc.second != null) {
                    Position ccPos = cc.second.getPosition();
                    if (getGs().getGame().getBWMap().isValidPosition(ccPos)) {
                        unit.move(ccPos);
                        getGs().myArmy.add(unit);
                        return true;
                    }
                }
                unit.move(getGs().getPlayer().getStartLocation().toPosition());
                getGs().myArmy.add(unit);
                return true;
            }
            mySim = getGs().sim.getSimulation(unit, SimInfo.SimType.GROUND);
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
        Unit toAttack = Util.getRangedTarget(unit, mySim.enemies);
        if (toAttack != null) {
            if (attackUnit != null && attackUnit.equals(toAttack)) return;
            UtilMicro.attack(unit, toAttack);
            attackUnit = toAttack;
        }
    }

    private Position selectNewAttack() {
        Position p = Util.chooseAttackPosition(myUnit.getPosition(), false);
        if (p != null && getGs().getGame().getBWMap().isValidPosition(p)) return p;
        if (getGs().enemyMainBase != null) return getGs().enemyMainBase.getLocation().toPosition();
        return null;
    }

    private Unit getUnitToAttack(Unit myUnit, Set<Unit> enemies) {
        Unit chosen = null;
        double distB = Double.MAX_VALUE;
        for (Unit u : enemies) {
            if (u.getType().isFlyer() || ((PlayerUnit) u).isCloaked()) continue;
            double distA = Util.broodWarDistance(myUnit.getPosition(), u.getPosition());
            if (chosen == null || distA < distB) {
                chosen = u;
                distB = distA;
            }
        }
        if (chosen != null) return chosen;
        return null;
    }

    private void retreat() {
        Position CC = getGs().getNearestCC(myUnit.getPosition());
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
            if (!meleeOnly && getGs().sim.getSimulation(unit, SimInfo.SimType.GROUND).lose) {
                status = Status.RETREAT;
                return;
            }
            if (status == Status.PATROL && actualFrame - lastPatrolFrame > 5) {
                status = Status.COMBAT;
                return;
            }
            int cd = unit.getGroundWeapon().cooldown();
            Unit closestAttacker = Util.getClosestUnit(unit, mySim.enemies);
            if (closestAttacker != null && (cd != 0 || closestAttacker.getDistance(unit) < unit.getGroundWeaponMaxRange() * 0.6)) {
                status = Status.KITE;
                return;
            }
            if (status == Status.COMBAT || status == Status.ATTACK) {
                if (attackUnit != null) {
                    int weaponRange = attackUnit instanceof GroundAttacker ? ((GroundAttacker) attackUnit).getGroundWeaponMaxRange() : 0;
                    if (weaponRange > type.groundWeapon().maxRange()) return;
                }
                if (cd > 0) {
                    attackUnit = null;
                    attackPos = null;
                    status = Status.KITE;
                    return;
                }
            }
            if (status == Status.KITE) {
                Unit closest = getUnitToAttack(unit, mySim.enemies);
                if (closest != null) {
                    double dist = unit.getDistance(closest);
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
        for (Unit e : mySim.enemies) {
            int weaponRange = e instanceof GroundAttacker ? ((GroundAttacker) e).getGroundWeaponMaxRange() : 0;
            WeaponType weapon = Util.getWeapon(e, unit);
            if ((weaponRange > 32 || e instanceof Bunker) && e.getDistance(unit) < ((PlayerUnit) e).getPlayer().getUnitStatCalculator().weaponMaxRange(weapon))
                return false;
        }
        return true;
    }

    private void kite() {
        //Position kite = UtilMicro.kiteAway(unit, closeEnemies);
        Optional<Unit> closestUnit = mySim.enemies.stream().min(Unit::getDistance);
        Position kite = closestUnit.map(unit1 -> UtilMicro.kiteAwayAlt(unit.getPosition(), unit1.getPosition())).orElse(null);
        if (kite == null || !getGs().getGame().getBWMap().isValidPosition(kite)) {
            retreat();
            return;
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
        return Objects.hash(unit);
    }

    @Override
    public int compareTo(Unit v1) {
        return this.unit.getId() - v1.getId();
    }
}
