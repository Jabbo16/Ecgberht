package ecgberht.BehaviourTrees.Build;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.EngineeringBay;

public class ChooseBay extends BrainAction {

    public ChooseBay(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (!IntelligenceAgency.enemyHasAirOrCloakedThreats()) {
                if (gameState.getArmySize() < gameState.getStrat().armyForBay) return BrainStatus.FAILURE;
                if (gameState.getStrat().name.contains("BioMech") && gameState.CCs.size() < 2) return BrainStatus.FAILURE;
            }
            if (Util.countUnitTypeSelf(UnitType.Terran_Engineering_Bay) < gameState.getStrat().numBays) {
                for (MutablePair<UnitType, TilePosition> w : gameState.workerBuild.values()) {
                    if (w.first == UnitType.Terran_Engineering_Bay) return BrainStatus.FAILURE;
                }
                for (Building w : gameState.workerTask.values()) {
                    if (w instanceof EngineeringBay) return BrainStatus.FAILURE;
                }
                gameState.chosenToBuild = UnitType.Terran_Engineering_Bay;
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

