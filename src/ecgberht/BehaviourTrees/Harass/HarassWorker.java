package ecgberht.BehaviourTrees.Harass;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Conditional;

public class HarassWorker extends Conditional {

    public HarassWorker(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            if (this.handler.chosenUnitToHarass == null || this.handler.chosenHarasser == null) {
                return BehavioralTree.State.FAILURE;
            }
            if (this.handler.chosenHarasser.attack(this.handler.chosenUnitToHarass)) {
                return BehavioralTree.State.SUCCESS;
            }
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}
