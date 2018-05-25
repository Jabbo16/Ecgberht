package ecgberht.Harass;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;

public class CheckHarasser extends Conditional {

    public CheckHarasser(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).chosenHarasser == null) {
                return State.FAILURE;
            } else {
                if (((GameState) this.handler).chosenHarasser.isIdle()) {
                    ((GameState) this.handler).chosenUnitToHarass = null;
                }
                ((GameState)this.handler).EI.defendHarass = true;
                return State.SUCCESS;
            }
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            System.err.println(e);
            return State.ERROR;
        }
    }
}
