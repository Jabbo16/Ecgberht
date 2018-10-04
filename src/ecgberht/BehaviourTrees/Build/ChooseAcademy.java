package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Strategy;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Academy;
import org.openbw.bwapi4j.unit.Building;

public class ChooseAcademy extends Action {

    public ChooseAcademy(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (Util.countBuildingAll(UnitType.Terran_Refinery) == 0 || Util.countBuildingAll(UnitType.Terran_Academy) > 0) {
                return State.FAILURE;
            }
            Strategy strat = ((GameState) this.handler).strat;
            if ((strat.name.equals("FullMech") || strat.name.equals("MechGreedyFE"))
                    && Util.countBuildingAll(UnitType.Terran_Factory) >= strat.facPerCC) {
                ((GameState) this.handler).chosenToBuild = UnitType.Terran_Academy;
                return State.SUCCESS;
            }
            if (Util.countBuildingAll(UnitType.Terran_Barracks) >= ((GameState) this.handler).strat.numRaxForAca) {
                for (MutablePair<UnitType, TilePosition> w : ((GameState) this.handler).workerBuild.values()) {
                    if (w.first == UnitType.Terran_Academy) {
                        return State.FAILURE;
                    }
                }
                for (Building w : ((GameState) this.handler).workerTask.values()) {
                    if (w instanceof Academy) {
                        return State.FAILURE;
                    }
                }
                ((GameState) this.handler).chosenToBuild = UnitType.Terran_Academy;
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
