package ecgberht.BehaviourTrees.IslandExpansion;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Worker;

public class CheckResourcesIsland extends Conditional {

    public CheckResourcesIsland(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            MutablePair<Integer, Integer> cash = ((GameState) this.handler).getCash();
            Worker chosen = ((GameState) this.handler).chosenWorkerDrop;
            UnitType chosenType = UnitType.Terran_Command_Center;
            TilePosition start = chosen.getTilePosition();
            TilePosition end = ((GameState) this.handler).chosenIsland.getLocation();
            Position realEnd = ((GameState) this.handler).getCenterFromBuilding(end.toPosition(), chosenType);
            if (cash.first + ((GameState) this.handler).getMineralsWhenReaching(start, realEnd.toTilePosition()) >= chosenType.mineralPrice() + ((GameState) this.handler).deltaCash.first) {
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
