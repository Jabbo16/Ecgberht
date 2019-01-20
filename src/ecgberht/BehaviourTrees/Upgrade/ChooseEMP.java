package ecgberht.BehaviourTrees.Upgrade;

import bwapi.Race;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;

public class ChooseEMP extends Action {

    public ChooseEMP(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.enemyRace != Race.Protoss) return State.FAILURE;
            boolean found = false;
            Unit chosen = null;
            for (Unit r : gameState.UBs) {
                if (r.getType() == UnitType.Terran_Science_Facility && !r.isResearching()) {
                    found = true;
                    chosen = r;
                    break;
                }
            }
            if (!found) return State.FAILURE;
            if (!gameState.getPlayer().isResearching(TechType.EMP_Shockwave) &&
                    !gameState.getPlayer().hasResearched(TechType.EMP_Shockwave)) {
                gameState.chosenUnitUpgrader = chosen;
                gameState.chosenResearch = TechType.EMP_Shockwave;
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
