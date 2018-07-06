package ecgberht.Agents;

import ecgberht.EnemyBuilding;
import ecgberht.Simulation.SimInfo;
import ecgberht.Squad;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static ecgberht.Ecgberht.getGs;

public class VesselAgent extends Agent implements Comparable<Unit> {

    public ScienceVessel unit;
    public Squad follow;
    Status status = Status.IDLE;
    private Set<Unit> airAttackers = new TreeSet<>();
    private Position center;
    private Unit target;

    public VesselAgent(Unit unit, Squad s) {
        super();
        this.unit = (ScienceVessel) unit;
        this.myUnit = unit;
        follow = s;
    }

    public String statusToString() {
        if (status == Status.KITE) return "Kite";
        if (status == Status.FOLLOW) return "Follow";
        if (status == Status.RETREAT) return "Retreat";
        if (status == Status.IDLE) return "Idle";
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
            if (follow == null || follow.members.isEmpty()) follow = getGs().chooseVesselSquad(unit.getPosition());
            if (follow == null) {
                status = Status.RETREAT;
                retreat();
                return false;
            }
            center = getGs().getSquadCenter(follow);
            getNewStatus();
            switch (status) {
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
                case IDLE:
                    idle();
                    break;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Exception VesselAgent");
            e.printStackTrace();
        }
        return false;
    }

    private void dMatrix() {
        if (target != null) {
            if (!target.exists()) target = null;
            else if (unit.getOrder() != Order.CastDefensiveMatrix) unit.defensiveMatrix((PlayerUnit) target);
            target = null;
        }
    }

    private void idle() {
        if (follow.attack != null) {
            Position attack = follow.attack;
            if (!getGs().getGame().getBWMap().isValidPosition(attack)) return;
            Position target = unit.getOrderTargetPosition();
            if (target != null && !target.equals(attack)) unit.move(attack);
            if (target == null) unit.move(attack);
        }
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
        int maxScore = 0;
        PlayerUnit chosen = null;
        if (follow != null && !follow.members.isEmpty() && unit.getEnergy() >= 100 && follow.status != Squad.Status.IDLE) {
            for (PlayerUnit u : follow.members) {
                int score = 1;
                if (!u.isUnderAttack() || ((MobileUnit) u).isDefenseMatrixed()) continue;
                if (u instanceof Mechanical) score = 5;
                if (u instanceof Marine) score = 3;
                if (u instanceof SCV || u instanceof Medic) score = 1;
                score *= u.maxHitPoints() / u.getHitPoints();
                if (chosen == null || score > maxScore) {
                    chosen = u;
                    maxScore = score;
                }
            }
        }
        if (maxScore > 2 && chosen != null) {
            status = Status.DMATRIX;
            target = chosen;
            return;
        }
        for (Unit u : getGs().enemyCombatUnitMemory) {
            double dist = getGs().broodWarDistance(u.getPosition(), myPos);
            if (dist <= 700 && u instanceof AirAttacker) airAttackers.add(u);
        }
        for (EnemyBuilding u : getGs().enemyBuildingMemory.values()) {
            if (!getGs().getGame().getBWMap().isVisible(u.pos)) continue;
            double dist = getGs().broodWarDistance(u.pos.toPosition(), myPos);
            if (dist <= 700 && (u.unit instanceof AirAttacker || u.type == UnitType.Terran_Bunker) && u.unit.isCompleted()) {
                airAttackers.add(u.unit);
            }
        }
        if (!airAttackers.isEmpty() && getGs().sim.getSimulation(unit, SimInfo.SimType.AIR).lose) {
            status = Status.KITE;
            return;
        }
        if (getGs().sim.getSimulation(unit, SimInfo.SimType.MIX).lose) {
            status = Status.RETREAT;
            return;
        } else if (getGs().broodWarDistance(unit.getPosition(), center) >= 80) {
            status = Status.FOLLOW;
        } else status = Status.IDLE;

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

    enum Status {DMATRIX, KITE, FOLLOW, IDLE, RETREAT}

}
