package ecgberht.AddonBuild;

import bwapi.Unit;
import bwapi.UnitType;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

public class ChooseMachineShop extends Action {

    public ChooseMachineShop(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (!((GameState) this.handler).Fs.isEmpty()) {
                for (Unit c : ((GameState) this.handler).Fs) {
                    if (!c.isTraining() && c.getAddon() == null) {
                        ((GameState) this.handler).chosenBuildingAddon = c;
                        ((GameState) this.handler).chosenAddon = UnitType.Terran_Machine_Shop;
                        return State.SUCCESS;
                    }
                }
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
