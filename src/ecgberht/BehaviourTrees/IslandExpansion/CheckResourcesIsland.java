package ecgberht.BehaviourTrees.IslandExpansion;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Conditional;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Worker;

public class CheckResourcesIsland extends Conditional {

    public CheckResourcesIsland(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            MutablePair<Integer, Integer> cash = this.handler.getCash();
            Worker chosen = this.handler.chosenWorkerDrop;
            UnitType chosenType = UnitType.Terran_Command_Center;
            TilePosition start = chosen.getTilePosition();
            TilePosition end = this.handler.chosenIsland.getLocation();
            Position realEnd = Util.getUnitCenterPosition(end.toPosition(), chosenType);
            if (cash.first + this.handler.getMineralsWhenReaching(start, realEnd.toTilePosition()) >= chosenType.mineralPrice() + this.handler.deltaCash.first) {
                return BehavioralTree.State.SUCCESS;
            }
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}
