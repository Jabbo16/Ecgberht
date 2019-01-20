package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.UnitType;
import bwapi.Unit;

public class ChooseGoliath extends Action {

    public ChooseGoliath(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.UBs.stream().filter(unit -> unit.getType() == UnitType.Terran_Armory).count() < 1) return State.FAILURE;
            int count = 0;
            for (Unit u :gameState.getPlayer().getUnits()) {
                if (!u.exists()) continue;
                if (u.getType() == UnitType.Terran_Goliath) count++;
                if (count >= gameState.maxGoliaths) return State.FAILURE;
            }
            if (!gameState.Fs.isEmpty()) {
                for (Unit b : gameState.Fs) {
                    if (!b.isTraining() && b.canTrain(UnitType.Terran_Goliath)) {
                        gameState.chosenUnit = UnitType.Terran_Goliath;
                        gameState.chosenBuilding = b;
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
