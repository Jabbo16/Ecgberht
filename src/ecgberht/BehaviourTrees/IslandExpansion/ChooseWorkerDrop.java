package ecgberht.BehaviourTrees.IslandExpansion;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.Worker;

public class ChooseWorkerDrop extends Action {

    public ChooseWorkerDrop(String name, GameHandler gh) {
        super(name, gh);

    }

    @Override
    public State execute() {
        try {
            Worker closestWorker = null;
            int frame = ((GameState) this.handler).frameCount;
            Position chosen = ((GameState) this.handler).chosenDropShip.unit.getPosition();
            if (!((GameState) this.handler).workerIdle.isEmpty()) {
                for (Worker u : ((GameState) this.handler).workerIdle) {
                    if (u.isCarryingMinerals()) continue;
                    if (u.getLastCommandFrame() == frame) continue;
                    if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
                        closestWorker = u;
                    }
                }
            }
            if (!((GameState) this.handler).workerMining.isEmpty()) {
                for (Worker u : ((GameState) this.handler).workerMining.keySet()) {
                    if (u.isCarryingMinerals()) continue;
                    if (u.getLastCommandFrame() == frame) continue;
                    if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen)) && !u.isCarryingMinerals()) {
                        closestWorker = u;
                    }
                }
            }
            if (closestWorker != null) {
                ((GameState) this.handler).chosenWorker = closestWorker;
                return State.SUCCESS;
            }
            ((GameState) this.handler).chosenDropShip = null;
            ((GameState) this.handler).chosenWorker = null;
            ((GameState) this.handler).chosenIsland = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}