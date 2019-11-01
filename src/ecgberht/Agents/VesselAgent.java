package ecgberht.Agents;

import bwapi.*;
import ecgberht.Simulation.SimInfo;
import ecgberht.Squad;
import ecgberht.UnitInfo;
import ecgberht.Util.Util;
import ecgberht.Util.UtilMicro;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static ecgberht.Ecgberht.getGs;

public class VesselAgent extends Agent implements Comparable<Unit> {
    
    public Squad follow = null;
    private Status status = Status.IDLE;
    private Set<UnitInfo> airAttackers = new TreeSet<>();
    private Position center;
    private Unit target;
    private Unit oldTarget;

    public VesselAgent(Unit unit) {
        super();
        this.unitInfo = getGs().unitStorage.getAllyUnits().get(unit);
        this.myUnit = unit;
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
            if (!myUnit.exists() || unitInfo == null) return true;
            actualFrame = getGs().frameCount;
            frameLastOrder = myUnit.getLastCommandFrame();
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
                        getGs().wizard.irradiatedUnits.remove(myUnit);
                        status = Status.IDLE;
                        target = null;
                        oldTarget = null;
                    }
                    break;
                case IRRADIATE:
                    if (unitInfo.energy <= TechType.Irradiate.energyCost()) {
                        getGs().wizard.irradiatedUnits.remove(myUnit);
                        status = Status.IDLE;
                        target = null;
                        oldTarget = null;
                    }
                    break;
                case EMP:
                    if (unitInfo.energy <= TechType.EMP_Shockwave.energyCost()) {
                        getGs().wizard.EMPedUnits.remove(myUnit);
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
        if (attack == null || !attack.isValid(getGs().bw)) return;
        UtilMicro.move(myUnit, attack);
    }

    private Squad chooseVesselSquad() {
        Squad chosen = null;
        double scoreMax = Double.MIN_VALUE;
        for (Squad s : getGs().sqManager.squads.values()) {
            double dist = unitInfo.getDistance(s.getSquadCenter());
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
                UtilMicro.emp(myUnit, target.getPosition());
                getGs().wizard.addEMPed(myUnit, target);
                oldTarget = target;
            }
        } else if (target != null && target.exists() && unitInfo.currentOrder != Order.CastEMPShockwave) {
            UtilMicro.emp(myUnit, target.getPosition());
            getGs().wizard.addEMPed(myUnit, target);
            oldTarget = target;
        }
        if (oldTarget != null && (!oldTarget.exists() || oldTarget.getShields() <= 1)) oldTarget = null;
        if (target != null && (!target.exists() || target.getShields() <= 1)) target = null;
    }

    private void irradiate() {
        if (unitInfo.currentOrder == Order.CastIrradiate) {
            if (target != null && oldTarget != null && !target.equals(oldTarget)) {
                UtilMicro.irradiate(myUnit, target);
                getGs().wizard.addIrradiated(myUnit, target);
                oldTarget = target;
            }
        } else if (target != null && target.exists() && unitInfo.currentOrder != Order.CastIrradiate) {
            UtilMicro.irradiate(myUnit, target);
            getGs().wizard.addIrradiated(myUnit, target);
            oldTarget = target;
        }
        if (oldTarget != null && (!oldTarget.exists() || oldTarget.isIrradiated())) oldTarget = null;
        if (target != null && (!target.exists() || target.isIrradiated())) target = null;
    }

    private void dMatrix() {
        if (unitInfo.currentOrder == Order.CastDefensiveMatrix) {
            if (target != null && oldTarget != null && !target.equals(oldTarget)) {
                UtilMicro.defenseMatrix(myUnit, target);
                getGs().wizard.addDefenseMatrixed(myUnit, target);
                oldTarget = target;
            }
        } else if (target != null && target.exists() && unitInfo.currentOrder != Order.CastDefensiveMatrix) {
            UtilMicro.defenseMatrix(myUnit, target);
            getGs().wizard.addDefenseMatrixed(myUnit, target);
            oldTarget = target;
        }
        if (oldTarget != null && (!oldTarget.exists() || oldTarget.isDefenseMatrixed()))
            oldTarget = null;
        if (target != null && (!target.exists() || target.isDefenseMatrixed())) target = null;
    }

    private void kite() {
        Set<UnitInfo> airThreats = airAttackers.stream().filter(u -> u.unitType == UnitType.Zerg_Scourge || u.unitType == UnitType.Zerg_Spore_Colony).collect(Collectors.toSet());
        if (!airThreats.isEmpty()) {
            Position kite = UtilMicro.kiteAway(myUnit, airThreats);
            if (kite != null) {
                UtilMicro.move(myUnit, kite);
                return;
            }
        }
        Position kite = UtilMicro.kiteAway(myUnit, airAttackers);
        if (kite == null || !kite.isValid(getGs().getGame())) return;
        UtilMicro.move(myUnit, kite);
    }

    private void followSquad() {
        if (center == null || !center.isValid(getGs().bw)) return;
        UtilMicro.move(myUnit, center);
    }

    private void getNewStatus() {
        SimInfo mySimAir = getGs().sim.getSimulation(unitInfo, SimInfo.SimType.AIR);
        SimInfo mySimMix = getGs().sim.getSimulation(unitInfo, SimInfo.SimType.MIX);
        boolean chasenByScourge = false;
        boolean sporeColony = false;
        double maxScore = 0;
        Unit chosen = null;
        if (getGs().enemyRace == Race.Zerg && !mySimAir.enemies.isEmpty()) {
            for (UnitInfo u : mySimAir.enemies) {
                if (u.unitType == UnitType.Zerg_Scourge && myUnit.equals(u.target)) chasenByScourge = true;
                else if (u.unitType == UnitType.Zerg_Spore_Colony && u.unit.getDistance(myUnit) < u.airRange * 1.2) sporeColony = true;
                if (chasenByScourge && sporeColony) break;
            }
            if (!mySimMix.enemies.isEmpty()) {
                // Irradiate
                Set<UnitInfo> irradiateTargets = new TreeSet<>(mySimMix.enemies);
                for (UnitInfo t : mySimMix.allies) {
                    if (t.isTank()) irradiateTargets.add(t);
                }
                if (follow != null && !irradiateTargets.isEmpty() && getGs().getPlayer().hasResearched(TechType.Irradiate) && myUnit.getEnergy() >= TechType.Irradiate.energyCost() && follow.status != Squad.Status.IDLE) {
                    for (UnitInfo u : irradiateTargets) {
                        if (u.unitType.isBuilding() || u.isEgg() || (!u.unitType.isOrganic() && !u.isTank())) continue;
                        if (u.unit.isIrradiated() || u.unit.isStasised()) continue;
                        if (!u.visible) continue;
                        if (getGs().wizard.isUnitIrradiated(u.unit)) continue;
                        double score = 1;
                        int closeUnits = 0;
                        for (UnitInfo close : irradiateTargets) {
                            if (u.equals(close) || !close.unitType.isOrganic() || close.burrowed) continue;
                            if (close.getDistance(u) <= 32) closeUnits++;
                        }
                        if (u.unitType == UnitType.Zerg_Lurker) score = u.burrowed ? 20 : 18; // Kill it with fire!!
                        else if (u.unitType == UnitType.Zerg_Mutalisk) score = 8;
                        else if (u.unitType == UnitType.Zerg_Hydralisk) score = 6;
                        else if (u.unitType == UnitType.Zerg_Zergling) score = 3;
                        score *= u.percentHealth; //Prefer healthy units
                        double multiplier = u.isTank() ? 3.75 : u.unitType == UnitType.Zerg_Lurker ? 2.5 : u.unitType == UnitType.Zerg_Mutalisk ? 2 : 1;
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
                if (follow != null && !empTargets.isEmpty() && getGs().getPlayer().hasResearched(TechType.EMP_Shockwave) && myUnit.getEnergy() >= TechType.EMP_Shockwave.energyCost() && follow.status != Squad.Status.IDLE) {
                    for (UnitInfo u : empTargets) { // TODO Change to rectangle to choose best Position and track emped positions
                        if (u.unitType.isBuilding() || u.unitType.isWorker() || u.unit.isIrradiated() || u.unit.isStasised())
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
                    if (maxScore >= 7) {
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
                if (follow != null && !matrixTargets.isEmpty() && myUnit.getEnergy() >= TechType.Defensive_Matrix.energyCost() && follow.status != Squad.Status.IDLE) {
                    for (UnitInfo u : matrixTargets) {
                        if (!u.unitType.canMove()) continue;
                        if (getGs().wizard.isDefenseMatrixed(u.unit)) continue;
                        double score = 1;
                        if (!u.unit.isUnderAttack() || u.unit.isDefenseMatrixed()) continue;
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
            if ((status == Status.IRRADIATE || status == Status.DMATRIX || status == Status.EMP) && target != null)
                return;
            if (!mySimAir.enemies.isEmpty()) {
                if (myUnit.isUnderAttack() || chasenByScourge || sporeColony) status = Status.KITE;
                else if (Util.broodWarDistance(myUnit.getPosition(), center) >= 100) status = Status.FOLLOW;
                else if (mySimAir.lose) status = Status.KITE;
            } else if (mySimMix.lose) status = Status.RETREAT;
            else if (Util.broodWarDistance(myUnit.getPosition(), center) >= 200) status = Status.FOLLOW;
            else status = Status.HOVER;
        }
    }

    private void retreat() {
        Position CC = getGs().getNearestCC(myUnit.getPosition(), true);
        if (CC != null) myUnit.move(CC);
        else myUnit.move(getGs().getPlayer().getStartLocation().toPosition());
        attackPos = null;
        attackUnit = null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this.myUnit) return true;
        if (!(o instanceof VesselAgent)) return false;
        VesselAgent vessel = (VesselAgent) o;
        return myUnit.equals(vessel.myUnit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myUnit.getID());
    }

    @Override
    public int compareTo(Unit v1) {
        return this.myUnit.getID() - v1.getID();
    }

    enum Status {DMATRIX, KITE, FOLLOW, IDLE, RETREAT, IRRADIATE, HOVER, EMP}

}
