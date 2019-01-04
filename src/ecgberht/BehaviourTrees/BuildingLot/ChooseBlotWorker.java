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
            Position chosen = gameState.chosenBuildingLot.getPosition();
            if (!gameState.workerIdle.isEmpty()) {
                for (Worker u : gameState.workerIdle) {
                    if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
                        closestWorker = u;
                    }
                }
            }
            if (!gameState.workerMining.isEmpty()) {
                for (Entry<Worker, MineralPatch> u : gameState.workerMining.entrySet()) {
                    if ((closestWorker == null || u.getKey().getDistance(chosen) < closestWorker.getDistance(chosen)) && !u.getKey().isCarryingMinerals()) {
                        closestWorker = u.getKey();
                    }
                }
            }
            if (closestWorker != null) {
                gameState.chosenWorker = closestWorker;
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
