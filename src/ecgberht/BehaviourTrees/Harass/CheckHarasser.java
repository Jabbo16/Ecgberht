package ecgberht.BehaviourTrees.Harass;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;

public class CheckHarasser extends Conditional {

    public CheckHarasser(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.chosenHarasser == null) return State.FAILURE;
            else {
                if (gameState.chosenHarasser.isIdle() || !gameState.chosenHarasser.isMoving() ||
                        !gameState.chosenHarasser.isAttacking()) {
                    gameState.chosenUnitToHarass = null;
                }
                return State.SUCCESS;
            }
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
