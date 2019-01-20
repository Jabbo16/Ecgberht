package ecgberht.BehaviourTrees.Build;

import bwapi.TilePosition;
import bwapi.Unit;
import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;
import bwapi.Position;
import bwapi.UnitType;

public class CheckResourcesBuilding extends Conditional {

    public CheckResourcesBuilding(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.chosenPosition == null) {
                gameState.chosenWorker = null;
                //((GameState) gameState).chosenToBuild = UnitType.None;
                return State.FAILURE;
            }
            MutablePair<Integer, Integer> cash = gameState.getCash();
            Unit chosen = gameState.chosenWorker;
            TilePosition start = chosen.getTilePosition();
            TilePosition end = gameState.chosenPosition;
            Position realEnd = Util.getUnitCenterPosition(end.toPosition(), gameState.chosenToBuild);
            if (gameState.getStrat().name.equals("ProxyBBS") && gameState.chosenToBuild == UnitType.Terran_Barracks) {
                if (Util.countBuildingAll(UnitType.Terran_Barracks) < 1) {
                    if (cash.first + gameState.getMineralsWhenReaching(start, realEnd.toTilePosition()) >= (gameState.chosenToBuild.mineralPrice() * 2 + 40 + gameState.deltaCash.first) && cash.second >= (gameState.chosenToBuild.gasPrice() * 2) + gameState.deltaCash.second) {
                        return State.SUCCESS;
                    }
                } else if (Util.countBuildingAll(UnitType.Terran_Barracks) == 1)
                    return State.SUCCESS;
                return State.FAILURE;
            } else if (cash.first + gameState.getMineralsWhenReaching(start, realEnd.toTilePosition()) >= (gameState.chosenToBuild.mineralPrice() + gameState.deltaCash.first) && cash.second >= (gameState.chosenToBuild.gasPrice()) + gameState.deltaCash.second) {
                return State.SUCCESS;
            }
            gameState.chosenWorker = null;
            gameState.chosenPosition = null;
            //((GameState) gameState).chosenToBuild = UnitType.None;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
