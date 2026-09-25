package ecgberht.BehaviourTrees.IslandExpansion;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.*;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Worker;

public class CheckResourcesIsland extends BrainAction {

    public CheckResourcesIsland(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            MutablePair<Integer, Integer> cash = gameState.getCash();
            Worker chosen = gameState.chosenWorkerDrop;
            UnitType chosenType = UnitType.Terran_Command_Center;
            TilePosition start = chosen.getTilePosition();
            TilePosition end = gameState.chosenIsland.getLocation();
            Position realEnd = Util.getUnitCenterPosition(end.toPosition(), chosenType);
            if (cash.first + gameState.getMineralsWhenReaching(start, realEnd.toTilePosition()) >= chosenType.mineralPrice() + gameState.deltaCash.first) {
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

