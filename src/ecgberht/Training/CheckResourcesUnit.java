package ecgberht.Training;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.util.Pair;

public class CheckResourcesUnit extends Conditional {

    public CheckResourcesUnit(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Pair<Integer, Integer> cash = ((GameState) this.handler).getCash();
            if (cash.first >= (((GameState) this.handler).chosenUnit.mineralPrice() + ((GameState) this.handler).deltaCash.first) && cash.second >= (((GameState) this.handler).chosenUnit.gasPrice()) + ((GameState) this.handler).deltaCash.second) {
                return State.SUCCESS;
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            System.err.println(e);
            return State.ERROR;
        }
    }
}
