package ecgberht.BehaviourTrees.Harass;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;

public class HarassWorker extends Conditional {

    public HarassWorker(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).chosenUnitToHarass == null || ((GameState) this.handler).chosenHarasser == null) {
                return State.FAILURE;
            }
            if (((GameState) this.handler).chosenHarasser.attack(((GameState) this.handler).chosenUnitToHarass)) {
                return State.SUCCESS;
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
