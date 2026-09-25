package ecgberht.BehaviourTrees.Build;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Armory;
import org.openbw.bwapi4j.unit.Building;

public class ChooseArmory extends BrainAction {

    public ChooseArmory(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.Fs.isEmpty() || !gameState.getPlayer().hasResearched(TechType.Tank_Siege_Mode))
                return BrainStatus.FAILURE;
            if (gameState.Fs.size() < gameState.getStrat().facForArmory) return BrainStatus.FAILURE;
            if (Util.countUnitTypeSelf(UnitType.Terran_Armory) < gameState.getStrat().numArmories) {
                for (MutablePair<UnitType, TilePosition> w : gameState.workerBuild.values()) {
                    if (w.first == UnitType.Terran_Armory) return BrainStatus.FAILURE;
                }
                for (Building w : gameState.workerTask.values()) {
                    if (w instanceof Armory) return BrainStatus.FAILURE;
                }
                gameState.chosenToBuild = UnitType.Terran_Armory;
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

