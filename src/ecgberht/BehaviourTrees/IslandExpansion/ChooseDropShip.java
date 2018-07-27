package ecgberht.BehaviourTrees.IslandExpansion;

import ecgberht.Agents.Agent;
import ecgberht.Agents.DropShipAgent;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

public class ChooseDropShip extends Action {

    public ChooseDropShip(String name, GameHandler gh) {
        super(name, gh);

    }

    @Override
    public State execute() {
        try {
            for (Agent u : ((GameState) this.handler).agents.values()) {
                if (u instanceof DropShipAgent && u.statusToString().equals("IDLE")) {
                    ((GameState) this.handler).chosenDropShip = (DropShipAgent) u;
                    return State.SUCCESS;
                }
            }
            ((GameState) this.handler).chosenDropShip = null;
            ((GameState) this.handler).chosenWorker = null;
            ((GameState) this.handler).chosenIsland = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}