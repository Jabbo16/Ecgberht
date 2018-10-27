package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.Worker;

public class ChooseWorker extends Action {

    public ChooseWorker(String name, GameState gh) {
        super(name, gh);

    }

    @Override
    public BehavioralTree.State execute() {
        try {
            Worker closestWorker = null;
            int frame = this.handler.frameCount;
            Position chosen = this.handler.chosenPosition.toPosition();
            if (!this.handler.workerIdle.isEmpty()) {
                for (Worker u : this.handler.workerIdle) {
                    if (u.getLastCommandFrame() == frame) {
                        continue;
                    }
                    if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
                        closestWorker = u;
                    }
                }
            }
            if (!this.handler.workerMining.isEmpty()) {
                for (Worker u : this.handler.workerMining.keySet()) {
                    if (u.getLastCommandFrame() == frame) {
                        continue;
                    }
                    if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen)) && !u.isCarryingMinerals()) {
                        closestWorker = u;
                    }
                }
            }
            if (closestWorker != null) {
                this.handler.chosenWorker = closestWorker;
                return BehavioralTree.State.SUCCESS;
            }
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}