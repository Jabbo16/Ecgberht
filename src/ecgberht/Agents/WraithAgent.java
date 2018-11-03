package ecgberht.Agents;

import ecgberht.Simulation.SimInfo;
import ecgberht.Util.Util;
import ecgberht.Util.UtilMicro;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.WeaponType;
import org.openbw.bwapi4j.unit.*;

import java.util.*;

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
    public boolean runAgent() { // TODO improve
        try {
            if (!unit.exists()) return true;
            actualFrame = getGs().frameCount;
            frameLastOrder = unit.getLastCommandFrame();
            airAttackers.clear();
            if (frameLastOrder == actualFrame) return false;
            Position attack = getBestBaseToHarass();
            AirAttacker closestThreat = null;
            double bestDist = Double.MAX_VALUE;
            airAttackers = getGs().sim.getSimulation(unit, SimInfo.SimType.AIR).enemies;
            Set<Unit> closeEnemies = getGs().sim.getSimulation(unit, SimInfo.SimType.MIX).enemies;
            Iterator<Unit> it = airAttackers.iterator();
            while (it.hasNext()) {
                Unit u = it.next();
                double dist = unit.getDistance(u);
                double hisAirWeaponRange = ((AirAttacker) u).getAirWeaponMaxRange();
                if (dist < bestDist) {
                    closestThreat = (AirAttacker) u;
                    bestDist = dist;
                }
                if (dist > hisAirWeaponRange) it.remove();
            }
            Set<Unit> mainTargets = getGs().sim.getSimulation(unit, SimInfo.SimType.MIX).enemies;
            Unit harassed = chooseHarassTarget(mainTargets);
            if (airAttackers.isEmpty()) {
                if (closestThreat != null) {
                    Weapon myWeapon = closestThreat.isFlying() ? unit.getAirWeapon() : unit.getGroundWeapon();
                    double hisAirWeaponRange = closestThreat.getAirWeaponMaxRange();
                    if (myWeapon.maxRange() > hisAirWeaponRange && bestDist >= hisAirWeaponRange * 1.1) {
                        if (myWeapon.cooldown() > 0) {
                            UtilMicro.attack(unit, closestThreat);
                            return false;
                        }
                    }
                    Position kitePos = UtilMicro.kiteAway(unit, new TreeSet<>(Collections.singleton(closestThreat)));
                    if (kitePos != null) {
                        UtilMicro.move(unit, kitePos);
                        return false;
                    }
                }
                if (harassed != null) {
                    UtilMicro.attack(unit, harassed);
                    return false;
                }
            }
            Position kitePos = UtilMicro.kiteAway(unit, airAttackers);
            if (kitePos != null) {
                UtilMicro.move(unit, kitePos);
                return false;
            }
            Unit target = Util.getRangedTarget(unit, closeEnemies);
            if (target != null) {
                UtilMicro.attack(unit, target);
                return false;
            }
            if (attack != null) {
                if (attack.getDistance(unit.getPosition()) >= 32 * 5) UtilMicro.move(unit, attack);
                else UtilMicro.attack(unit, attack);
                return false;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Exception WraithAgent");
            e.printStackTrace();
        }
        return false;
    }

    private Position getBestBaseToHarass() {
        return Util.chooseAttackPosition(unit.getPosition(), true);
    }

    private Unit chooseHarassTarget(Set<Unit> mainTargets) {
        Unit chosen = null;
        double maxScore = Double.MIN_VALUE;
        for (Unit u : mainTargets) {
            if (!u.exists()) continue;
            double dist = myUnit.getDistance(u);
            double score = u instanceof Worker ? 3 : (u instanceof Overlord ? 6 : 1);
            WeaponType weapon = Util.getWeapon(unit, u);
            score *= dist <= weapon.maxRange() ? 1.4 : 0.9;
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
