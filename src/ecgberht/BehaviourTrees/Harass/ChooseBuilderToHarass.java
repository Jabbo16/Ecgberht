package ecgberht.BehaviourTrees.Harass;

import bwapi.Race;
import bwapi.UnitType;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.Unit;

public class ChooseBuilderToHarass extends Action {

    public ChooseBuilderToHarass(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.enemyRace != Race.Terran) return State.FAILURE;
            for (Unit u : gameState.enemyCombatUnitMemory) {
                Unit aux = null;
                if (gameState.enemyMainBase != null && u.getType() == UnitType.Terran_SCV && u.isConstructing()) {
                    if (gameState.bwem.getMap().getArea(u.getTilePosition()).equals(gameState.bwem.getMap().getArea(gameState.enemyMainBase.getLocation()))) {
                        if (u.getBuildType().canProduce()) {
                            gameState.chosenUnitToHarass = u;
                            return State.SUCCESS;
                        }
                        aux = u;
                    }
                    if (aux != null) {
                        gameState.chosenUnitToHarass = aux;
                        return State.SUCCESS;
                    }
                }
            }
            gameState.chosenUnitToHarass = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
