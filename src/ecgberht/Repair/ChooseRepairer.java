package ecgberht.Repair;


import bwapi.Position;
import bwapi.Unit;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

public class ChooseRepairer extends Action {

    public ChooseRepairer(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Unit closestWorker = null;
            Position chosen = ((GameState) this.handler).chosenBuildingRepair.getPosition();
            int frame = ((GameState) this.handler).frameCount;
            for (Unit u : ((GameState) this.handler).workerIdle) {
                if (u.getLastCommandFrame() == frame) {
                    continue;
                }
                if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
                    closestWorker = u;
                }
            }
            for (Unit u : ((GameState) this.handler).workerMining.keySet()) {
                if (u.getLastCommandFrame() == frame) {
                    continue;
                }
                if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen)) && !u.isCarryingMinerals()) {
                    closestWorker = u;
                }
            }
            if (closestWorker != null) {
                ((GameState) this.handler).chosenRepairer = closestWorker;
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