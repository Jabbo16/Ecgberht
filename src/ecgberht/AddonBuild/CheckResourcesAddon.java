package ecgberht.AddonBuild;

import bwapi.Pair;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;

public class CheckResourcesAddon extends Conditional {

    public CheckResourcesAddon(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Pair<Integer, Integer> cash = ((GameState) this.handler).getCash();
            if (cash.first >= (((GameState) this.handler).chosenAddon.mineralPrice() + ((GameState) this.handler).deltaCash.first) && cash.second >= (((GameState) this.handler).chosenAddon.gasPrice()) + ((GameState) this.handler).deltaCash.second) {
                return State.SUCCESS;
            }
            ((GameState) this.handler).chosenBuildingAddon = null;
            ((GameState) this.handler).chosenAddon = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            System.err.println(e);
            return State.ERROR;
        }
    }
}
