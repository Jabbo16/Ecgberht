package ecgberht.BehaviourTrees.Harass;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;

public class CheckExplorer extends Conditional {

    public CheckExplorer(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (!gameState.learningManager.defendHarass() && !gameState.explore) return State.FAILURE;
            else {
                gameState.chosenUnitToHarass = null;
                return State.SUCCESS;
            }
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
