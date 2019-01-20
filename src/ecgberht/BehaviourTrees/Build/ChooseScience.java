package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.UnitType;

public class ChooseScience extends Action {

    public ChooseScience(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {

            if (gameState.MBs.isEmpty() || gameState.Fs.isEmpty() || gameState.Ps.isEmpty() || gameState.getStrat().numCCForScience > Util.getNumberCCs()) {
                return State.FAILURE;
            }
            if (Util.countBuildingAll(UnitType.Terran_Science_Facility) == 0) {
                gameState.chosenToBuild = UnitType.Terran_Science_Facility;
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
