package ecgberht.BehaviourTrees.IslandExpansion;

import bwapi.Unit;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.Position;

public class ChooseWorkerDrop extends Action {

    public ChooseWorkerDrop(String name, GameState gh) {
        super(name, gh);

    }

    @Override
    public State execute() {
        try {
            Unit closestWorker = null;
            int frame = gameState.frameCount;
            Position chosen = gameState.chosenDropShip.myUnit.getPosition();
            if (!gameState.workerIdle.isEmpty()) {
                for (Unit u : gameState.workerIdle) {
                    if (u.isCarryingMinerals()) continue;
                    if (u.getLastCommandFrame() == frame) continue;
                    if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
                        closestWorker = u;
                    }
                }
            }
            if (!gameState.workerMining.isEmpty()) {
                for (Unit u : gameState.workerMining.keySet()) {
                    if (u.isCarryingMinerals()) continue;
                    if (u.getLastCommandFrame() == frame) continue;
                    if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen)) && !u.isCarryingMinerals()) {
                        closestWorker = u;
                    }
                }
            }
            if (closestWorker != null) {
                gameState.chosenWorker = closestWorker;
                return State.SUCCESS;
            }
            gameState.chosenDropShip = null;
            gameState.chosenWorker = null;
            gameState.chosenIsland = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}