package ecgberht.Agents;

import bwapi.Order;
import ecgberht.UnitInfo;
import ecgberht.Util.Util;
import bwapi.Position;
import bwapi.Unit;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static ecgberht.Ecgberht.getGs;

public class DropShipAgent extends Agent implements Comparable<Unit> {

    Status status = Status.IDLE;
    private Set<UnitInfo> airAttackers = new TreeSet<>();
    private Set<Unit> cargoLoaded = new TreeSet<>();
    private Set<Unit> cargoWanted = new TreeSet<>();
    private Position target;
    private Unit pickingUp;

    public DropShipAgent(Unit unit) {
        super();
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
            if (u.getType().isWorker() && (u.isCarryingMinerals() ||  u.isCarryingGas())) {
                u.returnCargo();
                u.rightClick(myUnit, true);
            } else u.rightClick(myUnit, false);
        }
    }

    public void setTarget(Position target) {
        this.target = target;
    }

    private void checkLoaded() {
        if (pickingUp == null) return;
        Unit transport = pickingUp.getTransport();
        if (transport != null && transport.equals(myUnit)) {
            cargoLoaded.add(pickingUp);
            cargoWanted.remove(pickingUp);
            pickingUp = null;
        }
    }

    private void checkUnloaded() {
        for (Unit u : cargoLoaded) {
            Unit transport = u.getTransport();
            if (transport == null) {
                cargoLoaded.remove(u);
                break;
            }
        }
    }

    @Override
    public boolean runAgent() {
        try {
            if (!myUnit.exists() || unitInfo == null) return true;
            actualFrame = getGs().frameCount;
            frameLastOrder = myUnit.getLastCommandFrame();
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
        if (myUnit.getOrder() == Order.MoveUnload || myUnit.getOrder() == Order.Unload) return;
        myUnit.unloadAll(target);
    }

    private void moving() {
        if (target == null) return;
        if (myUnit.getTargetPosition() != null && myUnit.getTargetPosition().equals(target)) return;
        myUnit.move(target);
    }

    private void picking() {
        if (cargoWanted.isEmpty()) return;
        if (pickingUp == null) {
            double distB = Double.MAX_VALUE;
            for (Unit u : cargoWanted) {
                double distA = Util.broodWarDistance(myUnit.getPosition(), u.getPosition());
                if (pickingUp == null || distA < distB) {
                    pickingUp = u;
                    distB = distA;
                }
            }
            if (pickingUp != null) {
                myUnit.load(pickingUp);
            }
        } else {
            if (myUnit.getOrderTarget() != null && myUnit.getOrderTarget().equals(pickingUp)) return;
            checkLoaded();
        }
    }

    private void idle() {
    }

    protected void retreat() {
        Position CC = getGs().getNearestCC(myUnit.getPosition(), false);
        if (CC != null) target = CC;
        else target = getGs().getPlayer().getStartLocation().toPosition();
        myUnit.move(target);
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
            if (Util.broodWarDistance(myUnit.getPosition(), target) < 200) return Status.DROP;
            return Status.MOVING;
        }
        if (status == Status.DROP) {
            if (cargoLoaded.isEmpty()) return Status.RETREAT;
            else return Status.DROP;
        }
        if (status == Status.RETREAT) {
            if (target != null && Util.broodWarDistance(myUnit.getPosition(), target) <= 64) return Status.IDLE;
            else return Status.RETREAT;
        }
        return Status.IDLE;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this.myUnit) return true;
        if (!(o instanceof DropShipAgent)) return false;
        DropShipAgent dropship = (DropShipAgent) o;
        return myUnit.equals(dropship.myUnit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myUnit.getID());
    }

    @Override
    public int compareTo(Unit v1) {
        return this.myUnit.getID() - v1.getID();
    }

    enum Status {PICKING, MOVING, DROP, RETREAT, IDLE}
}