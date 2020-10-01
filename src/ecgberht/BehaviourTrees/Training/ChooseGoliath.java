package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Armory;
import org.openbw.bwapi4j.unit.Factory;
import org.openbw.bwapi4j.unit.Unit;

public class ChooseGoliath extends Action {

    public ChooseGoliath(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.UBs.stream().filter(unit -> unit instanceof Armory).count() < 1) return State.FAILURE;
            int count = 0;
            for (Unit u : gameState.getGame().getUnits(gameState.getPlayer())) {
                if (!u.exists()) continue;
                if (u.getType() == UnitType.Terran_Goliath) count++;
                if (count >= gameState.maxGoliaths) return State.FAILURE;
            }
            if (!gameState.Fs.isEmpty()) {
                for (Factory b : gameState.Fs) {
                    if (!b.isTraining() && b.canTrain(UnitType.Terran_Goliath)) {
                        gameState.chosenUnit = UnitType.Terran_Goliath;
                        gameState.chosenTrainingFacility = b;
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
