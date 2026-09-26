package ecgberht.BehaviourTrees.IslandExpansion;
import ecgberht.brain.*;

import bwem.Base;
import ecgberht.GameState;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.Position;

public class ChooseIsland extends BrainAction {

    public ChooseIsland(String name, GameState gh) {
        super(name, gh);

    }

    @Override
    public BrainStatus execute() {
        try {
            Base chosen = null;
            double distMax = Double.MAX_VALUE;
            Position drop = gameState.mil.chosenDropShip.unit.getPosition();
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
                return BrainStatus.SUCCESS;
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





