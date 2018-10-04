package ecgberht.BehaviourTrees.IslandExpansion;

import ecgberht.Agents.DropShipAgent;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.unit.Worker;


public class CheckDropped extends Conditional {

    public CheckDropped(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Worker scv = ((GameState) this.handler).chosenWorkerDrop;
            DropShipAgent ship = ((GameState) this.handler).chosenDropShip;
            if (ship == null) return State.SUCCESS;
            if (scv != null) {
                if (ship.statusToString().equals("RETREAT")) {
                    Unit transport = scv.getTransport();
                    if (transport == null) {
                        ((GameState) this.handler).chosenDropShip = null;
                        return State.SUCCESS;
                    }
                }
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
