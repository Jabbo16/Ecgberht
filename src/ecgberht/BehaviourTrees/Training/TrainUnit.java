package ecgberht.BehaviourTrees.Training;

import bwapi.TilePosition;
import bwapi.Unit;
import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.UnitType;

public class TrainUnit extends Action {

    public TrainUnit(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.chosenUnit == UnitType.None) return State.FAILURE;
            Unit chosen = gameState.chosenBuilding;
            if (gameState.getStrat().name.equals("ProxyBBS")) {
                if (Util.countBuildingAll(UnitType.Terran_Barracks) == 2 &&
                        Util.countBuildingAll(UnitType.Terran_Supply_Depot) == 0) {
                    gameState.chosenBuilding = null;
                    gameState.chosenToBuild = UnitType.None;
                    return State.FAILURE;
                }
                if (gameState.getSupply() > 0) {
                    chosen.train(gameState.chosenUnit);
                    return State.SUCCESS;
                }
            }
            if (gameState.getStrat().name.equals("ProxyEightRax")) {
                if (Util.countBuildingAll(UnitType.Terran_Barracks) == 0 &&
                        gameState.supplyMan.getSupplyUsed() >= 16) {
                    gameState.chosenBuilding = null;
                    gameState.chosenToBuild = UnitType.None;
                    return State.FAILURE;
                }
                if (gameState.getSupply() > 0) {
                    chosen.train(gameState.chosenUnit);
                    return State.SUCCESS;
                }
            }
            if (gameState.getSupply() > 4 || gameState.checkSupply() || gameState.getPlayer().supplyTotal() >= 400) {
                if (!gameState.defense && gameState.chosenToBuild == UnitType.Terran_Command_Center) {
                    boolean found = false;
                    for (MutablePair<UnitType, TilePosition> w : gameState.workerBuild.values()) {
                        if (w.first == UnitType.Terran_Command_Center) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        gameState.chosenBuilding = null;
                        gameState.chosenUnit = UnitType.None;
                        return State.FAILURE;
                    }
                }
                chosen.train(gameState.chosenUnit);
                return State.SUCCESS;
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
