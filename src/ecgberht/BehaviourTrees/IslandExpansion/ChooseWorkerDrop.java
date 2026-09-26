package ecgberht.BehaviourTrees.IslandExpansion;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.Worker;

public class ChooseWorkerDrop extends BrainAction {

    public ChooseWorkerDrop(String name, GameState gh) {
        super(name, gh);

    }

    @Override
    public BrainStatus execute() {
        try {
            Worker closestWorker = null;
            int frame = gameState.frameCount;
            Position chosen = gameState.mil.chosenDropShip.unit.getPosition();
            if (!gameState.workerIdle.isEmpty()) {
                for (Worker u : gameState.workerIdle) {
                    if (u.isCarryingMinerals()) continue;
                    if (u.getLastCommandFrame() == frame) continue;
                    if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
                        closestWorker = u;
                    }
                }
            }
            if (!gameState.eco.workerMining.isEmpty()) {
                for (Worker u : gameState.eco.workerMining.keySet()) {
                    if (u.isCarryingMinerals()) continue;
                    if (u.getLastCommandFrame() == frame) continue;
                    if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen)) && !u.isCarryingMinerals()) {
                        closestWorker = u;
                    }
                }
            }
            if (closestWorker != null) {
                gameState.chosenWorker = closestWorker;
                return BrainStatus.SUCCESS;
            }
            gameState.mil.chosenDropShip = null;
            gameState.chosenWorker = null;
            gameState.chosenIsland = null;
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}





