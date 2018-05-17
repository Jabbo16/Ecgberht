package ecgberht.MoveToBuild;

import bwapi.Pair;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

public class ChooseBarracks extends Action {

    public ChooseBarracks(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).strat.name != "ProxyBBS") {
                if (!((GameState) this.handler).MBs.isEmpty() && ((GameState) this.handler).countUnit(UnitType.Terran_Barracks) == ((GameState) this.handler).strat.numRaxForAca && ((GameState) this.handler).countUnit(UnitType.Terran_Academy) == 0) {
                    return State.FAILURE;
                }
                if (((GameState) this.handler).countUnit(UnitType.Terran_Barracks) == ((GameState) this.handler).strat.numRaxForAca && ((GameState) this.handler).countUnit(UnitType.Terran_Refinery) == 0) {
                    return State.FAILURE;
                }
            } else {
                if (((GameState) this.handler).getPlayer().supplyUsed() < 16) {
                    return State.FAILURE;
                }
            }
            if (((GameState) this.handler).strat.buildUnits.contains(UnitType.Terran_Factory)) {
                int count = 0;
                boolean found = false;
                for (Pair<Unit, Pair<UnitType, TilePosition>> w : ((GameState) this.handler).workerBuild) { // TODO was bugged and checked workerTask 2 times, test
                    if (w.second.first == UnitType.Terran_Barracks) {
                        count++;
                    }
                    if (w.second.first == UnitType.Terran_Factory) {
                        found = true;
                    }
                }
                for (Pair<Unit, Unit> w : ((GameState) this.handler).workerTask) {
                    if (w.second.getType() == UnitType.Terran_Barracks) {
                        count++;
                    }
                    if (w.second.getType() == UnitType.Terran_Factory) {
                        found = true;
                    }
                }
                if (!((GameState) this.handler).Fs.isEmpty()) {
                    found = true;
                }
                if (count + ((GameState) this.handler).MBs.size() > ((GameState) this.handler).strat.numRaxForFac && !found) {
                    return State.FAILURE;
                }
            }
            if (((GameState) this.handler).countUnit(UnitType.Terran_Barracks) < ((GameState) this.handler).strat.raxPerCC * ((GameState) this.handler).CCs.size()) {
                ((GameState) this.handler).chosenToBuild = UnitType.Terran_Barracks;
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
