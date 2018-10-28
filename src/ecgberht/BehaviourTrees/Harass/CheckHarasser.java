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
            if (this.handler.chosenHarasser == null) return State.FAILURE;
            else {
                if (this.handler.chosenHarasser.isIdle() || !this.handler.chosenHarasser.isMoving() ||
                        !this.handler.chosenHarasser.isAttacking()) {
                    this.handler.chosenUnitToHarass = null;
                }
                this.handler.learningManager.setHarass(true);
                return State.SUCCESS;
            }
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
