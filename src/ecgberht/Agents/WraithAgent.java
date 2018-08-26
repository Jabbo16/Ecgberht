package ecgberht.Agents;

import ecgberht.Simulation.SimInfo;
import ecgberht.Util.Util;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.WeaponType;
import org.openbw.bwapi4j.unit.*;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static ecgberht.Ecgberht.getGs;

public class WraithAgent extends Agent implements Comparable<Unit> {

    public Wraith unit;
    public String name;
    private Set<Unit> airAttackers = new TreeSet<>();

    public WraithAgent(Unit unit, String name) {
        super();
        this.unit = (Wraith) unit;
        this.name = name;
        this.myUnit = unit;
    }

    @Override
    public boolean runAgent() {
        try {
            if (!unit.exists()) return true;
            if (unit.getHitPoints() <= 15) {
                Position cc = getGs().MainCC.second.getPosition();
                if (cc != null) unit.move(cc);
                else unit.move(getGs().getPlayer().getStartLocation().toPosition());
                getGs().myArmy.add(unit);
                return true;
            }
            actualFrame = getGs().frameCount;
            frameLastOrder = unit.getLastCommandFrame();
            closeEnemies.clear();
            mainTargets.clear();
            airAttackers.clear();
            if (frameLastOrder == actualFrame) return false;
            //Status old = status;
            getNewStatus();
            //if (old == status && status != Status.COMBAT && status != Status.ATTACK) return false;
            //if (status != Status.COMBAT) attackUnit = null;
            attackUnit = null;
            if ((status == Status.ATTACK || status == Status.IDLE) && (unit.isIdle() || unit.getOrder() == Order.PlayerGuard) && !unit.isAttacking()) {
                Position pos = Util.chooseAttackPosition(unit.getPosition(), true);
                Position target = unit.getOrderTargetPosition();
                if (pos != null && getGs().getGame().getBWMap().isValidPosition(pos) && (target == null || !target.equals(pos))) {
                    unit.attack(pos);
                    return false;
                }
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
            }
            return false;
        } catch (Exception e) {
            System.err.println("Exception WraithAgent");
            e.printStackTrace();
        }
        return false;
    }

    private void kite() {
        Position kite = getGs().kiteAway(unit, airAttackers);
        Position improvedKite = Util.isPositionMapEdge(kite) ? Util.improveMapEdgePosition(kite) : null;
        Position target = unit.getOrderTargetPosition();
        if (improvedKite != null && getGs().getGame().getBWMap().isValidPosition(improvedKite)) {
            if (target != null && !target.equals(improvedKite)) unit.move(improvedKite);
            if (target == null) unit.move(improvedKite);
            return;
        }
        if (!getGs().getGame().getBWMap().isValidPosition(kite)) return;
        if (kite.equals(unit.getPosition())) {
            retreat();
            return;
        }
        if (target != null && !target.equals(kite)) unit.move(kite);
        if (target == null) unit.move(kite);

    }

    @Override
    Unit getUnitToAttack(Unit myUnit, Set<Unit> enemies) {
        Unit chosen = null;
        double distB = Double.MAX_VALUE;
        for (Unit u : enemies) {
            if (!u.exists() || (((PlayerUnit) u).isCloaked() && !((PlayerUnit) u).isDetected())) continue;
            double distA = Util.broodWarDistance(myUnit.getPosition(), u.getPosition());
            if (chosen == null || distA < distB) {
                chosen = u;
                distB = distA;
            }
        }
        if (chosen != null) return chosen;
        return null;
    }

    private void combat() {
        Unit toAttack;
        if (!mainTargets.isEmpty()) {
            toAttack = chooseHarassTarget();
            if (toAttack != null) {
                if (attackUnit != null && attackUnit.equals(toAttack)) return;
                unit.attack(toAttack);
                attackUnit = toAttack;
                attackPos = null;
            }
        } else if (!airAttackers.isEmpty()) {
            toAttack = getUnitToAttack(unit, airAttackers);
            if (toAttack != null && (attackUnit == null || !attackUnit.equals(toAttack))) {
                unit.attack(toAttack);
                attackUnit = toAttack;
                attackPos = null;
            }
        } else if (!closeEnemies.isEmpty()) {
            toAttack = getUnitToAttack(unit, closeEnemies);
            if (toAttack != null) {
                if (attackUnit != null && attackUnit.equals(toAttack)) return;
                unit.attack(toAttack);
                attackUnit = toAttack;
                attackPos = null;
            }
        }
    }

    private void getNewStatus() {
        SimInfo mySimAir = getGs().sim.getSimulation(unit, SimInfo.SimType.AIR);
        SimInfo mySimMix = getGs().sim.getSimulation(unit, SimInfo.SimType.MIX);
        boolean chasenByScourge = false;
        boolean staticAirDefense = false;
        if (mySimMix.enemies.isEmpty()) {
            status = Status.ATTACK;
            return;
        }
        if (getGs().enemyRace == Race.Zerg && !mySimAir.enemies.isEmpty()) {
            for (Unit u : mySimAir.enemies) {
                if (u instanceof Scourge && ((Scourge) u).getOrderTarget().equals(unit)) chasenByScourge = true;
                else if (u instanceof SporeColony && u.getDistance(unit) < ((SporeColony) u).getAirWeapon().maxRange() * 1.1) {
                    staticAirDefense = true;
                }
                if (chasenByScourge && staticAirDefense) break;
            }
        }
        if (getGs().enemyRace == Race.Protoss && !mySimAir.enemies.isEmpty()) {
            for (Unit u : mySimAir.enemies) {
                if (u instanceof SporeColony && u.getDistance(unit) < ((SporeColony) u).getAirWeapon().maxRange() * 1.1) {
                    staticAirDefense = true;
                    break;
                }
            }
        }
        for (Unit u : mySimMix.enemies) {
            if (u instanceof Worker || u instanceof Overlord) mainTargets.add(u);
        }
        airAttackers = mySimAir.enemies;
        closeEnemies = mySimMix.enemies;
        if (closeEnemies.isEmpty()) status = Status.ATTACK;
        else if (!airAttackers.isEmpty()) {
            Unit closestAirAttacker = Util.getClosestUnit(unit, airAttackers);
            if (chasenByScourge || staticAirDefense) status = Status.KITE;
            else if (closestAirAttacker != null) {
                double dist = closestAirAttacker.getDistance(unit);
                Weapon weapon = closestAirAttacker.getType().isFlyer() ? unit.getAirWeapon() : unit.getGroundWeapon();
                double enemyRange = ((AirAttacker) closestAirAttacker).getAirWeaponMaxRange(); // TODO helper method that includes upgrades
                if (weapon.cooldown() == 0 && enemyRange < weapon.maxRange() && dist > enemyRange * 1.15)
                    status = Status.COMBAT;
                else if (dist < enemyRange * 1.5) status = Status.KITE;
            } else if (mySimAir.lose) status = Status.KITE;
            else status = Status.ATTACK;
        } else if (!mainTargets.isEmpty()) status = Status.COMBAT;
        else status = Status.ATTACK;
    }

    private void attack() {
        if (unit.isAttacking()) return;
        Position newAttackPos;
        if (getGs().enemyMainBase != null) newAttackPos = getGs().enemyMainBase.getLocation().toPosition();
        else newAttackPos = Util.chooseAttackPosition(unit.getPosition(), true);
        if (attackPos != null && attackPos.equals(newAttackPos)) return;
        attackPos = newAttackPos;
        if (attackPos != null && getGs().bw.getBWMap().isValidPosition(attackPos)) {
            Position target = unit.getOrderTargetPosition();
            if (target == null || (getGs().getGame().getBWMap().isValidPosition(target) && !target.equals(attackPos))) {
                if (Util.broodWarDistance(attackPos, unit.getPosition()) <= unit.getGroundWeaponMaxRange())
                    unit.attack(attackPos);
                else unit.move(attackPos);
            }
        }
        attackUnit = null;
    }

    private Unit chooseHarassTarget() {
        Unit chosen = null;
        double maxScore = Double.MIN_VALUE;
        for (Unit u : mainTargets) {
            if (!u.exists()) continue;
            double dist = myUnit.getDistance(u);
            double score = u instanceof Worker ? 2 : (u instanceof Overlord ? 3 : 1);
            WeaponType weapon = Util.getWeapon(unit, u);
            score *= dist <= weapon.maxRange() ? 1.2 : 0.8;
            score *= (double) unit.getType().maxHitPoints() / (double) unit.getHitPoints();
            if (chosen == null || maxScore < score) {
                chosen = u;
                maxScore = score;
            }
        }
        if (chosen != null) return chosen;
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this.unit) return true;
        if (!(o instanceof WraithAgent)) return false;
        WraithAgent wraith = (WraithAgent) o;
        return unit.equals(wraith.unit);
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
