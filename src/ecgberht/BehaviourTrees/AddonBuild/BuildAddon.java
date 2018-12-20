package ecgberht.BehaviourTrees.AddonBuild;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;

public class BuildAddon extends Action {

    public BuildAddon(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (!this.handler.defense && this.handler.chosenToBuild == UnitType.Terran_Command_Center) {
                boolean found = false;
                for (MutablePair<UnitType, TilePosition> w : this.handler.workerBuild.values()) {
                    if (w.first == UnitType.Terran_Command_Center) {
                        found = true;
                        break;
                    }
                }
                if (!found) return State.FAILURE;
            }
            if (this.handler.chosenBuildingAddon.getAddon() == null && this.handler.chosenBuildingAddon.build(this.handler.chosenAddon)) {
                this.handler.chosenBuildingAddon = null;
                this.handler.chosenAddon = null;
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
