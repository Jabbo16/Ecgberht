package ecgberht.BehaviourTrees.Build;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.UnitType;

public class ChooseScience extends BrainAction {

    public ChooseScience(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {

            if (gameState.MBs.isEmpty() || gameState.Fs.isEmpty() || gameState.Ps.isEmpty() || gameState.getStrat().numCCForScience > Util.getNumberCCs()) {
                return BrainStatus.FAILURE;
            }
            if (Util.countBuildingAll(UnitType.Terran_Science_Facility) == 0) {
                gameState.chosenToBuild = UnitType.Terran_Science_Facility;
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

