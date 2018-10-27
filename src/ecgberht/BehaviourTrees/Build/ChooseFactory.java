package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;

public class ChooseFactory extends Action {

    public ChooseFactory(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            String strat = this.handler.strat.name;
            if (strat.equals("FullMech") || strat.equals("MechGreedyFE")) {
                Player self = this.handler.getPlayer();
                if (Util.countBuildingAll(UnitType.Terran_Factory) > 1 &&
                        !self.isResearching(TechType.Tank_Siege_Mode) && !self.hasResearched(TechType.Tank_Siege_Mode)) {
                    return State.FAILURE;
                }
            }
            if (this.handler.MBs.isEmpty() || this.handler.strat.numRaxForFac > Util.countBuildingAll(UnitType.Terran_Barracks) ||
                    (Util.countBuildingAll(UnitType.Terran_Factory) > 0 && this.handler.strat.facPerCC == 0)) {
                return State.FAILURE;
            }
            if (Util.countBuildingAll(UnitType.Terran_Factory) == 0 && this.handler.strat.facPerCC == 0) {
                this.handler.chosenToBuild = UnitType.Terran_Factory;
                return State.SUCCESS;
            } else if (Util.countBuildingAll(UnitType.Terran_Factory) < this.handler.strat.facPerCC * Util.getNumberCCs()) {
                this.handler.chosenToBuild = UnitType.Terran_Factory;
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
