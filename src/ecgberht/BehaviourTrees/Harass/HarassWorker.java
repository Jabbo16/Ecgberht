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
            if (gameState.chosenUnitToHarass == null || gameState.chosenHarasser == null) return State.FAILURE;
            if (gameState.chosenHarasser.attack(gameState.chosenUnitToHarass)) return State.SUCCESS;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
