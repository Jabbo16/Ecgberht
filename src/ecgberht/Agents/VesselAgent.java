package ecgberht.Agents;

import ecgberht.EnemyBuilding;
import ecgberht.Simulation.SimInfo;
import ecgberht.Squad;
import ecgberht.Util.Util;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;
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

    private void irradiate() {
        if (target != null && target.exists() && unit.getOrder() != Order.CastIrradiate)
            unit.irradiate((PlayerUnit) target);
        else target = null;
    }

    private void dMatrix() {
        if (target != null && target.exists() && unit.getOrder() != Order.CastDefensiveMatrix)
            unit.defensiveMatrix((PlayerUnit) target);
        else target = null;
    }

    private void kite() {
        Position kite = getGs().kiteAway(unit, airAttackers);
        if (!getGs().getGame().getBWMap().isValidPosition(kite)) return;
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
        Position myPos = unit.getPosition();
        double maxScore = 0;
        PlayerUnit chosen = null;
        // Irradiate
        Set<Unit> irradiateTargets = new TreeSet<>(getGs().sim.getSimulation(unit, SimInfo.SimType.MIX).enemies);
        for (Unit t : getGs().sim.getSimulation(unit, SimInfo.SimType.MIX).allies) {
            if (t instanceof SiegeTank) irradiateTargets.add(t);
        }
        if (follow != null && !irradiateTargets.isEmpty() && getGs().getPlayer().hasResearched(TechType.Irradiate) && unit.getEnergy() >= TechType.Irradiate.energyCost() && follow.status != Squad.Status.IDLE) {
            for (Unit u : irradiateTargets) {
                if (u instanceof Building || (!(u instanceof Organic) && !(u instanceof SiegeTank))) continue;
                if (u instanceof MobileUnit && (((MobileUnit) u).isIrradiated() || ((MobileUnit) u).isStasised()))
                    continue;
                double score = 1;
                int closeUnits = 0;
                for (Unit close : irradiateTargets) {
                    if (u.equals(close) || !(close instanceof Organic)) continue;
                    if (close.getDistance(u) <= 64) closeUnits++;
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
        // Defense Matrix
        Set<Unit> matrixTargets = new TreeSet<>(getGs().sim.getSimulation(unit, SimInfo.SimType.MIX).allies);
        if (follow != null && !matrixTargets.isEmpty() && unit.getEnergy() >= TechType.Defensive_Matrix.energyCost() && follow.status != Squad.Status.IDLE) {
            for (Unit u : matrixTargets) {
                if (!(u instanceof MobileUnit)) continue;
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

        for (Unit u : getGs().enemyCombatUnitMemory) {
            double dist = Util.broodWarDistance(u.getPosition(), myPos);
            if (dist <= 700 && u instanceof AirAttacker) airAttackers.add(u);
        }
        for (EnemyBuilding u : getGs().enemyBuildingMemory.values()) {
            if (!getGs().getGame().getBWMap().isVisible(u.pos)) continue;
            double dist = Util.broodWarDistance(u.pos.toPosition(), myPos);
            if (dist <= 700 && (u.unit instanceof AirAttacker || u.type == UnitType.Terran_Bunker) && u.unit.isCompleted()) {
                airAttackers.add(u.unit);
            }
        }
        if (!airAttackers.isEmpty() && getGs().sim.getSimulation(unit, SimInfo.SimType.AIR).lose) {
            status = unit.isUnderAttack() ? Status.KITE : Status.FOLLOW;
        } else if (getGs().sim.getSimulation(unit, SimInfo.SimType.MIX).lose) status = Status.RETREAT;
        else if (Util.broodWarDistance(unit.getPosition(), center) >= 300) status = Status.FOLLOW;
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

    enum Status {DMATRIX, KITE, FOLLOW, IDLE, RETREAT, IRRADIATE, HOVER}

}
