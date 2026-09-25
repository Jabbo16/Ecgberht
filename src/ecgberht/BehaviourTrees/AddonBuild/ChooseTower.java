package ecgberht.BehaviourTrees.AddonBuild;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Starport;

public class ChooseTower extends BrainAction {

    public ChooseTower(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            for (Starport c : gameState.Ps) {
                if (!c.isTraining() && c.getAddon() == null) {
                    gameState.chosenBuildingAddon = c;
                    gameState.chosenAddon = UnitType.Terran_Control_Tower;
                    return BrainStatus.SUCCESS;
                }
            }
            gameState.chosenBuildingAddon = null;
            gameState.chosenAddon = null;
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}

