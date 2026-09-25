package ecgberht.BehaviourTrees.Training;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Starport;


public class ChooseWraith extends BrainAction {

    public ChooseWraith(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (!gameState.Ps.isEmpty()) {
                if (Util.countUnitTypeSelf(UnitType.Terran_Wraith) <= gameState.maxWraiths) {
                    for (Starport b : gameState.Ps) {
                        if (!b.isTraining() && b.canTrain(UnitType.Terran_Wraith)) {
                            gameState.chosenUnit = UnitType.Terran_Wraith;
                            gameState.chosenTrainingFacility = b;
                            return BrainStatus.SUCCESS;
                        }
                    }
                }
            }
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}

