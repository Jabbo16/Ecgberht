package ecgberht.BehaviourTrees.Harass;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Conditional;

public class CheckExplorer extends Conditional {

    public CheckExplorer(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            if (!this.handler.EI.defendHarass) {
                return BehavioralTree.State.FAILURE;
            } else {
                this.handler.chosenUnitToHarass = null;
                return BehavioralTree.State.SUCCESS;
            }
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}
