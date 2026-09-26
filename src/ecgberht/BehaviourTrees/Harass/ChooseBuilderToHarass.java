package ecgberht.BehaviourTrees.Harass;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.Unit;

public class ChooseBuilderToHarass extends BrainAction {

    public ChooseBuilderToHarass(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.enemyRace != Race.Terran) return BrainStatus.FAILURE;
            for (Unit u : gameState.enemyCombatUnitMemory) {
                Unit aux = null;
                if (gameState.enemyMainBase != null && u instanceof SCV && ((SCV) u).isConstructing()) {
                    if (gameState.bwem.getMap().getArea(u.getTilePosition()).equals(gameState.bwem.getMap().getArea(gameState.enemyMainBase.getLocation()))) {
                        if (((SCV) u).getBuildType().canProduce()) {
                            gameState.chosenUnitToHarass = u;
                            return BrainStatus.SUCCESS;
                        }
                        aux = u;
                    }
                    if (aux != null) {
                        gameState.chosenUnitToHarass = aux;
                        return BrainStatus.SUCCESS;
                    }
                }
            }
            gameState.chosenUnitToHarass = null;
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}






