package ecgberht.MoveToBuild;

import bwapi.UnitType;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

public class ChoosePort extends Action {

    public ChoosePort(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {

            if (((GameState) this.handler).MBs.isEmpty() || ((GameState) this.handler).Fs.isEmpty() || ((GameState) this.handler).strat.numCCForPort > ((GameState) this.handler).CCs.size() ||
                    (((GameState) this.handler).countUnit(UnitType.Terran_Starport) > 0 && ((GameState) this.handler).strat.portPerCC == 0)) {
                return State.FAILURE;
            }
            if (((GameState) this.handler).countUnit(UnitType.Terran_Starport) == 0 && ((GameState) this.handler).strat.portPerCC == 0) {
                ((GameState) this.handler).chosenToBuild = UnitType.Terran_Starport;
                return State.SUCCESS;
            } else if (((GameState) this.handler).countUnit(UnitType.Terran_Starport) < ((GameState) this.handler).strat.portPerCC * ((GameState) this.handler).CCs.size()) {
                ((GameState) this.handler).chosenToBuild = UnitType.Terran_Starport;
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
