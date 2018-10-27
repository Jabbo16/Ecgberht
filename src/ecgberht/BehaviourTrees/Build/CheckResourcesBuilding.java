package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Conditional;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Worker;

public class CheckResourcesBuilding extends Conditional {

    public CheckResourcesBuilding(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            if (this.handler.chosenPosition == null) {
                this.handler.chosenWorker = null;
                //((GameState) this.handler).chosenToBuild = UnitType.None;
                return BehavioralTree.State.FAILURE;
            }
            MutablePair<Integer, Integer> cash = this.handler.getCash();
            Worker chosen = this.handler.chosenWorker;
            TilePosition start = chosen.getTilePosition();
            TilePosition end = this.handler.chosenPosition;
            Position realEnd = Util.getUnitCenterPosition(end.toPosition(), this.handler.chosenToBuild);
            if (this.handler.strat.name.equals("ProxyBBS") && this.handler.chosenToBuild == UnitType.Terran_Barracks) {
                if (Util.countBuildingAll(UnitType.Terran_Barracks) < 1) {
                    if (cash.first + this.handler.getMineralsWhenReaching(start, realEnd.toTilePosition()) >= (this.handler.chosenToBuild.mineralPrice() * 2 + 40 + this.handler.deltaCash.first) && cash.second >= (this.handler.chosenToBuild.gasPrice() * 2) + this.handler.deltaCash.second) {
                        return BehavioralTree.State.SUCCESS;
                    }
                } else if (Util.countBuildingAll(UnitType.Terran_Barracks) == 1)
                    return BehavioralTree.State.SUCCESS;
                return BehavioralTree.State.FAILURE;
            } else if (cash.first + this.handler.getMineralsWhenReaching(start, realEnd.toTilePosition()) >= (this.handler.chosenToBuild.mineralPrice() + this.handler.deltaCash.first) && cash.second >= (this.handler.chosenToBuild.gasPrice()) + this.handler.deltaCash.second) {
                return BehavioralTree.State.SUCCESS;
            }
            this.handler.chosenWorker = null;
            this.handler.chosenPosition = null;
            //((GameState) this.handler).chosenToBuild = UnitType.None;
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}
