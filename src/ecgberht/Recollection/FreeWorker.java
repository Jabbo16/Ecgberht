package ecgberht.Recollection;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.unit.Worker;

public class FreeWorker extends Action {

    public FreeWorker(String name, GameHandler gh) {
        super(name, gh);

    }

    @Override
    public State execute() {
        try {
            if (!((GameState) this.handler).workerIdle.isEmpty()) {
                int frame = ((GameState) this.handler).frameCount;
                for (Worker w : ((GameState) this.handler).workerIdle) {
                    if (w.isIdle() && w.getLastCommandFrame() != frame) {
                        ((GameState) this.handler).chosenWorker = w;
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