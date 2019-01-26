package ecgberht.Agents;

import ecgberht.Simulation.SimInfo;
import ecgberht.UnitInfo;
import ecgberht.Util.Util;
import ecgberht.Util.UtilMicro;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.type.WeaponType;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.unit.Weapon;
import org.openbw.bwapi4j.unit.Wraith;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static ecgberht.Ecgberht.getGs;

public class WraithAgent extends Agent implements Comparable<Unit> {

    public Wraith unit;
    public String name;
    private Set<UnitInfo> airAttackers = new TreeSet<>();

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
            UnitInfo closestThreat = null;
            double bestDist = Double.MAX_VALUE;
            SimInfo airSim = getGs().sim.getSimulation(unitInfo, SimInfo.SimType.AIR);
            airAttackers = airSim.enemies;
            for (UnitInfo u : airAttackers) {
                double dist = unitInfo.getDistance(u);
                double predictedDist = unitInfo.getPredictedDistance(u);
                double hisAirWeaponRange = u.airRange;
                if (dist > hisAirWeaponRange) continue;
                if (predictedDist < bestDist) {
                    closestThreat = u;
                    bestDist = predictedDist;
                }
            }
            Set<UnitInfo> mainTargets = getGs().sim.getSimulation(unitInfo, SimInfo.SimType.MIX).enemies;
            UnitInfo harassed = chooseHarassTarget(mainTargets);
            if (airAttackers.isEmpty()) { // Logic inside is borked
                if (closestThreat != null) {
                    Weapon myWeapon = closestThreat.flying ? unit.getAirWeapon() : unit.getGroundWeapon();
                    double hisAirWeaponRange = closestThreat.airRange;
                    if (myWeapon.maxRange() > hisAirWeaponRange && bestDist >= hisAirWeaponRange) {
                        if (myWeapon.cooldown() > 0) {
                            UtilMicro.attack(unit, closestThreat);
                        } else UtilMicro.move(unit, closestThreat.lastPosition);
                        return false;
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
            UnitInfo target = Util.getRangedTarget(unitInfo, mainTargets);
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

    private UnitInfo chooseHarassTarget(Set<UnitInfo> mainTargets) {
        UnitInfo chosen = null;
        double maxScore = Double.MIN_VALUE;
        for (UnitInfo u : mainTargets) {
            //if (!u.unit.exists()) continue;
            double dist = unitInfo.getDistance(u);
            double score = u.unitType.isWorker() ? 5 : (u.unitType == UnitType.Zerg_Overlord ? 8 : 1);
            WeaponType weapon = Util.getWeapon(unitInfo, u);
            score *= dist <= weapon.maxRange() ? 1.4 : 0.9;
            score *= (double) u.unitType.maxHitPoints() / (double) u.health;
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
