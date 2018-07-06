package ecgberht.Training;

import ecgberht.GameState;
import ecgberht.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.ControlTower;
import org.openbw.bwapi4j.unit.ResearchingFacility;
import org.openbw.bwapi4j.unit.Starport;


public class ChooseSituationalUnit extends Action {

    public ChooseSituationalUnit(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            // Testing vessels
            if (Util.countUnitTypeSelf(UnitType.Terran_Science_Vessel) > 2) return State.FAILURE;
            boolean tower = false;
            for (ResearchingFacility u : ((GameState) this.handler).UBs) {
                if (u instanceof ControlTower) {
                    tower = true;
                    break;
                }
            }
            if (!tower) return State.FAILURE;
            for (Starport s : ((GameState) this.handler).Ps) {
                if (s.getAddon() != null && s.getAddon().isCompleted() && !s.isTraining()) {
                    ((GameState) this.handler).chosenUnit = UnitType.Terran_Science_Vessel;
                    ((GameState) this.handler).chosenBuilding = s;
                    return State.SUCCESS;
                }
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
