package ecgberht.BehaviourTrees.IslandExpansion;

import bwapi.TilePosition;
import bwapi.Unit;
import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;
import bwapi.Position;
import bwapi.UnitType;

public class CheckResourcesIsland extends Conditional {

    public CheckResourcesIsland(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            MutablePair<Integer, Integer> cash = gameState.getCash();
            Unit chosen = gameState.chosenWorkerDrop;
            UnitType chosenType = UnitType.Terran_Command_Center;
            TilePosition start = chosen.getTilePosition();
            TilePosition end = gameState.chosenIsland.getLocation();
            Position realEnd = Util.getUnitCenterPosition(end.toPosition(), chosenType);
            if (cash.first + gameState.getMineralsWhenReaching(start, realEnd.toTilePosition()) >= chosenType.mineralPrice() + gameState.deltaCash.first) {
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
