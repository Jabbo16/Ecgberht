package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree;
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
    public BehavioralTree.State execute() {
        try {
            int armories = (int) this.handler.UBs.stream().filter(unit -> unit instanceof Armory).count();
            if (armories < 1) return BehavioralTree.State.FAILURE;
            int count = 0;
            for (Unit u : this.handler.getGame().getUnits(this.handler.getPlayer())) {
                if (!u.exists()) continue;
                if (u instanceof Goliath) count++;
                if (count >= this.handler.maxGoliaths) return BehavioralTree.State.FAILURE;
            }
            if (!this.handler.Fs.isEmpty()) {
                for (Factory b : this.handler.Fs) {
                    if (!b.isTraining() && b.canTrain(UnitType.Terran_Goliath)) {
                        this.handler.chosenUnit = UnitType.Terran_Goliath;
                        this.handler.chosenBuilding = b;
                        return BehavioralTree.State.SUCCESS;
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
