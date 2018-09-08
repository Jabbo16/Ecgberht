package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Armory;
import org.openbw.bwapi4j.unit.Factory;
import org.openbw.bwapi4j.unit.Goliath;
import org.openbw.bwapi4j.unit.Unit;

public class ChooseGoliath extends Action {

    public ChooseGoliath(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            int armories = (int) ((GameState)this.handler).UBs.stream().filter(unit -> unit instanceof Armory).count();
            if(armories < 1) return State.FAILURE;
            int count = 0;
            for (Unit u : ((GameState) this.handler).getGame().getUnits(((GameState) this.handler).getPlayer())) {
                if (!u.exists()) continue;
                if (u instanceof Goliath) count++;
                if (count >= ((GameState) this.handler).maxGoliaths) return State.FAILURE;
            }
            if (!((GameState) this.handler).Fs.isEmpty()) {
                for (Factory b : ((GameState) this.handler).Fs) {
                    if (!b.isTraining() && b.canTrain(UnitType.Terran_Goliath)) {
                        ((GameState) this.handler).chosenUnit = UnitType.Terran_Goliath;
                        ((GameState) this.handler).chosenBuilding = b;
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
