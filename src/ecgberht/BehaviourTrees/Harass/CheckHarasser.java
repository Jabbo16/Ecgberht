package ecgberht.BehaviourTrees.Harass;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.*;

public class CheckHarasser extends BrainAction {

    public CheckHarasser(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.chosenHarasser == null) return BrainStatus.FAILURE;
            else {
                if (gameState.chosenHarasser.isIdle() || !gameState.chosenHarasser.isMoving() ||
                        !gameState.chosenHarasser.isAttacking()) {
                    gameState.chosenUnitToHarass = null;
                }
                return BrainStatus.SUCCESS;
            }
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}

