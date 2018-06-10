package ecgberht.MoveToBuild;

import bwapi.Pair;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

public class ChooseBay extends Action {

    public ChooseBay(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).getArmySize() < ((GameState) this.handler).strat.armyForBay) {
                return State.FAILURE;
            }
            if (((GameState) this.handler).getPlayer().allUnitCount(UnitType.Terran_Engineering_Bay) < ((GameState) this.handler).strat.numBays) {
                for (Pair<Unit, Pair<UnitType, TilePosition>> w : ((GameState) this.handler).workerBuild) {
                    if (w.second.first == UnitType.Terran_Engineering_Bay) {
                        return State.FAILURE;
                    }
                }
                for (Pair<Unit, Unit> w : ((GameState) this.handler).workerTask) {
                    if (w.second.getType() == UnitType.Terran_Engineering_Bay) {
                        return State.FAILURE;
                    }
                }
                ((GameState) this.handler).chosenToBuild = UnitType.Terran_Engineering_Bay;
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
