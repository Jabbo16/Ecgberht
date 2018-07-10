package ecgberht.Build;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.UnitType;

public class ChooseBunker extends Action {

    public ChooseBunker(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).getGame().getBWMap().mapHash().equals("6f5295624a7e3887470f3f2e14727b1411321a67")) {
                return State.FAILURE;
            }
            if (((GameState) this.handler).MBs.size() >= 1 && ((GameState) this.handler).countUnit(UnitType.Terran_Bunker) == 0) {
                ((GameState) this.handler).chosenToBuild = UnitType.Terran_Bunker;
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
