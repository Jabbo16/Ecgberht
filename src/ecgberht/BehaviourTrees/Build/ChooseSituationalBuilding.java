package ecgberht.BehaviourTrees.Build;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

public class ChooseSituationalBuilding extends Action {

    public ChooseSituationalBuilding(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {

            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
