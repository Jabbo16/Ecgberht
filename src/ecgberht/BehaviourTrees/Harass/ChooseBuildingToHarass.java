package ecgberht.BehaviourTrees.Harass;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.UnitInfo;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;

import java.util.stream.Collectors;

public class ChooseBuildingToHarass extends BrainAction {

    public ChooseBuildingToHarass(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.chosenUnitToHarass != null) return BrainStatus.FAILURE;
            for (UnitInfo u : gameState.unitStorage.getEnemyUnits().values().stream().filter(u -> u.unitType.isBuilding()).collect(Collectors.toSet())) {
                if (gameState.enemyMainBase != null && gameState.bwem.getMap().getArea(u.tileposition).equals(gameState.bwem.getMap().getArea(gameState.enemyMainBase.getLocation()))) {
                    gameState.chosenUnitToHarass = u.unit;
                    return BrainStatus.SUCCESS;
                }
            }
            if (gameState.chosenHarasser.isIdle()) {
                gameState.workerIdle.add(gameState.chosenHarasser);
                gameState.chosenHarasser.stop(false);
                gameState.chosenHarasser = null;
                gameState.chosenUnitToHarass = null;
            }
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}






