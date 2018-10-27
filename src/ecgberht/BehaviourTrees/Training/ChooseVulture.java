package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Factory;


public class ChooseVulture extends Action {

    public ChooseVulture(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            if (!this.handler.Fs.isEmpty()) {
                if (Util.countUnitTypeSelf(UnitType.Terran_Vulture) * 2 <= Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Siege_Mode) + Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Tank_Mode) + 2) {
                    for (Factory b : this.handler.Fs) {
                        if (!b.isTraining() && b.canTrain(UnitType.Terran_Vulture)) {
                            this.handler.chosenUnit = UnitType.Terran_Vulture;
                            this.handler.chosenBuilding = b;
                            return BehavioralTree.State.SUCCESS;
                        }
                    }
                }
            }
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}
