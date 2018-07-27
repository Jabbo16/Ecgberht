package ecgberht.BehaviourTrees.IslandExpansion;

import bwem.Base;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;


public class CheckIslands extends Conditional {

    public CheckIslands(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).islandBases.isEmpty() || !((GameState) this.handler).islandExpand)
                return State.FAILURE;
            for (Base b : ((GameState) this.handler).islandBases) {
                if (!((GameState) this.handler).CCs.containsKey(b)) return State.SUCCESS;
            }
            ((GameState) this.handler).chosenDropShip = null;
            ((GameState) this.handler).chosenWorker = null;
            ((GameState) this.handler).chosenIsland = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
