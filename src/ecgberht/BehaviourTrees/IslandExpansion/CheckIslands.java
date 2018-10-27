package ecgberht.BehaviourTrees.IslandExpansion;

import bwem.Base;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;


public class CheckIslands extends Conditional {

    public CheckIslands(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (this.handler.islandBases.isEmpty() || !this.handler.islandExpand)
                return State.FAILURE;
            for (Base b : this.handler.islandBases) {
                if (!this.handler.islandCCs.containsKey(b)) return State.SUCCESS;
            }
            this.handler.chosenDropShip = null;
            this.handler.chosenWorker = null;
            this.handler.chosenIsland = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
