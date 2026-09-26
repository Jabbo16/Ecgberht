package ecgberht.BehaviourTrees.AddonBuild;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.*;

public class CheckResourcesAddon extends BrainAction {

    public CheckResourcesAddon(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            MutablePair<Integer, Integer> cash = gameState.getCash();
            if (cash.first >= (gameState.chosenAddon.mineralPrice() + gameState.eco.deltaCash.first) && cash.second >= (gameState.chosenAddon.gasPrice()) + gameState.eco.deltaCash.second) {
                return BrainStatus.SUCCESS;
            }
            gameState.tech.chosenBuildingAddon = null;
            gameState.chosenAddon = null;
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}






