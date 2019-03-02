package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;
import org.openbw.bwapi4j.type.UnitType;

public class CheckResourcesUnit extends Conditional {

    public CheckResourcesUnit(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            MutablePair<Integer, Integer> cash = gameState.getCash();
            if (cash.first >= (gameState.chosenUnit.mineralPrice() + gameState.deltaCash.first) && cash.second >= (gameState.chosenUnit.gasPrice()) + gameState.deltaCash.second) {
                return State.SUCCESS;
            }
            gameState.chosenBuilding = null;
            gameState.chosenToBuild = UnitType.None;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
