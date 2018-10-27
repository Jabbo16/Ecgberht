package ecgberht.BehaviourTrees.Upgrade;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Conditional;

public class CheckResourcesUpgrade extends Conditional {

    public CheckResourcesUpgrade(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            MutablePair<Integer, Integer> cash = this.handler.getCash();
            if (this.handler.chosenUpgrade != null) {
                if (cash.first >= (this.handler.chosenUpgrade.mineralPrice(this.handler.getPlayer().getUpgradeLevel(this.handler.chosenUpgrade)) +
                        this.handler.deltaCash.first) && cash.second >= (this.handler.chosenUpgrade.gasPrice(this.handler.getPlayer().getUpgradeLevel(this.handler.chosenUpgrade)))
                        + this.handler.deltaCash.second) {
                    return BehavioralTree.State.SUCCESS;
                }
            } else if (this.handler.chosenResearch != null) {
                if (cash.first >= (this.handler.chosenResearch.mineralPrice() + this.handler.deltaCash.first) && cash.second >= (this.handler.chosenResearch.gasPrice()) + this.handler.deltaCash.second) {
                    return BehavioralTree.State.SUCCESS;
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
