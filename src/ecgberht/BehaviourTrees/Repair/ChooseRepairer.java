package ecgberht.BehaviourTrees.Repair;


import bwapi.Unit;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.Position;

public class ChooseRepairer extends Action {

    public ChooseRepairer(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Unit closestWorker = null;
            Position chosen = gameState.chosenUnitRepair.getPosition();
            int frame = gameState.frameCount;
            for (Unit u : gameState.workerIdle) {
                if (u.getLastCommandFrame() == frame) continue;
                if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
                    closestWorker = u;
                }
            }
            for (Unit u : gameState.workerMining.keySet()) {
                if (u.getLastCommandFrame() == frame) continue;
                if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen)) && !u.isCarryingMinerals()) {
                    closestWorker = u;
                }
            }
            if (closestWorker != null) {
                gameState.chosenRepairer = closestWorker;
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