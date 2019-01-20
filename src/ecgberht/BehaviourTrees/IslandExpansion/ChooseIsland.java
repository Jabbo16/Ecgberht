package ecgberht.BehaviourTrees.IslandExpansion;

import bwem.Base;
import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.Position;

public class ChooseIsland extends Action {

    public ChooseIsland(String name, GameState gh) {
        super(name, gh);

    }

    @Override
    public State execute() {
        try {
            Base chosen = null;
            double distMax = Double.MAX_VALUE;
            Position drop = gameState.chosenDropShip.myUnit.getPosition();
            for (Base b : gameState.islandBases) {
                if (gameState.islandCCs.containsKey(b)) continue;
                double dist = Util.broodWarDistance(b.getLocation().toPosition(), drop);
                if (dist < distMax) {
                    distMax = dist;
                    chosen = b;
                }
            }
            if (chosen != null) {
                gameState.chosenIsland = chosen;
                return State.SUCCESS;
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