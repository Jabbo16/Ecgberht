package ecgberht.BehaviourTrees.Scouting;

import bwem.Base;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Conditional;

public class CheckVisibleBase extends Conditional {

    public CheckVisibleBase(String name, GameState gh) {
        super(name, gh);

    }

    @Override
    public BehavioralTree.State execute() {
        try {
            if (this.handler.chosenScout == null) {
                return BehavioralTree.State.FAILURE;
            }
            if (!this.handler.scoutSLs.isEmpty()) {
                for (Base b : this.handler.scoutSLs) {
                    if ((this.handler.getGame().getBWMap().isVisible(b.getLocation()))) {
                        this.handler.scoutSLs.remove(b);
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
