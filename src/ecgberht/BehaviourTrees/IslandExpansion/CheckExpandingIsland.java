package ecgberht.BehaviourTrees.IslandExpansion;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;


public class CheckExpandingIsland extends Conditional {

    public CheckExpandingIsland(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).chosenWorkerDrop != null) {
                if (((GameState) this.handler).chosenDropShip == null || !((GameState) this.handler).chosenDropShip.statusToString().equals("IDLE")) {
                    return State.SUCCESS;
                }
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
