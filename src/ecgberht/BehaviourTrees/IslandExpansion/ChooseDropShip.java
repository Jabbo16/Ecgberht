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
            for (Agent u : gameState.agents.values()) {
                if (u instanceof DropShipAgent && u.statusToString().equals("IDLE")) {
                    gameState.chosenDropShip = (DropShipAgent) u;
                    return State.SUCCESS;
                }
            }
            gameState.chosenDropShip = null;
            gameState.chosenWorker = null;
            gameState.chosenIsland = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}