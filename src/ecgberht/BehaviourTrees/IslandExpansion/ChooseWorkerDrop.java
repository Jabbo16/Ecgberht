package ecgberht.BehaviourTrees.IslandExpansion;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.Worker;

public class ChooseWorkerDrop extends Action {

    public ChooseWorkerDrop(String name, GameState gh) {
        super(name, gh);

    }

    @Override
    public BehavioralTree.State execute() {
        try {
            Worker closestWorker = null;
            int frame = this.handler.frameCount;
            Position chosen = this.handler.chosenDropShip.unit.getPosition();
            if (!this.handler.workerIdle.isEmpty()) {
                for (Worker u : this.handler.workerIdle) {
                    if (u.isCarryingMinerals()) continue;
                    if (u.getLastCommandFrame() == frame) continue;
                    if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
                        closestWorker = u;
                    }
                }
            }
            if (!this.handler.workerMining.isEmpty()) {
                for (Worker u : this.handler.workerMining.keySet()) {
                    if (u.isCarryingMinerals()) continue;
                    if (u.getLastCommandFrame() == frame) continue;
                    if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen)) && !u.isCarryingMinerals()) {
                        closestWorker = u;
                    }
                }
            }
            if (closestWorker != null) {
                this.handler.chosenWorker = closestWorker;
                return BehavioralTree.State.SUCCESS;
            }
            this.handler.chosenDropShip = null;
            this.handler.chosenWorker = null;
            this.handler.chosenIsland = null;
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}