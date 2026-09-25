package ecgberht.BehaviourTrees.IslandExpansion;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.*;


public class CheckExpandingIsland extends BrainAction {

    public CheckExpandingIsland(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.chosenWorkerDrop != null && (gameState.chosenDropShip == null
                    || !gameState.chosenDropShip.statusToString().equals("IDLE"))) {
                return BrainStatus.SUCCESS;
            }
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}

