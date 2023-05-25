package ecgberht.Agents;

import ecgberht.Simulation.SimInfo;
import ecgberht.Squad;
import ecgberht.UnitInfo;
import ecgberht.UnitInfoDistance;
import ecgberht.Util.Util;
import ecgberht.Util.UtilMicro;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.*;
import org.openbw.bwapi4j.unit.*;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static ecgberht.Ecgberht.getGs;

public class VesselAgent extends Agent implements Comparable<Unit> {

    public ScienceVessel unit;
    public Squad follow = null;
    private Status status = Status.IDLE;
    private Set<UnitInfo> airAttackers = new TreeSet<>();
    private Position center;
    private Unit target;
    private Unit oldTarget;

    public VesselAgent(Unit unit) {
        super(unit);
        this.unit = (ScienceVessel) unit;

    }

    public String statusToString() {
        if (status == Status.IRRADIATE) return "Irradiate";
        if (status == Status.DMATRIX) return "DefenseMatrix";
        if (status == Status.KITE) return "Kite";
        if (status == Status.FOLLOW) return "Follow";
        if (status == Status.RETREAT) return "Retreat";
        if (status == Status.IDLE) return "Idle";
        if (status == Status.HOVER) return "Hover";
        if (status == Status.EMP) return "EMP";
        return "None";
    }

    @Override
    public boolean runAgent() {
        try {
            if (!unit.exists() || unitInfo == null) return true;
            actualFrame = getGs().frameCount;
            frameLastOrder = unit.getLastCommandFrame();
            airAttackers.clear();
            if (frameLastOrder == actualFrame) return false;
            follow = chooseVesselSquad();
            if (follow == null) {
                status = Status.RETREAT;
                retreat();
                return false;
            }
            switch (status) {
                case DMATRIX:
                    if (unitInfo.energy <= TechType.Defensive_Matrix.energyCost()) {
                        getGs().wizard.irradiatedUnits.remove(unit);
                        status = Status.IDLE;
                        target = null;
                        oldTarget = null;
                    }
                    break;
                case IRRADIATE:
                    if (unitInfo.energy <= TechType.Irradiate.energyCost()) {
                        getGs().wizard.irradiatedUnits.remove(unit);
                        status = Status.IDLE;
                        target = null;
                        oldTarget = null;
                    }
                    break;
                case EMP:
                    if (unitInfo.energy <= TechType.EMP_Shockwave.energyCost()) {
                        getGs().wizard.EMPedUnits.remove(unit);
                        status = Status.IDLE;
                        target = null;
                        oldTarget = null;
                    }
                    break;
            }
            center = follow.getSquadCenter();
            getNewStatus();
            switch (status) {
                case IRRADIATE:
                    irradiate();
                    break;
                case DMATRIX:
                    dMatrix();
                    break;
                case KITE:
                    kite();
                    break;
                case FOLLOW:
                    followSquad();
                    break;
                case RETREAT:
                    retreat();
                    break;
                case HOVER:
                    hover();
                    break;
                case EMP:
                    emp();
                    break;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Exception VesselAgent");
            e.printStackTrace();
        }
        return false;
    }

    private void hover() {
        Position attack = follow.attack;
        if (attack == null || !getGs().getGame().getBWMap().isValidPosition(attack)) return;
        UtilMicro.move(unit, attack);
    }

    private Squad chooseVesselSquad() {
        Squad chosen = null;
        double scoreMax = Double.MIN_VALUE;
        for (Squad s : getGs().sqManager.squads.values()) {
            double dist = unitInfo.toUnitInfoDistance().getDistance(s.getSquadCenter());
            double score = -Math.pow(s.members.size(), 3) / dist;
            if (chosen == null || score > scoreMax) {
                chosen = s;
                scoreMax = dist;
            }
        }
        return chosen;
    }

    private void emp() {
        if (unitInfo.currentOrder == Order.CastEMPShockwave) {
            if (target != null && oldTarget != null && !target.equals(oldTarget)) {
                UtilMicro.emp(unit, target.getPosition());
                getGs().wizard.addEMPed(unit, (PlayerUnit) target);
                oldTarget = target;
            }
        } else if (target != null && target.exists() && unitInfo.currentOrder != Order.CastEMPShockwave) {
            UtilMicro.emp(unit, target.getPosition());
            getGs().wizard.addEMPed(unit, (PlayerUnit) target);
            oldTarget = target;
        }
        if (oldTarget != null && (!oldTarget.exists() || ((PlayerUnit) oldTarget).getShields() <= 1)) oldTarget = null;
        if (target != null && (!target.exists() || ((PlayerUnit) target).getShields() <= 1)) target = null;
    }

    private void irradiate() {
        if (unitInfo.currentOrder == Order.CastIrradiate) {
            if (target != null && oldTarget != null && !target.equals(oldTarget)) {
                UtilMicro.irradiate(unit, (PlayerUnit) target);
                getGs().wizard.addIrradiated(unit, (PlayerUnit) target);
                oldTarget = target;
            }
        } else if (target != null && target.exists() && unitInfo.currentOrder != Order.CastIrradiate) {
            UtilMicro.irradiate(unit, (PlayerUnit) target);
            getGs().wizard.addIrradiated(unit, (PlayerUnit) target);
            oldTarget = target;
        }
        if (oldTarget != null && (!oldTarget.exists() || ((PlayerUnit) oldTarget).isIrradiated())) oldTarget = null;
        if (target != null && (!target.exists() || ((PlayerUnit) target).isIrradiated())) target = null;
    }

    private void dMatrix() {
        if (unitInfo.currentOrder == Order.CastDefensiveMatrix) {
            if (target != null && oldTarget != null && !target.equals(oldTarget)) {
                UtilMicro.defenseMatrix(unit, (MobileUnit) target);
                getGs().wizard.addDefenseMatrixed(unit, (MobileUnit) target);
                oldTarget = target;
            }
        } else if (target != null && target.exists() && unitInfo.currentOrder != Order.CastDefensiveMatrix) {
            UtilMicro.defenseMatrix(unit, (MobileUnit) target);
            getGs().wizard.addDefenseMatrixed(unit, (MobileUnit) target);
            oldTarget = target;
        }
        if (oldTarget != null && (!oldTarget.exists() || ((MobileUnit) oldTarget).isDefenseMatrixed()))
            oldTarget = null;
        if (target != null && (!target.exists() || ((MobileUnit) target).isDefenseMatrixed())) target = null;
    }

    private void kite() {
        Set<UnitInfo> airThreats = airAttackers.stream().filter(u -> u.unitType == UnitType.Zerg_Scourge || u.unitType == UnitType.Zerg_Spore_Colony).collect(Collectors.toSet());
        if (!airThreats.isEmpty()) {
            Position kite = UtilMicro.kiteAway(unit, airThreats);
            if (kite != null) {
                UtilMicro.move(unit, kite);
                return;
            }
        }
        Position kite = UtilMicro.kiteAway(unit, airAttackers);
        if (kite == null || !getGs().getGame().getBWMap().isValidPosition(kite)) return;
        UtilMicro.move(unit, kite);
    }

    private void followSquad() {
        if (center == null || !getGs().getGame().getBWMap().isValidPosition(center)) return;
        UtilMicro.move(unit, center);
    }

    private void getNewStatus() {
        SimInfo mySimAir = getGs().sim.getSimulation(unitInfo, SimInfo.SimType.AIR);
        SimInfo mySimMix = getGs().sim.getSimulation(unitInfo, SimInfo.SimType.MIX);
        boolean chasenByScourge = false;
        boolean sporeColony = false;
        double maxScore = 0;
        PlayerUnit chosen = null;
        if (getGs().enemyRace == Race.Zerg && !mySimAir.enemies.isEmpty()) {
            for (UnitInfo u : mySimAir.enemies) {
                if (u.unit instanceof Scourge && u.target.equals(unit)) chasenByScourge = true;
                else if (u.unit instanceof SporeColony && unitInfo.toUnitInfoDistance().getDistance(u) < u.airRange * 1.2)
                    sporeColony = true;
                if (chasenByScourge && sporeColony) break;
            }
        }
        if (!mySimMix.enemies.isEmpty()) {
            // Irradiate
            Set<UnitInfo> irradiateTargets = new TreeSet<>(mySimMix.enemies);
            for (UnitInfo t : mySimMix.allies) {
                if (t.unit instanceof SiegeTank) irradiateTargets.add(t);
            }
            if (follow != null && !irradiateTargets.isEmpty() && getGs().getPlayer().hasResearched(TechType.Irradiate) && unit.getEnergy() >= TechType.Irradiate.energyCost() && follow.status != Squad.Status.IDLE) {
                for (UnitInfo u : irradiateTargets) {
                    if (!u.visible) continue;
                    if (u.unit instanceof Building || u.unit instanceof Egg || (!(u.unit instanceof Organic) && !(u.unit instanceof SiegeTank)))
                        continue;
                    if (u.unit instanceof MobileUnit && (u.unit.isIrradiated() || ((MobileUnit) u.unit).isStasised()))
                        continue;
                    if (getGs().wizard.isUnitIrradiated(u.unit)) continue;
                    double score = 1;
                    int closeUnits = 0;
                    for (UnitInfo close : irradiateTargets) {
                        if (u.equals(close) || !(close.unit instanceof Organic) || close.burrowed) continue;
                        if (u.toUnitInfoDistance().getDistance(close) <= 32) closeUnits++;
                    }
                    if (u.unitType == UnitType.Zerg_Lurker) score = u.burrowed ? 20 : 18; // Kill it with fire!!
                    else if (u.unitType == UnitType.Zerg_Mutalisk) score = 8;
                    else if (u.unitType == UnitType.Zerg_Hydralisk) score = 5;
                    else if (u.unitType == UnitType.Zerg_Zergling) score = 2;
                    score *= u.percentHealth; //Prefer healthy units
                    double multiplier = u.unit instanceof SiegeTank ? 3.75 : u.unitType == UnitType.Zerg_Lurker ? 2.5 : u.unitType == UnitType.Zerg_Mutalisk ? 2 : 1;
                    score += multiplier * closeUnits;
                    if (chosen == null || score > maxScore) {
                        chosen = u.unit;
                        maxScore = score;
                    }
                }
                if (maxScore >= 5) {
                    status = Status.IRRADIATE;
                    target = chosen;
                    return;
                }
            }
            chosen = null;
            maxScore = 0;

            // EMP
            Set<UnitInfo> empTargets = new TreeSet<>(mySimMix.enemies);
            if (follow != null && !empTargets.isEmpty() && getGs().getPlayer().hasResearched(TechType.EMP_Shockwave) && unit.getEnergy() >= TechType.EMP_Shockwave.energyCost() && follow.status != Squad.Status.IDLE) {
                for (UnitInfo u : empTargets) { // TODO Change to rectangle to choose best Position and track emped positions
                    if (!u.visible || u.unit instanceof Building || u.unit instanceof Worker || u.unit instanceof MobileUnit && (u.unit.isIrradiated() || ((MobileUnit) u.unit).isStasised()))
                        continue;
                    if (getGs().wizard.isUnitEMPed(u.unit)) continue;
                    double score = 1;
                    double closeUnits = 0;
                    for (UnitInfo close : empTargets) {
                        if (u.equals(close)) continue;
                        if (close.lastPosition.getDistance(u.lastPosition) <= WeaponType.EMP_Shockwave.innerSplashRadius())
                            closeUnits += close.shields * 0.6;
                    }
                    if (u.unitType == UnitType.Protoss_High_Templar) score = 10;
                    else if (u.unitType == UnitType.Protoss_Arbiter) score = 7;
                    else if (u.unitType == UnitType.Protoss_Archon || u.unitType == UnitType.Protoss_Dark_Archon)
                        score = 5;
                    score *= u.percentShield; //Prefer healthy units(shield)
                    double multiplier = u.unitType == UnitType.Protoss_High_Templar ? 6 : 1;
                    score += multiplier * closeUnits;
                    if (chosen == null || score > maxScore) {
                        chosen = u.unit;
                        maxScore = score;
                    }
                }
                if (maxScore >= 6) {
                    status = Status.EMP;
                    target = chosen;
                    return;
                }
            }
            chosen = null;
            maxScore = 0;
        }
        if (!mySimMix.allies.isEmpty()) {
            // Defense Matrix
            Set<UnitInfo> matrixTargets = new TreeSet<>(mySimMix.allies);
            if (follow != null && !matrixTargets.isEmpty() && unit.getEnergy() >= TechType.Defensive_Matrix.energyCost() && follow.status != Squad.Status.IDLE) {
                for (UnitInfo u : matrixTargets) {
                    if (!(u.unit instanceof MobileUnit)) continue;
                    if (getGs().wizard.isDefenseMatrixed((MobileUnit) u.unit)) continue;
                    double score = 1;
                    if (!u.unit.isUnderAttack() || ((MobileUnit) u.unit).isDefenseMatrixed()) continue;
                    if (u.unitType.isMechanical()) score = 8;
                    if (u.unitType == UnitType.Terran_Marine || u.unitType == UnitType.Terran_Firebat) score = 3;
                    if (u.unitType == UnitType.Terran_SCV || u.unitType == UnitType.Terran_Medic) score = 1;
                    score *= (double) u.unitType.maxHitPoints() / (double) u.health;
                    if (chosen == null || score > maxScore) {
                        chosen = u.unit;
                        maxScore = score;
                    }
                }
                if (maxScore >= 2) {
                    status = Status.DMATRIX;
                    target = chosen;
                    return;
                }
            }
        }
        if ((status == Status.IRRADIATE || status == Status.DMATRIX || status == Status.EMP) && target != null) return;
        if (!mySimAir.enemies.isEmpty()) {
            if (unit.isUnderAttack() || chasenByScourge || sporeColony) status = Status.KITE;
            else if (Util.broodWarDistance(unit.getPosition(), center) >= 100) status = Status.FOLLOW;
            else if (mySimAir.lose) status = Status.KITE;
        } else if (mySimMix.lose) status = Status.RETREAT;
        else if (Util.broodWarDistance(unit.getPosition(), center) >= 200) status = Status.FOLLOW;
        else status = Status.HOVER;
    }

    private void retreat() {
        Position CC = getGs().getNearestCC(myUnit.getPosition(), true);
        if (CC != null) ((MobileUnit) myUnit).move(CC);
        else ((MobileUnit) myUnit).move(getGs().getPlayer().getStartLocation().toPosition());
        attackPos = null;
        attackUnit = null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this.unit) return true;
        if (!(o instanceof VesselAgent)) return false;
        VesselAgent vessel = (VesselAgent) o;
        return unit.equals(vessel.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit);
    }

    @Override
    public int compareTo(Unit v1) {
        return this.unit.getId() - v1.getId();
    }

    enum Status {DMATRIX, KITE, FOLLOW, IDLE, RETREAT, IRRADIATE, HOVER, EMP}

}
