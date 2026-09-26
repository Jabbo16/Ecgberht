package ecgberht.BehaviourTrees.IslandExpansion;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.Worker;

import java.util.Collections;
import java.util.TreeSet;

public class SendToDrop extends BrainAction {

    public SendToDrop(String name, GameState gh) {
        super(name, gh);

    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.mil.chosenDropShip != null && gameState.chosenWorker != null) {
                Worker chosen = gameState.chosenWorker;
                if (gameState.workerIdle.contains(chosen)) {
                    gameState.workerIdle.remove(chosen);
                } else if (gameState.eco.workerMining.containsKey(chosen)) {
                    MineralPatch mineral = gameState.eco.workerMining.get(chosen);
                    gameState.eco.workerMining.remove(chosen);
                    if (gameState.eco.mineralsAssigned.containsKey(mineral)) {
                        gameState.eco.mining--;
                        gameState.eco.mineralsAssigned.put(mineral, gameState.eco.mineralsAssigned.get(mineral) - 1);
                    }
                }
                gameState.mil.chosenDropShip.setCargo(new TreeSet<>(Collections.singletonList(gameState.chosenWorker)));
                gameState.mil.chosenDropShip.setTarget(gameState.chosenIsland.getLocation().toPosition());
                gameState.chosenWorkerDrop = gameState.chosenWorker;
                gameState.chosenWorker = null;
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





