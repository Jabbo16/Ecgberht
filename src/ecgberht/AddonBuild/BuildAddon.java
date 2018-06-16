package ecgberht.AddonBuild;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.util.Pair;

public class BuildAddon extends Action {

    public BuildAddon(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (!((GameState) this.handler).defense) {
                if (((GameState) this.handler).chosenToBuild == UnitType.Terran_Command_Center) {
                    boolean found = false;
                    for (Pair<UnitType, TilePosition> w : ((GameState) this.handler).workerBuild.values()) {
                        if (w.first == UnitType.Terran_Command_Center) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) return State.FAILURE;
                }
            }
            if (((GameState) this.handler).chosenBuildingAddon.getAddon() == null) {
                if (((GameState) this.handler).chosenBuildingAddon.build(((GameState) this.handler).chosenAddon)) {
                    ((GameState) this.handler).chosenBuildingAddon = null;
                    ((GameState) this.handler).chosenAddon = null;
                    return State.SUCCESS;
                }
            }
            ((GameState) this.handler).chosenBuildingAddon = null;
            ((GameState) this.handler).chosenAddon = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
