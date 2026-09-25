package ecgberht.BehaviourTrees.Harass;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.*;

public class HarassWorker extends BrainAction {

    public HarassWorker(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.chosenUnitToHarass == null || gameState.chosenHarasser == null) return BrainStatus.FAILURE;
            if (gameState.chosenHarasser.attack(gameState.chosenUnitToHarass)) return BrainStatus.SUCCESS;
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}

