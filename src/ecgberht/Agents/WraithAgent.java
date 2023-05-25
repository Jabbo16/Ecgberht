package ecgberht.Agents;

import bwem.util.Pair;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(WraithAgent.class);
    public Wraith unit;
    public String name;
    private Set<UnitInfo> airAttackers = new TreeSet<>();

    public WraithAgent(Unit unit, String name) {
        super(unit);
        this.unit = (Wraith) unit;
        this.name = name;
    }

    @Override
    public boolean runAgent() {
        try {
            if (!unit.exists() || unitInfo == null) return true;
            actualFrame = getGs().frameCount;
            frameLastOrder = unit.getLastCommandFrame();
            if (frameLastOrder == actualFrame) return false;
            airAttackers.clear();

            Position attack = getBestBaseToHarass();
            Pair<UnitInfo, Double> resultFindClosestTreatAndBestDist = findClosestTreatAndBestDist();
            UnitInfo closestThreat = resultFindClosestTreatAndBestDist.getFirst();
            double bestDist = resultFindClosestTreatAndBestDist.getSecond();

            Set<UnitInfo> mainTargets = getGs().sim.getSimulation(unitInfo, SimInfo.SimType.MIX).enemies;
            UnitInfo harassTarget = chooseHarassTarget(mainTargets);

            if (closestThreat != null) {
                wraithAttackClosestThreat(closestThreat, harassTarget, bestDist);
            }
            if (harassTarget != null) {
                UtilMicro.attack(unitInfo, harassTarget);
                return false;
            }
            UnitInfo target = Util.getRangedTarget(unitInfo, mainTargets);
            if (target != null) {
                UtilMicro.attack(unitInfo, target);
                return false;
            }
            if (attack != null) {
                if (attack.getDistance(myUnit.getPosition()) >= 32 * 5) UtilMicro.move(unit, attack);
                else UtilMicro.attack(unit, attack);
                return false;
            }
            return false;
        } catch (Exception e) {
            logger.error("Exception occurred in WraithAgent", e);
        }
        return false;
    }

    private Pair<UnitInfo, Double> findClosestTreatAndBestDist(){
        UnitInfo closestThreat = null;
        double bestDist = Double.MAX_VALUE;
        SimInfo airSim = getGs().sim.getSimulation(unitInfo, SimInfo.SimType.AIR);
        airAttackers = airSim.enemies;
        for (UnitInfo u : airAttackers) {
            double predictedDist = unitInfo.toUnitInfoDistance().getPredictedDistance(u);
            if (predictedDist < bestDist) {
                closestThreat = u;
                bestDist = predictedDist;
            }
        }
        return new Pair<>(closestThreat, bestDist);
    }

    private void wraithAttackClosestThreat(UnitInfo closestThreat, UnitInfo harassTarget, double bestDist){
        Weapon myWeapon = closestThreat.flying ? unit.getAirWeapon() : unit.getGroundWeapon();
        double hisAirWeaponRange = closestThreat.airRange;
        Position kitePos = UtilMicro.kiteAway(unit, new TreeSet<>(Collections.singleton(closestThreat)));
        if (myWeapon.maxRange() > hisAirWeaponRange && bestDist > hisAirWeaponRange) {
            if (myWeapon.cooldown() != 0) {
                if (kitePos != null) {
                    UtilMicro.move(unit, kitePos);
                }
            } else if (harassTarget != null && unitInfo.toUnitInfoDistance().getDistance(harassTarget) <= myWeapon.maxRange()) {
                UtilMicro.attack(unitInfo, harassTarget);
            } else UtilMicro.attack(unitInfo, closestThreat);
            return;
        }
        if (kitePos != null) {
            UtilMicro.move(unit, kitePos);
        }
    }

    private Position getBestBaseToHarass() {
        return Util.chooseAttackPosition(unit.getPosition(), true);
    }

    private double calculateHarassTargetScore(UnitInfo u){
        double unitDistance = unitInfo.toUnitInfoDistance().getDistance(u);
        double harassTargetScore;
        if (u.unitType.isWorker()) {
            harassTargetScore = 5;
        } else if (u.unitType == UnitType.Zerg_Overlord) {
            harassTargetScore = 8;
        } else {
            harassTargetScore = 1;
        }

        WeaponType weapon = Util.getWeapon(unitInfo, u);
        harassTargetScore *= unitDistance <= weapon.maxRange() ? 1.4 : 0.9;
        harassTargetScore *= (double) u.unitType.maxHitPoints() / (double) u.health;

        return harassTargetScore;
    }

    private UnitInfo chooseHarassTarget(Set<UnitInfo> mainTargets) {
        UnitInfo harassTargerChosen = null;
        double maxScore = Double.MIN_VALUE;

        for (UnitInfo u : mainTargets) {
            double harassTargetScore = calculateHarassTargetScore(u);
            if (harassTargerChosen == null || maxScore < harassTargetScore) {
                harassTargerChosen = u;
                maxScore = harassTargetScore;
            }
        }
        return harassTargerChosen;
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
        return Objects.hash(unit.getId());
    }

    @Override
    public int compareTo(Unit v1) {
        return this.unit.getId() - v1.getId();
    }

}
