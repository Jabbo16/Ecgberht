package ecgberht.BehaviourTrees.AddonBuild;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;

public class CheckResourcesAddon extends Conditional {

    public CheckResourcesAddon(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            MutablePair<Integer, Integer> cash = gameState.getCash();
            if (cash.first >= (gameState.chosenAddon.mineralPrice() + gameState.deltaCash.first) && cash.second >= (gameState.chosenAddon.gasPrice()) + gameState.deltaCash.second) {
                return State.SUCCESS;
            }
            gameState.chosenBuildingAddon = null;
            gameState.chosenAddon = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
