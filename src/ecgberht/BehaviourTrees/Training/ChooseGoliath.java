package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Armory;
import org.openbw.bwapi4j.unit.Factory;
import org.openbw.bwapi4j.unit.Goliath;
import org.openbw.bwapi4j.unit.Unit;

public class ChooseGoliath extends Action {

    public ChooseGoliath(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (this.handler.UBs.stream().filter(unit -> unit instanceof Armory).count() < 1) return State.FAILURE;
            int count = 0;
            for (Unit u : this.handler.getGame().getUnits(this.handler.getPlayer())) {
                if (!u.exists()) continue;
                if (u.getType() == UnitType.Terran_Goliath) count++;
                else continue;
                if (count >= this.handler.maxGoliaths) return State.FAILURE;
            }
            if (!this.handler.Fs.isEmpty()) {
                for (Factory b : this.handler.Fs) {
                    if (!b.isTraining() && b.canTrain(UnitType.Terran_Goliath)) {
                        this.handler.chosenUnit = UnitType.Terran_Goliath;
                        this.handler.chosenBuilding = b;
                        return State.SUCCESS;
                    }
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
