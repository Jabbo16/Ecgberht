package ecgberht.BehaviourTrees.IslandExpansion;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.Worker;

public class MoveIsland extends BrainAction {

    public MoveIsland(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            Worker chosen = gameState.chosenWorkerDrop;
            UnitType chosenType = UnitType.Terran_Command_Center;
            TilePosition chosenTile = gameState.chosenIsland.getLocation();
            Position realEnd = Util.getUnitCenterPosition(chosenTile.toPosition(), chosenType);
            if (chosen.move(realEnd)) {
                gameState.workerBuild.put((SCV) chosen, new MutablePair<>(chosenType, chosenTile));
                gameState.deltaCash.first += chosenType.mineralPrice();
                gameState.deltaCash.second += chosenType.gasPrice();
                gameState.chosenWorkerDrop = null;
                gameState.chosenIsland = null;
                gameState.islandExpand = false;
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

