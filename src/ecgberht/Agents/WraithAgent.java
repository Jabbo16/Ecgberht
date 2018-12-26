package ecgberht.Agents;

import ecgberht.Simulation.SimInfo;
import ecgberht.UnitStorage;
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
    private Set<UnitStorage.UnitInfo> airAttackers = new TreeSet<>();

    public WraithAgent(Unit unit, String name) {
        super();
        this.unitInfo = getGs().unitStorage.getAllyUnits().get(unit);
        this.unit = (Wraith) unit;
        this.name = name;
        this.myUnit = unit;
    }

    @Override
    public boolean runAgent() { // TODO improve
        try {
            if (!unit.exists() || unitInfo == null) return true;
            actualFrame = getGs().frameCount;
            frameLastOrder = unit.getLastCommandFrame();
            airAttackers.clear();
            if (frameLastOrder == actualFrame) return false;
            Position attack = getBestBaseToHarass();
            UnitStorage.UnitInfo closestThreat = null;
            double bestDist = Double.MAX_VALUE;
            airAttackers = getGs().sim.getSimulation(unitInfo, SimInfo.SimType.AIR).enemies;
            Set<UnitStorage.UnitInfo> closeEnemies = getGs().sim.getSimulation(unitInfo, SimInfo.SimType.MIX).enemies;
            Iterator<UnitStorage.UnitInfo> it = airAttackers.iterator();
            while (it.hasNext()) {
                UnitStorage.UnitInfo u = it.next();
                double dist = unit.getDistance(u.unit);
                double hisAirWeaponRange = u.airRange;
                if (dist < bestDist) {
                    closestThreat = u;
                    bestDist = dist;
                }
                if (dist > hisAirWeaponRange) it.remove();
            }
            Set<UnitStorage.UnitInfo> mainTargets = getGs().sim.getSimulation(unitInfo, SimInfo.SimType.MIX).enemies;
            Unit harassed = chooseHarassTarget(mainTargets);
            if (airAttackers.isEmpty()) { // TODO improve this
                if (closestThreat != null) {
                    Weapon myWeapon = closestThreat.flying ? unit.getAirWeapon() : unit.getGroundWeapon();
                    double hisAirWeaponRange = closestThreat.airRange;
                    if (myWeapon.maxRange() > hisAirWeaponRange && bestDist >= hisAirWeaponRange * 1.1) {
                        if (myWeapon.cooldown() > 0) {
                            UtilMicro.attack(unit, closestThreat.unit);
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
            UnitStorage.UnitInfo target = Util.getRangedTarget(unitInfo, closeEnemies);
            if (target != null) {
                UtilMicro.attack(unit, target.unit);
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

    private Unit chooseHarassTarget(Set<UnitStorage.UnitInfo> mainTargets) {
        Unit chosen = null;
        double maxScore = Double.MIN_VALUE;
        for (UnitStorage.UnitInfo u : mainTargets) {
            if (!u.unit.exists()) continue;
            double dist = myUnit.getDistance(u.unit);
            double score = u.unit instanceof Worker ? 3 : (u.unit instanceof Overlord ? 6 : 1);
            WeaponType weapon = Util.getWeapon(unit, u.unit);
            score *= dist <= weapon.maxRange() ? 1.4 : 0.9;
            score *= (double) u.unitType.maxHitPoints() / (double) u.health;
            if (chosen == null || maxScore < score) {
                chosen = u.unit;
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
