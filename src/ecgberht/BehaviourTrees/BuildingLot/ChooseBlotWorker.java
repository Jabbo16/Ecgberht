package ecgberht.BehaviourTrees.BuildingLot;

import bwapi.Unit;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.Position;

import java.util.Map.Entry;

public class ChooseBlotWorker extends Action {

    public ChooseBlotWorker(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Unit closestWorker = null;
            Position chosen = gameState.chosenBuildingLot.getPosition();
            if (!gameState.workerIdle.isEmpty()) {
                for (Unit u : gameState.workerIdle) {
                    if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
                        closestWorker = u;
                    }
                }
            }
            if (!gameState.workerMining.isEmpty()) {
                for (Entry<Unit, Unit> u : gameState.workerMining.entrySet()) {
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
