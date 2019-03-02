package ecgberht.BehaviourTrees.Harass;

import ecgberht.GameState;
import ecgberht.UnitInfo;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;

import java.util.stream.Collectors;

public class ChooseBuildingToHarass extends Action {

    public ChooseBuildingToHarass(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.chosenUnitToHarass != null) return State.FAILURE;
            for (UnitInfo u : gameState.unitStorage.getEnemyUnits().values().stream().filter(u -> u.unitType.isBuilding()).collect(Collectors.toSet())) {
                if (gameState.enemyMainBase != null && gameState.bwem.getMap().getArea(u.tileposition).equals(gameState.bwem.getMap().getArea(gameState.enemyMainBase.getLocation()))) {
                    gameState.chosenUnitToHarass = u.unit;
                    return State.SUCCESS;
                }
            }
            if (gameState.chosenHarasser.isIdle()) {
                gameState.workerIdle.add(gameState.chosenHarasser);
                gameState.chosenHarasser.stop(false);
                gameState.chosenHarasser = null;
                gameState.chosenUnitToHarass = null;
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
