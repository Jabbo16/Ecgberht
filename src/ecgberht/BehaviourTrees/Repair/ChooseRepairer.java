package ecgberht.BehaviourTrees.Repair;


import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.Worker;

public class ChooseRepairer extends Action {

    public ChooseRepairer(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            SCV closestWorker = null;
            Position chosen = ((GameState) this.handler).chosenBuildingRepair.getPosition();
            int frame = ((GameState) this.handler).frameCount;
            for (Worker u : ((GameState) this.handler).workerIdle) {
                if (u.getLastCommandFrame() == frame) continue;
                if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
                    closestWorker = (SCV) u;
                }
            }
            for (Worker u : ((GameState) this.handler).workerMining.keySet()) {
                if (u.getLastCommandFrame() == frame) continue;
                if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen)) && !u.isCarryingMinerals()) {
                    closestWorker = (SCV) u;
                }
            }
            if (closestWorker != null) {
                ((GameState) this.handler).chosenRepairer = closestWorker;
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