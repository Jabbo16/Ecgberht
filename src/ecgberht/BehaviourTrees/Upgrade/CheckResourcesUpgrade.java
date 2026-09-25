package ecgberht.BehaviourTrees.Upgrade;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.*;

public class CheckResourcesUpgrade extends BrainAction {

    public CheckResourcesUpgrade(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            MutablePair<Integer, Integer> cash = gameState.getCash();
            if (gameState.chosenUpgrade != null) {
                if (cash.first >= (gameState.chosenUpgrade.mineralPrice(gameState.getPlayer().getUpgradeLevel(gameState.chosenUpgrade)) +
                        gameState.deltaCash.first) && cash.second >= (gameState.chosenUpgrade.gasPrice(gameState.getPlayer().getUpgradeLevel(gameState.chosenUpgrade)))
                        + gameState.deltaCash.second) {
                    return BrainStatus.SUCCESS;
                }
            } else if (gameState.chosenResearch != null) {
                if (cash.first >= (gameState.chosenResearch.mineralPrice() + gameState.deltaCash.first) && cash.second >= (gameState.chosenResearch.gasPrice()) + gameState.deltaCash.second) {
                    return BrainStatus.SUCCESS;
                }
            }
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}

