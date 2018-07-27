package ecgberht.BehaviourTrees.AddonBuild;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Starport;

public class ChooseTower extends Action {

    public ChooseTower(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            for (Starport c : ((GameState) this.handler).Ps) {
                if (!c.isTraining() && c.getAddon() == null) {
                    ((GameState) this.handler).chosenBuildingAddon = c;
                    ((GameState) this.handler).chosenAddon = UnitType.Terran_Control_Tower;
                    return State.SUCCESS;
                }
            }
            ((GameState) this.handler).chosenBuildingAddon = null;
            ((GameState) this.handler).chosenAddon = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
