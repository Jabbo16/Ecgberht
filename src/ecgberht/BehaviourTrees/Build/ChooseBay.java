package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.EngineeringBay;

public class ChooseBay extends Action {

    public ChooseBay(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            if (this.handler.getArmySize() < this.handler.strat.armyForBay) {
                return BehavioralTree.State.FAILURE;
            }
            if (Util.countUnitTypeSelf(UnitType.Terran_Engineering_Bay) < this.handler.strat.numBays) {
                for (MutablePair<UnitType, TilePosition> w : this.handler.workerBuild.values()) {
                    if (w.first == UnitType.Terran_Engineering_Bay) {
                        return BehavioralTree.State.FAILURE;
                    }
                }
                for (Building w : this.handler.workerTask.values()) {
                    if (w instanceof EngineeringBay) {
                        return BehavioralTree.State.FAILURE;
                    }
                }
                this.handler.chosenToBuild = UnitType.Terran_Engineering_Bay;
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
