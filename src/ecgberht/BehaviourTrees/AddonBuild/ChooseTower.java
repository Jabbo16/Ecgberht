package ecgberht.BehaviourTrees.AddonBuild;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Starport;

public class ChooseTower extends Action {

    public ChooseTower(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            for (Starport c : this.handler.Ps) {
                if (!c.isTraining() && c.getAddon() == null) {
                    this.handler.chosenBuildingAddon = c;
                    this.handler.chosenAddon = UnitType.Terran_Control_Tower;
                    return BehavioralTree.State.SUCCESS;
                }
            }
            this.handler.chosenBuildingAddon = null;
            this.handler.chosenAddon = null;
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}
