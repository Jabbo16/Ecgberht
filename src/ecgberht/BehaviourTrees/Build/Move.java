package ecgberht.BehaviourTrees.Build;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.Worker;

public class Move extends BrainAction {

    public Move(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            Worker chosen = gameState.chosenWorker;
            Position realEnd = Util.getUnitCenterPosition(gameState.chosenPosition.toPosition(), gameState.chosenToBuild);
            if (chosen.move(realEnd)) {
                if (gameState.workerIdle.contains(chosen)) gameState.workerIdle.remove(chosen);
                else if (gameState.eco.workerMining.containsKey(chosen)) {
                    MineralPatch mineral = gameState.eco.workerMining.get(chosen);
                    gameState.eco.workerMining.remove(chosen);
                    if (gameState.eco.mineralsAssigned.containsKey(mineral)) {
                        gameState.eco.mining--;
                        gameState.eco.mineralsAssigned.put(mineral, gameState.eco.mineralsAssigned.get(mineral) - 1);
                    }
                }
                if (gameState.chosenToBuild == UnitType.Terran_Command_Center
                        && gameState.bwem.getMap().getArea(gameState.chosenPosition).equals(gameState.naturalArea)
                        && gameState.naturalChoke != null) {
                    gameState.mil.defendPosition = gameState.naturalChoke.getCenter().toPosition();
                }
                gameState.eco.workerBuild.put((SCV) chosen, new MutablePair<>(gameState.chosenToBuild, gameState.chosenPosition));
                gameState.eco.deltaCash.first += gameState.chosenToBuild.mineralPrice();
                gameState.eco.deltaCash.second += gameState.chosenToBuild.gasPrice();
                gameState.chosenWorker = null;
                gameState.chosenToBuild = UnitType.None;
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






