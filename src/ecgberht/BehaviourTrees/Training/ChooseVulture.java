package ecgberht.BehaviourTrees.Training;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Factory;


public class ChooseVulture extends BrainAction {

    public ChooseVulture(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (!gameState.tech.Fs.isEmpty()) {
                if (gameState.getStrat().name.equals("VultureRush") || Util.countUnitTypeSelf(UnitType.Terran_Vulture) * 2 <= Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Siege_Mode) + Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Tank_Mode) + 2) {
                    for (Factory b : gameState.tech.Fs) {
                        if (!b.isTraining() && b.canTrain(UnitType.Terran_Vulture)) {
                            gameState.chosenUnit = UnitType.Terran_Vulture;
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






