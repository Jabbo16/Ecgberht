package ecgberht.BehaviourTrees.IslandExpansion;

import bwem.Base;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Position;

public class ChooseIsland extends Action {

    public ChooseIsland(String name, GameHandler gh) {
        super(name, gh);

    }

    @Override
    public State execute() {
        try {
            Base chosen = null;
            double distMax = Double.MAX_VALUE;
            Position drop = ((GameState) this.handler).chosenDropShip.unit.getPosition();
            for (Base b : ((GameState) this.handler).islandBases) {
                if (((GameState) this.handler).CCs.containsKey(b)) continue;
                double dist = ((GameState) this.handler).broodWarDistance(b.getLocation().toPosition(), drop);
                if (dist < distMax) {
                    distMax = dist;
                    chosen = b;
                }
            }
            if (chosen != null) {
                ((GameState) this.handler).chosenIsland = chosen;
                return State.SUCCESS;
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