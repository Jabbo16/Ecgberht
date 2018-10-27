package ecgberht.BehaviourTrees.Recollection;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.unit.Worker;

public class FreeWorker extends Action {

    public FreeWorker(String name, GameState gh) {
        super(name, gh);

    }

    @Override
    public BehavioralTree.State execute() {
        try {
            this.handler.chosenWorker = null;
            if (!this.handler.workerIdle.isEmpty()) {
                int frame = this.handler.frameCount;
                for (Worker w : this.handler.workerIdle) {
                    if (w.getLastCommandFrame() != frame) {
                        this.handler.chosenWorker = w;
                        return BehavioralTree.State.SUCCESS;
                    }
                }
            }
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}