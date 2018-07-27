package ecgberht.Agents;

import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.unit.Dropship;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.unit.Worker;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static ecgberht.Ecgberht.getGs;

public class DropShipAgent extends Agent implements Comparable<Unit> {

    public Dropship unit;
    Status status = Status.IDLE;
    private Set<Unit> airAttackers = new TreeSet<>();
    private Set<Unit> cargoLoaded = new TreeSet<>();
    private Set<Unit> cargoWanted = new TreeSet<>();
    private Position target;
    private Unit pickingUp;

    public DropShipAgent(Unit unit) {
        super();
        this.unit = (Dropship) unit;
        this.myUnit = unit;
    }

    public String statusToString() {
        switch (status) {
            case PICKING:
                return "PICKING";
            case MOVING:
                return "MOVING";
            case DROP:
                return "DROP";
            case RETREAT:
                return "RETREAT";
            case IDLE:
                return "IDLE";
        }
        return "None";
    }

    public void setCargo(Set<Unit> cargo) {
        this.cargoWanted = cargo;
        for (Unit u : this.cargoWanted) {
            if (u instanceof Worker && (((Worker) u).isCarryingMinerals() || ((Worker) u).isCarryingGas())) {
                ((Worker) u).returnCargo();
                ((MobileUnit) u).rightClick(unit, true);
            } else ((MobileUnit) u).rightClick(unit, false);
        }
    }

    public void setTarget(Position target) {
        this.target = target;
    }

    private void checkLoaded() {
        if (pickingUp == null) return;
        Unit transport = ((MobileUnit) pickingUp).getTransport();
        if (transport != null && transport.equals(unit)) {
            cargoLoaded.add(pickingUp);
            cargoWanted.remove(pickingUp);
            pickingUp = null;
        }
    }

    private void checkUnloaded() {
        for (Unit u : cargoLoaded) {
            Unit transport = ((MobileUnit) u).getTransport();
            if (transport == null) cargoLoaded.remove(u);
            break;
        }
    }

    @Override
    public boolean runAgent() {
        try {
            if (!unit.exists()) return true;
            actualFrame = getGs().frameCount;
            frameLastOrder = unit.getLastCommandFrame();
            if (actualFrame == frameLastOrder) return false;
            airAttackers.clear();
            if (frameLastOrder == actualFrame) return false;
            status = getNewStatus();
            switch (status) {
                case PICKING:
                    picking();
                    break;
                case MOVING:
                    moving();
                    break;
                case DROP:
                    drop();
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
            System.err.println("Exception DropShipAgent");
            e.printStackTrace();
        }
        return false;
    }

    private void drop() {
        if (target == null) return;
        checkUnloaded();
        if (cargoLoaded.isEmpty()) return;
        if (unit.getOrder() == Order.MoveUnload || unit.getOrder() == Order.Unload) return;
        unit.unloadAll(target);
    }

    private void moving() {
        if (target == null) return;
        if (unit.getTargetPosition() != null && unit.getTargetPosition().equals(target)) return;
        unit.move(target);
    }

    private void picking() {
        if (cargoWanted.isEmpty()) return;
        if (pickingUp == null) {
            double distB = Double.MAX_VALUE;
            for (Unit u : cargoWanted) {
                double distA = getGs().broodWarDistance(unit.getPosition(), u.getPosition());
                if (pickingUp == null || distA < distB) {
                    pickingUp = u;
                    distB = distA;
                }
            }
            if (pickingUp != null) {
                unit.load((MobileUnit) pickingUp);
            }
        } else {
            if (unit.getOrderTarget() != null && unit.getOrderTarget().equals(pickingUp)) return;
            checkLoaded();
        }
    }

    private void idle() {
    }

    @Override
    protected void retreat() {
        Position CC = getGs().getNearestCC(myUnit.getPosition());
        if (CC != null) target = CC;
        else target = getGs().getPlayer().getStartLocation().toPosition();
        ((MobileUnit) myUnit).move(target);
    }

    private Status getNewStatus() {
        if (status == Status.IDLE) {
            if (!cargoWanted.isEmpty()) return Status.PICKING;
            if (!cargoLoaded.isEmpty() && target != null) return Status.MOVING;
        }
        if (status == Status.PICKING) {
            if (!cargoWanted.isEmpty()) return Status.PICKING;
            else if (!cargoLoaded.isEmpty() && target != null) return Status.MOVING;
        }
        if (status == Status.MOVING) {
            if (target == null) return Status.IDLE;
            if (getGs().broodWarDistance(unit.getPosition(), target) < 200) return Status.DROP;
            return Status.MOVING;
        }
        if (status == Status.DROP) {
            if (cargoLoaded.isEmpty()) return Status.RETREAT;
            else return Status.DROP;
        }
        if (status == Status.RETREAT) {
            if (target != null && getGs().broodWarDistance(unit.getPosition(), target) <= 64) return Status.IDLE;
            else return Status.RETREAT;
        }
        return Status.IDLE;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this.unit) return true;
        if (!(o instanceof DropShipAgent)) return false;
        DropShipAgent dropship = (DropShipAgent) o;
        return unit.equals(dropship.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit);
    }

    @Override
    public int compareTo(Unit v1) {
        return this.unit.getId() - v1.getId();
    }

    enum Status {PICKING, MOVING, DROP, RETREAT, IDLE}
}
