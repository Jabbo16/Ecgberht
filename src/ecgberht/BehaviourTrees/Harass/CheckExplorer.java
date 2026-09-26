package ecgberht.BehaviourTrees.Harass;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.*;

public class CheckExplorer extends BrainAction {

    public CheckExplorer(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (!gameState.learningManager.defendHarass() && !gameState.explore && gameState.getStrat().harass)
                return BrainStatus.FAILURE;
            else {
                gameState.chosenUnitToHarass = null;
                return BrainStatus.SUCCESS;
            }
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}






