package ecgberht.BehaviourTrees.tech.buildingLot;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.Worker;

public class FinishBuilding extends BrainAction {

    public FinishBuilding(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            Worker chosen = gameState.chosenWorker;
            if (chosen.rightClick(gameState.tech.chosenBuildingLot, false)) {
                if (gameState.workerIdle.contains(chosen)) gameState.workerIdle.remove(chosen);
                else if (gameState.eco.workerMining.containsKey(chosen)) {
                    MineralPatch mineral = gameState.eco.workerMining.get(chosen);
                    gameState.eco.workerMining.remove(chosen);
                    if (gameState.eco.mineralsAssigned.containsKey(mineral)) {
                        gameState.eco.mining--;
                        gameState.eco.mineralsAssigned.put(mineral, gameState.eco.mineralsAssigned.get(mineral) - 1);
                    }
                }
                gameState.eco.workerTask.put((SCV) chosen, gameState.tech.chosenBuildingLot);
                gameState.chosenWorker = null;
                gameState.tech.buildingLot.remove(gameState.tech.chosenBuildingLot);
                gameState.tech.chosenBuildingLot = null;
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






