package ecgberht.BehaviourTrees.IslandExpansion;
import ecgberht.brain.*;

import ecgberht.Agents.Agent;
import ecgberht.Agents.DropShipAgent;
import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;

public class ChooseDropShip extends BrainAction {

    public ChooseDropShip(String name, GameState gh) {
        super(name, gh);

    }

    @Override
    public BrainStatus execute() {
        try {
            for (Agent u : gameState.mil.agents.values()) {
                if (u instanceof DropShipAgent && u.statusToString().equals("IDLE")) {
                    gameState.mil.chosenDropShip = (DropShipAgent) u;
                    return BrainStatus.SUCCESS;
                }
            }
            gameState.mil.chosenDropShip = null;
            gameState.chosenWorker = null;
            gameState.chosenIsland = null;
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}





