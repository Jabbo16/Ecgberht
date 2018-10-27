package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.UnitType;

public class ChooseScience extends Action {

    public ChooseScience(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {

            if (this.handler.MBs.isEmpty() || this.handler.Fs.isEmpty() || this.handler.Ps.isEmpty() || this.handler.strat.numCCForScience > Util.getNumberCCs()) {
                return BehavioralTree.State.FAILURE;
            }
            if (Util.countBuildingAll(UnitType.Terran_Science_Facility) == 0) {
                this.handler.chosenToBuild = UnitType.Terran_Science_Facility;
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
