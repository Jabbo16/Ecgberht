package ecgberht.BehaviourTrees.IslandExpansion;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.Worker;

import java.util.Collections;
import java.util.TreeSet;

public class SendToDrop extends Action {

    public SendToDrop(String name, GameState gh) {
        super(name, gh);

    }

    @Override
    public State execute() {
        try {
            if (gameState.chosenDropShip != null && gameState.chosenWorker != null) {
                Worker chosen = gameState.chosenWorker;
                if (gameState.workerIdle.contains(chosen)) {
                    gameState.workerIdle.remove(chosen);
                } else if (gameState.workerMining.containsKey(chosen)) {
                    MineralPatch mineral = gameState.workerMining.get(chosen);
                    gameState.workerMining.remove(chosen);
                    if (gameState.mineralsAssigned.containsKey(mineral)) {
                        gameState.mining--;
                        gameState.mineralsAssigned.put(mineral, gameState.mineralsAssigned.get(mineral) - 1);
                    }
                }
                gameState.chosenDropShip.setCargo(new TreeSet<>(Collections.singletonList(gameState.chosenWorker)));
                gameState.chosenDropShip.setTarget(gameState.chosenIsland.getLocation().toPosition());
                gameState.chosenWorkerDrop = gameState.chosenWorker;
                gameState.chosenWorker = null;
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