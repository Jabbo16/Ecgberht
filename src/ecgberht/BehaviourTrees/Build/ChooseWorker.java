package ecgberht.BehaviourTrees.Build;

import bwapi.Unit;
import bwem.Area;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.Position;

public class ChooseWorker extends Action {

    public ChooseWorker(String name, GameState gh) {
        super(name, gh);

    }

    @Override
    public State execute() {
        try {
            Unit closestWorker = null;
            int frame = gameState.frameCount;
            Position chosen = gameState.chosenPosition.toPosition();
            Area posArea = gameState.bwem.getMap().getArea(gameState.chosenPosition);
            if (!gameState.workerIdle.isEmpty()) {
                for (Unit u : gameState.workerIdle) {
                    if (u.getLastCommandFrame() == frame) continue;
                    Area workerArea = gameState.bwem.getMap().getArea(u.getTilePosition());
                    if (workerArea == null) continue;
                    if (posArea != null && !posArea.equals(workerArea) && !posArea.isAccessibleFrom(workerArea)) {
                        continue;
                    }
                    if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen)))
                        closestWorker = u;
                }
            }
            if (!gameState.workerMining.isEmpty()) {
                for (Unit u : gameState.workerMining.keySet()) {
                    if (u.getLastCommandFrame() == frame) continue;
                    Area workerArea = gameState.bwem.getMap().getArea(u.getTilePosition());
                    if (workerArea == null) continue;
                    if (posArea != null && !posArea.equals(workerArea) && !posArea.isAccessibleFrom(workerArea)) {
                        continue;
                    }
                    if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen)) && !u.isCarryingMinerals()) {
                        closestWorker = u;
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