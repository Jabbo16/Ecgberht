package ecgberht.BehaviourTrees.IslandExpansion;

import bwapi.TilePosition;
import bwapi.Unit;
import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.Position;
import bwapi.UnitType;

public class MoveIsland extends Action {

    public MoveIsland(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Unit chosen = gameState.chosenWorkerDrop;
            UnitType chosenType = UnitType.Terran_Command_Center;
            TilePosition chosenTile = gameState.chosenIsland.getLocation();
            Position realEnd = Util.getUnitCenterPosition(chosenTile.toPosition(), chosenType);
            if (chosen.move(realEnd)) {
                gameState.workerBuild.put(chosen, new MutablePair<>(chosenType, chosenTile));
                gameState.deltaCash.first += chosenType.mineralPrice();
                gameState.deltaCash.second += chosenType.gasPrice();
                gameState.chosenWorkerDrop = null;
                gameState.chosenIsland = null;
                gameState.islandExpand = false;
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
