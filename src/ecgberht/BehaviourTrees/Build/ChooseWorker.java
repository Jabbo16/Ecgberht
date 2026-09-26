package ecgberht.BehaviourTrees.Build;
import ecgberht.brain.*;

import bwem.Area;
import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.Worker;

public class ChooseWorker extends BrainAction {

    public ChooseWorker(String name, GameState gh) {
        super(name, gh);

    }

    @Override
    public BrainStatus execute() {
        try {
            Worker closestWorker = null;
            int frame = gameState.frameCount;
            Position chosen = gameState.chosenPosition.toPosition();
            Area posArea = gameState.bwem.getMap().getArea(gameState.chosenPosition);
            if (!gameState.workerIdle.isEmpty()) {
                for (Worker u : gameState.workerIdle) {
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
            if (!gameState.eco.workerMining.isEmpty()) {
                for (Worker u : gameState.eco.workerMining.keySet()) {
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
                return BrainStatus.SUCCESS;
            }
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}





