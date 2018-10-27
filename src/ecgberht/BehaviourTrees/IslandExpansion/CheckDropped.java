package ecgberht.BehaviourTrees.IslandExpansion;

import ecgberht.Agents.DropShipAgent;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Conditional;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.unit.Worker;


public class CheckDropped extends Conditional {

    public CheckDropped(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            Worker scv = this.handler.chosenWorkerDrop;
            DropShipAgent ship = this.handler.chosenDropShip;
            if (ship == null) return BehavioralTree.State.SUCCESS;
            if (scv != null) {
                if (ship.statusToString().equals("RETREAT")) {
                    Unit transport = scv.getTransport();
                    if (transport == null) {
                        this.handler.chosenDropShip = null;
                        return BehavioralTree.State.SUCCESS;
                    }
                }
            }
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}
