package ecgberht.BehaviourTrees.BuildingLot;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.Worker;

import java.util.Map.Entry;

public class ChooseBlotWorker extends Action {

    public ChooseBlotWorker(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Worker closestWorker = null;
            Position chosen = this.handler.chosenBuildingLot.getPosition();
            if (!this.handler.workerIdle.isEmpty()) {
                for (Worker u : this.handler.workerIdle) {
                    if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
                        closestWorker = u;
                    }
                }
            }
            if (!this.handler.workerMining.isEmpty()) {
                for (Entry<Worker, MineralPatch> u : this.handler.workerMining.entrySet()) {
                    if ((closestWorker == null || u.getKey().getDistance(chosen) < closestWorker.getDistance(chosen)) && !u.getKey().isCarryingMinerals()) {
                        closestWorker = u.getKey();
                    }
                }
            }
            if (closestWorker != null) {
                this.handler.chosenWorker = closestWorker;
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
