package ecgberht.BehaviourTrees.Scouting;

import bwem.Base;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;

public class CheckVisibleBase extends Conditional {

    public CheckVisibleBase(String name, GameState gh) {
        super(name, gh);

    }

    @Override
    public State execute() {
        try {
            if (gameState.chosenScout == null) return State.FAILURE;
            if (!gameState.scoutSLs.isEmpty()) {
                for (Base b : gameState.scoutSLs) {
                    if ((gameState.bw.isVisible(b.getLocation()))) {
                        gameState.scoutSLs.remove(b);
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
