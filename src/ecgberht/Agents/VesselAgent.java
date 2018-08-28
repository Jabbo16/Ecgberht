package ecgberht.Agents;

import ecgberht.Simulation.SimInfo;
import ecgberht.Squad;
import ecgberht.Util.Util;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.WeaponType;
import org.openbw.bwapi4j.unit.*;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static ecgberht.Ecgberht.getGs;

public class VesselAgent extends Agent implements Comparable<Unit> {

    public ScienceVessel unit;
    public Squad follow = null;
    private Status status = Status.IDLE;
    private Set<Unit> airAttackers = new TreeSet<>();
    private Position center;
    private Unit target;

    public VesselAgent(Unit unit) {
        super();
        this.unit = (ScienceVessel) unit;
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
            if (!unit.exists()) return true;
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
            center = follow.getSquadCenter();
            getNewStatus();
            switch (status) {
                case IRRADIATE:
                    irradiate();
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
        if (attack == null) return;
        if (!getGs().getGame().getBWMap().isValidPosition(attack)) return;
        Position target = unit.getOrderTargetPosition();
        if (target != null && !target.equals(attack)) unit.move(attack);
        else if (target == null) unit.move(attack);
    }

    private Squad chooseVesselSquad() {
        Squad chosen = null;
        double scoreMax = Double.MIN_VALUE;
        for (Squad s : getGs().sqManager.squads.values()) {
            double dist = s.getSquadCenter().getDistance(unit.getPosition());
            double score = -Math.pow(s.members.size(), 3) / dist;
            if (chosen == null || score > scoreMax) {
                chosen = s;
                scoreMax = dist;
            }
        }
        return chosen;
    }

    private void emp() {
        if (target != null && target.exists() && unit.getOrder() != Order.CastEMPShockwave) {
            unit.empShockWave(target.getPosition());
            getGs().wizard.addEMPed(unit, (PlayerUnit) target);
        } else target = null;
    }

    private void irradiate() {
        if (target != null && target.exists() && unit.getOrder() != Order.CastIrradiate) {
            unit.irradiate((PlayerUnit) target);
            getGs().wizard.addIrradiated(unit, (PlayerUnit) target);
        } else target = null;
    }

    private void dMatrix() {
        if (target != null && target.exists() && unit.getOrder() != Order.CastDefensiveMatrix) {
            unit.defensiveMatrix((PlayerUnit) target);
            getGs().wizard.addDefenseMatrixed(unit, (MobileUnit) target);
        } else target = null;
    }

    private void kite() {
        Position kite = getGs().kiteAway(unit, airAttackers);
        if (kite == null || !getGs().getGame().getBWMap().isValidPosition(kite)) return;
        Position target = unit.getOrderTargetPosition();
        if (target != null && !target.equals(kite)) unit.move(kite);
        if (target == null) unit.move(kite);
    }

    private void followSquad() {
        if (!getGs().getGame().getBWMap().isValidPosition(center)) return;
        Position target = unit.getOrderTargetPosition();
        if (target != null && !target.equals(center)) unit.move(center);
        if (target == null) unit.move(center);
    }

    private void getNewStatus() {
        SimInfo mySimAir = getGs().sim.getSimulation(unit, SimInfo.SimType.AIR);
        SimInfo mySimMix = getGs().sim.getSimulation(unit, SimInfo.SimType.MIX);
        boolean chasenByScourge = false;
        boolean sporeColony = false;
        double maxScore = 0;
        PlayerUnit chosen = null;
        if (getGs().enemyRace == Race.Zerg && !mySimAir.enemies.isEmpty()) {
            for (Unit u : mySimAir.enemies) {
                if (u instanceof Scourge && ((Scourge) u).getOrderTarget().equals(unit)) {
                    chasenByScourge = true;
                } else if (u instanceof SporeColony && u.getDistance(unit) < ((SporeColony) u).getAirWeapon().maxRange() * 1.1) {
                    sporeColony = true;
                }
                if (chasenByScourge && sporeColony) break;
            }
        }
        if (!mySimMix.enemies.isEmpty()) {
            // Irradiate
            Set<Unit> irradiateTargets = new TreeSet<>(mySimMix.enemies);
            for (Unit t : mySimMix.allies) {
                if (t instanceof SiegeTank) irradiateTargets.add(t);
            }
            if (follow != null && !irradiateTargets.isEmpty() && getGs().getPlayer().hasResearched(TechType.Irradiate) && unit.getEnergy() >= TechType.Irradiate.energyCost() && follow.status != Squad.Status.IDLE) {
                for (Unit u : irradiateTargets) {
                    if (u instanceof Building || u instanceof Egg || (!(u instanceof Organic) && !(u instanceof SiegeTank)))
                        continue;
                    if (u instanceof MobileUnit && (((MobileUnit) u).isIrradiated() || ((MobileUnit) u).isStasised()))
                        continue;
                    if (getGs().wizard.isUnitIrradiated(u)) continue;
                    double score = 1;
                    int closeUnits = 0;
                    for (Unit close : irradiateTargets) {
                        if (u.equals(close) || !(close instanceof Organic)) continue;
                        if (close.getDistance(u) <= 32) closeUnits++;
                    }
                    if (u instanceof Lurker) score = ((Lurker) u).isBurrowed() ? 14 : 12;
                    else if (u instanceof Mutalisk) score = 8;
                    else if (u instanceof Hydralisk) score = 6;
                    else if (u instanceof Zergling) score = 3;
                    score *= ((double) ((PlayerUnit) u).getHitPoints()) / (double) (((PlayerUnit) u).maxHitPoints()); //Prefer healthy units
                    double multiplier = u instanceof SiegeTank ? 3.5 : u instanceof Lurker ? 1.5 : 0.75;
                    score += multiplier * closeUnits;
                    if (chosen == null || score > maxScore) {
                        chosen = (PlayerUnit) u;
                        maxScore = score;
                    }
                }
                if (maxScore >= 5.5) {
                    status = Status.IRRADIATE;
                    target = chosen;
                    return;
                }
            }
            chosen = null;
            maxScore = 0;

            // EMP
            Set<Unit> empTargets = new TreeSet<>(mySimMix.enemies);
            if (follow != null && !empTargets.isEmpty() && getGs().getPlayer().hasResearched(TechType.EMP_Shockwave) && unit.getEnergy() >= TechType.EMP_Shockwave.energyCost() && follow.status != Squad.Status.IDLE) {
                for (Unit u : empTargets) { // TODO Change to rectangle to choose best Position and track emped positions
                    if (u instanceof Building || u instanceof Worker || u instanceof MobileUnit && (((MobileUnit) u).isIrradiated() || ((MobileUnit) u).isStasised()))
                        continue;
                    if (getGs().wizard.isUnitEMPed(u)) continue;
                    double score = 1;
                    double closeUnits = 0;
                    for (Unit close : empTargets) {
                        if (u.equals(close)) continue;
                        if (close.getPosition().getDistance(u.getPosition()) <= WeaponType.EMP_Shockwave.innerSplashRadius())
                            closeUnits += ((PlayerUnit) close).getShields() * 0.6;
                    }
                    if (u instanceof HighTemplar) score = 10;
                    else if (u instanceof Arbiter) score = 7;
                    else if (u instanceof Archon || u instanceof DarkArchon) score = 5;
                    score *= ((double) ((PlayerUnit) u).getShields()) / (double) (((PlayerUnit) u).maxShields()); //Prefer healthy units(shield)
                    double multiplier = u instanceof HighTemplar ? 6 : 1;
                    score += multiplier * closeUnits;
                    if (chosen == null || score > maxScore) {
                        chosen = (PlayerUnit) u;
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
            Set<Unit> matrixTargets = new TreeSet<>(mySimMix.allies);
            if (follow != null && !matrixTargets.isEmpty() && unit.getEnergy() >= TechType.Defensive_Matrix.energyCost() && follow.status != Squad.Status.IDLE) {
                for (Unit u : matrixTargets) {
                    if (!(u instanceof MobileUnit)) continue;
                    if (getGs().wizard.isDefenseMatrixed(u)) continue;
                    int score = 1;
                    if (!((PlayerUnit) u).isUnderAttack() || ((MobileUnit) u).isDefenseMatrixed()) continue;
                    if (u instanceof Mechanical) score = 6;
                    if (u instanceof Marine) score = 3;
                    if (u instanceof SCV || u instanceof Medic) score = 1;
                    score *= ((PlayerUnit) u).maxHitPoints() / ((PlayerUnit) u).getHitPoints();
                    if (chosen == null || score > maxScore) {
                        chosen = (PlayerUnit) u;
                        maxScore = score;
                    }
                }
                if (maxScore > 2) {
                    status = Status.DMATRIX;
                    target = chosen;
                    return;
                }
            }
        }
        if (!mySimAir.enemies.isEmpty()) {
            if (unit.isUnderAttack() || chasenByScourge || sporeColony) status = Status.KITE;
            else if (Util.broodWarDistance(unit.getPosition(), center) >= 100) status = Status.FOLLOW;
            else if (mySimAir.lose) status = Status.KITE;
        } else if (mySimMix.lose) status = Status.RETREAT;
        else if (Util.broodWarDistance(unit.getPosition(), center) >= 200) status = Status.FOLLOW;
        else status = Status.HOVER;
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
