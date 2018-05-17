package ecgberht.MoveToBuild;

import bwapi.UnitType;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

public class ChooseFactory extends Action {

    public ChooseFactory(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {

            if (((GameState) this.handler).MBs.isEmpty() || ((GameState) this.handler).strat.numRaxForFac > ((GameState) this.handler).countUnit(UnitType.Terran_Barracks) ||
                    (((GameState) this.handler).countUnit(UnitType.Terran_Factory) > 0 && ((GameState) this.handler).strat.facPerCC == 0)) {
                return State.FAILURE;
            }
            if (((GameState) this.handler).countUnit(UnitType.Terran_Factory) == 0 && ((GameState) this.handler).strat.facPerCC == 0) {
                ((GameState) this.handler).chosenToBuild = UnitType.Terran_Factory;
                return State.SUCCESS;
            } else if (((GameState) this.handler).countUnit(UnitType.Terran_Factory) < ((GameState) this.handler).strat.facPerCC * ((GameState) this.handler).CCs.size()) {
                ((GameState) this.handler).chosenToBuild = UnitType.Terran_Factory;
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
