package ecgberht.BuildingLot;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.Worker;

import java.util.Map.Entry;

public class ChooseBlotWorker extends Action {

    public ChooseBlotWorker(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Worker closestWorker = null;
            Position chosen = ((GameState) this.handler).chosenBuildingLot.getPosition();
            if (!((GameState) this.handler).workerIdle.isEmpty()) {
                for (Worker u : ((GameState) this.handler).workerIdle) {
                    if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
                        closestWorker = u;
                    }
                }
            }
            if (!((GameState) this.handler).workerMining.isEmpty()) {
                for (Entry<Worker, MineralPatch> u : ((GameState) this.handler).workerMining.entrySet()) {
                    if ((closestWorker == null || u.getKey().getDistance(chosen) < closestWorker.getDistance(chosen)) && !u.getKey().isCarryingMinerals()) {
                        closestWorker = u.getKey();
                    }
                }
            }
            if (closestWorker != null) {
                ((GameState) this.handler).chosenWorker = closestWorker;
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
