package ecgberht.BehaviourTrees.IslandExpansion;

import bwem.Base;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Conditional;


public class CheckIslands extends Conditional {

    public CheckIslands(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            if (this.handler.islandBases.isEmpty() || !this.handler.islandExpand)
                return BehavioralTree.State.FAILURE;
            for (Base b : this.handler.islandBases) {
                if (!this.handler.islandCCs.containsKey(b)) return BehavioralTree.State.SUCCESS;
            }
            this.handler.chosenDropShip = null;
            this.handler.chosenWorker = null;
            this.handler.chosenIsland = null;
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}
