package ecgberht.BehaviourTrees.Training;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.TrainingFacility;

public class TrainUnit extends BrainAction {

    public TrainUnit(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.chosenUnit == UnitType.None) return BrainStatus.FAILURE;
            TrainingFacility chosen = gameState.chosenTrainingFacility;
            if (gameState.getStrat().name.equals("ProxyBBS")) {
                if (Util.countBuildingAll(UnitType.Terran_Barracks) == 2 &&
                        Util.countBuildingAll(UnitType.Terran_Supply_Depot) == 0) {
                    gameState.chosenTrainingFacility = null;
                    gameState.chosenToBuild = UnitType.None;
                    return BrainStatus.FAILURE;
                }
                if (gameState.getSupply() > 0) {
                    chosen.train(gameState.chosenUnit);
                    return BrainStatus.SUCCESS;
                }
            }
            if (gameState.getStrat().name.equals("ProxyEightRax")) {
                if (Util.countBuildingAll(UnitType.Terran_Barracks) == 0 &&
                        gameState.supplyMan.getSupplyUsed() >= 16) {
                    gameState.chosenTrainingFacility = null;
                    gameState.chosenToBuild = UnitType.None;
                    return BrainStatus.FAILURE;
                }
                if (gameState.getSupply() > 0) {
                    chosen.train(gameState.chosenUnit);
                    return BrainStatus.SUCCESS;
                }
            }
            if (gameState.getSupply() > 4 || gameState.checkSupply() || gameState.getPlayer().supplyTotal() >= 400) {
                if (!gameState.mil.defense && gameState.chosenToBuild == UnitType.Terran_Command_Center) {
                    boolean found = false;
                    for (MutablePair<UnitType, TilePosition> w : gameState.eco.workerBuild.values()) {
                        if (w.first == UnitType.Terran_Command_Center) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        gameState.chosenTrainingFacility = null;
                        gameState.chosenUnit = UnitType.None;
                        return BrainStatus.FAILURE;
                    }
                }
                chosen.train(gameState.chosenUnit);
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






