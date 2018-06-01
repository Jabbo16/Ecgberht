package ecgberht.Build;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.UnitType;

public class ChooseScience extends Action {

    public ChooseScience(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {

            if (((GameState) this.handler).MBs.isEmpty() || ((GameState) this.handler).Fs.isEmpty() || ((GameState) this.handler).Ps.isEmpty() || ((GameState) this.handler).strat.numCCForScience > ((GameState) this.handler).CCs.size()) {
                return State.FAILURE;
            }
            if (((GameState) this.handler).countUnit(UnitType.Terran_Science_Facility) == 0) {
                ((GameState) this.handler).chosenToBuild = UnitType.Terran_Science_Facility;
                return State.SUCCESS;
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
