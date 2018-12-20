package ecgberht.BehaviourTrees.Harass;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;

public class HarassWorker extends Conditional {

    public HarassWorker(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (this.handler.chosenUnitToHarass == null || this.handler.chosenHarasser == null) return State.FAILURE;
            if (this.handler.chosenHarasser.attack(this.handler.chosenUnitToHarass)) return State.SUCCESS;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
