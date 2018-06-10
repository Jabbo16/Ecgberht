package ecgberht.Scouting;

import bwapi.Unit;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

public class ChooseScout extends Action {

    public ChooseScout(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            for (Unit u : ((GameState) this.handler).workerIdle) {
                ((GameState) this.handler).chosenScout = u;
                ((GameState) this.handler).workerIdle.remove(u);
                break;
            }
            if (((GameState) this.handler).chosenScout == null) {
                for (Unit u : ((GameState) this.handler).workerMining.keySet()) {
                    if (!u.isCarryingMinerals()) {
                        ((GameState) this.handler).chosenScout = u;
                        ((GameState) this.handler).workerMining.remove(u);
                    }
                    break;
                }
            }
            if (((GameState) this.handler).chosenScout != null) {
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
