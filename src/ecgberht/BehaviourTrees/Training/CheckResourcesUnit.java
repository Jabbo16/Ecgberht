package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.*;
import org.openbw.bwapi4j.type.UnitType;

public class CheckResourcesUnit extends BrainAction {

    public CheckResourcesUnit(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            MutablePair<Integer, Integer> cash = gameState.getCash();
            if (cash.first >= (gameState.chosenUnit.mineralPrice() + gameState.eco.deltaCash.first) && cash.second >= (gameState.chosenUnit.gasPrice()) + gameState.eco.deltaCash.second) {
                return BrainStatus.SUCCESS;
            }
            gameState.chosenTrainingFacility = null;
            gameState.chosenToBuild = UnitType.None;
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}






