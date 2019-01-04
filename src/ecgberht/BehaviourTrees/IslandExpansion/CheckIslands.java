package ecgberht.BehaviourTrees.IslandExpansion;

import bwem.Base;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;


public class CheckIslands extends Conditional {

    public CheckIslands(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.islandBases.isEmpty() || !gameState.islandExpand) return State.FAILURE;
            for (Base b : gameState.islandBases) {
                if (!gameState.islandCCs.containsKey(b)) return State.SUCCESS;
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
