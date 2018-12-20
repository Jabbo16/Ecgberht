package ecgberht.BehaviourTrees.IslandExpansion;

import ecgberht.Agents.Agent;
import ecgberht.Agents.DropShipAgent;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;

public class ChooseDropShip extends Action {

    public ChooseDropShip(String name, GameState gh) {
        super(name, gh);

    }

    @Override
    public State execute() {
        try {
            for (Agent u : this.handler.agents.values()) {
                if (u instanceof DropShipAgent && u.statusToString().equals("IDLE")) {
                    this.handler.chosenDropShip = (DropShipAgent) u;
                    return State.SUCCESS;
                }
            }
            this.handler.chosenDropShip = null;
            this.handler.chosenWorker = null;
            this.handler.chosenIsland = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}