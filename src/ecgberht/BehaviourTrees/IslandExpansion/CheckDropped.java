package ecgberht.BehaviourTrees.IslandExpansion;

import ecgberht.Agents.DropShipAgent;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;
import bwapi.Unit;


public class CheckDropped extends Conditional {

    public CheckDropped(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Unit scv = gameState.chosenWorkerDrop;
            DropShipAgent ship = gameState.chosenDropShip;
            if (ship == null) return State.SUCCESS;
            if (scv != null && ship.statusToString().equals("RETREAT")) {
                Unit transport = scv.getTransport();
                if (transport == null) {
                    gameState.chosenDropShip = null;
                    return State.SUCCESS;
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
