package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Strategy;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Academy;
import org.openbw.bwapi4j.unit.Building;

public class ChooseAcademy extends Action {

    public ChooseAcademy(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            if (Util.countBuildingAll(UnitType.Terran_Refinery) == 0 || Util.countBuildingAll(UnitType.Terran_Academy) > 0) {
                return BehavioralTree.State.FAILURE;
            }
            Strategy strat = this.handler.strat;
            if ((strat.name.equals("FullMech") || strat.name.equals("MechGreedyFE"))
                    && Util.countBuildingAll(UnitType.Terran_Factory) >= strat.facPerCC) {
                this.handler.chosenToBuild = UnitType.Terran_Academy;
                return BehavioralTree.State.SUCCESS;
            }
            if (Util.countBuildingAll(UnitType.Terran_Barracks) >= this.handler.strat.numRaxForAca) {
                for (MutablePair<UnitType, TilePosition> w : this.handler.workerBuild.values()) {
                    if (w.first == UnitType.Terran_Academy) {
                        return BehavioralTree.State.FAILURE;
                    }
                }
                for (Building w : this.handler.workerTask.values()) {
                    if (w instanceof Academy) {
                        return BehavioralTree.State.FAILURE;
                    }
                }
                this.handler.chosenToBuild = UnitType.Terran_Academy;
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
