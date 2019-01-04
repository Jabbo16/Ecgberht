package ecgberht.BehaviourTrees.Upgrade;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;

public class CheckResourcesUpgrade extends Conditional {

    public CheckResourcesUpgrade(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            MutablePair<Integer, Integer> cash = gameState.getCash();
            if (gameState.chosenUpgrade != null) {
                if (cash.first >= (gameState.chosenUpgrade.mineralPrice(gameState.getPlayer().getUpgradeLevel(gameState.chosenUpgrade)) +
                        gameState.deltaCash.first) && cash.second >= (gameState.chosenUpgrade.gasPrice(gameState.getPlayer().getUpgradeLevel(gameState.chosenUpgrade)))
                        + gameState.deltaCash.second) {
                    return State.SUCCESS;
                }
            } else if (gameState.chosenResearch != null) {
                if (cash.first >= (gameState.chosenResearch.mineralPrice() + gameState.deltaCash.first) && cash.second >= (gameState.chosenResearch.gasPrice()) + gameState.deltaCash.second) {
                    return State.SUCCESS;
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
