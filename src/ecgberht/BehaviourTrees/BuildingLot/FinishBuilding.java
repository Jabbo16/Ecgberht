package ecgberht.BehaviourTrees.BuildingLot;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.Worker;

public class FinishBuilding extends Action {

    public FinishBuilding(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Worker chosen = gameState.chosenWorker;
            if (chosen.rightClick(gameState.chosenBuildingLot, false)) {
                if (gameState.workerIdle.contains(chosen)) gameState.workerIdle.remove(chosen);
                else if (gameState.workerMining.containsKey(chosen)) {
                    MineralPatch mineral = gameState.workerMining.get(chosen);
                    gameState.workerMining.remove(chosen);
                    if (gameState.mineralsAssigned.containsKey(mineral)) {
                        gameState.mining--;
                        gameState.mineralsAssigned.put(mineral, gameState.mineralsAssigned.get(mineral) - 1);
                    }
                }
                gameState.workerTask.put((SCV) chosen, gameState.chosenBuildingLot);
                gameState.chosenWorker = null;
                gameState.buildingLot.remove(gameState.chosenBuildingLot);
                gameState.chosenBuildingLot = null;
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
