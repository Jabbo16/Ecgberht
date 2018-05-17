package ecgberht.Scouting;

import bwem.Base;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;

public class CheckVisibleBase extends Conditional {

    public CheckVisibleBase(String name, GameHandler gh) {
        super(name, gh);

    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).chosenScout == null) {
                return State.FAILURE;
            }
            if (!((GameState) this.handler).ScoutSLs.isEmpty()) {
                for (Base b : ((GameState) this.handler).ScoutSLs) {
                    if ((((GameState) this.handler).getGame().getBWMap().isVisible(b.getLocation()))) {
                        ((GameState) this.handler).ScoutSLs.remove(b);
                        return State.SUCCESS;
                    }
                }
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            System.err.println(e);
            return State.ERROR;
        }
    }
}
