package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.*;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Worker;

public class CheckResourcesBuilding extends BrainAction {

    public CheckResourcesBuilding(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.chosenPosition == null) {
                gameState.chosenWorker = null;
                //((GameState) gameState).chosenToBuild = UnitType.None;
                return BrainStatus.FAILURE;
            }
            MutablePair<Integer, Integer> cash = gameState.getCash();
            Worker chosen = gameState.chosenWorker;
            TilePosition start = chosen.getTilePosition();
            TilePosition end = gameState.chosenPosition;
            Position realEnd = Util.getUnitCenterPosition(end.toPosition(), gameState.chosenToBuild);
            if (gameState.getStrat().name.equals("ProxyBBS") && gameState.chosenToBuild == UnitType.Terran_Barracks) {
                if (Util.countBuildingAll(UnitType.Terran_Barracks) < 1) {
                    if (cash.first + gameState.getMineralsWhenReaching(start, realEnd.toTilePosition()) >= (gameState.chosenToBuild.mineralPrice() * 2 + 40 + gameState.eco.deltaCash.first) && cash.second >= (gameState.chosenToBuild.gasPrice() * 2) + gameState.eco.deltaCash.second) {
                        return BrainStatus.SUCCESS;
                    }
                } else if (Util.countBuildingAll(UnitType.Terran_Barracks) == 1)
                    return BrainStatus.SUCCESS;
                return BrainStatus.FAILURE;
            } else if (cash.first + gameState.getMineralsWhenReaching(start, realEnd.toTilePosition()) >= (gameState.chosenToBuild.mineralPrice() + gameState.eco.deltaCash.first) && cash.second >= (gameState.chosenToBuild.gasPrice()) + gameState.eco.deltaCash.second) {
                return BrainStatus.SUCCESS;
            }
            gameState.chosenWorker = null;
            gameState.chosenPosition = null;
            //((GameState) gameState).chosenToBuild = UnitType.None;
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}






