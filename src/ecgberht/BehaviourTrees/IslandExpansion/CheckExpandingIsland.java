package ecgberht.BehaviourTrees.IslandExpansion;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;


public class CheckExpandingIsland extends Conditional {

    public CheckExpandingIsland(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (this.handler.chosenWorkerDrop != null) {
                if (this.handler.chosenDropShip == null || !this.handler.chosenDropShip.statusToString().equals("IDLE")) {
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
