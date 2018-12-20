package ecgberht.BehaviourTrees.AddonBuild;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;

public class CheckResourcesAddon extends Conditional {

    public CheckResourcesAddon(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            MutablePair<Integer, Integer> cash = this.handler.getCash();
            if (cash.first >= (this.handler.chosenAddon.mineralPrice() + this.handler.deltaCash.first) && cash.second >= (this.handler.chosenAddon.gasPrice()) + this.handler.deltaCash.second) {
                return State.SUCCESS;
            }
            this.handler.chosenBuildingAddon = null;
            this.handler.chosenAddon = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
