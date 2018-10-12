package ecgberht.Agents;

import ecgberht.EnemyBuilding;
import ecgberht.Util.Util;
import ecgberht.Util.UtilMicro;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.type.WeaponType;
import org.openbw.bwapi4j.unit.*;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static ecgberht.Ecgberht.getGs;

public class WraithAgent extends Agent implements Comparable<Unit> {

    public Wraith unit;
    public String name;
    private Set<Unit> airAttackers = new TreeSet<>();
    private Set<Unit> enemyStaticDefense = new TreeSet<>();

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
            mainTargets.clear();
            airAttackers.clear();
            enemyStaticDefense.clear();
            if (frameLastOrder == actualFrame) return false;
            Position attack = getBestBaseToHarass();
            AirAttacker closestThreat = null;
            double bestDist = Double.MAX_VALUE;
            for (Unit u : getGs().enemyCombatUnitMemory) {
                if (unit.getDistance(u) < UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange()) {
                    if (u instanceof AirAttacker) {
                        double hisAirWeaponRange = Util.getEnemyAirWeaponRange((AirAttacker) u);
                        double dist = unit.getDistance(u);
                        if (dist < bestDist) {
                            closestThreat = (AirAttacker) u;
                            bestDist = dist;
                        }
                        if (dist <= hisAirWeaponRange) airAttackers.add(u);
                    }
                    mainTargets.add(u);
                }
            }
            for (EnemyBuilding u : getGs().enemyBuildingMemory.values()) {
                if (u.unit.isVisible()) {
                    if (!(u.unit instanceof AirAttacker) && !(u.unit instanceof Bunker)) continue;
                    if (u.unit.getDistance(unit) < UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange()) {
                        airAttackers.add(u.unit);
                    }
                }
            }
            Unit harassed = chooseHarassTarget();
            if (airAttackers.isEmpty()) {
                if (closestThreat != null) {
                    Weapon myWeapon = closestThreat.isFlying() ? unit.getAirWeapon() : unit.getGroundWeapon();
                    double hisAirWeaponRange = Util.getEnemyAirWeaponRange(closestThreat);
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

            Unit target = Util.getTarget(unit, closeEnemies);
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


    private Unit chooseHarassTarget() {
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
