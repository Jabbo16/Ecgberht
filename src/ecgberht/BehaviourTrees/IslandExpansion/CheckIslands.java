package ecgberht.BehaviourTrees.IslandExpansion;

import bwem.Base;
import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.*;


public class CheckIslands extends BrainAction {

    public CheckIslands(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.islandBases.isEmpty() || !gameState.islandExpand) return BrainStatus.FAILURE;
            for (Base b : gameState.islandBases) {
                if (!gameState.islandCCs.containsKey(b)) return BrainStatus.SUCCESS;
            }
            gameState.chosenDropShip = null;
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

