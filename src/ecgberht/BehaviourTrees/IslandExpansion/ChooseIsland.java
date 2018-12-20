package ecgberht.BehaviourTrees.IslandExpansion;

import bwem.Base;
import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.Position;

public class ChooseIsland extends Action {

    public ChooseIsland(String name, GameState gh) {
        super(name, gh);

    }

    @Override
    public State execute() {
        try {
            Base chosen = null;
            double distMax = Double.MAX_VALUE;
            Position drop = this.handler.chosenDropShip.unit.getPosition();
            for (Base b : this.handler.islandBases) {
                if (this.handler.islandCCs.containsKey(b)) continue;
                double dist = Util.broodWarDistance(b.getLocation().toPosition(), drop);
                if (dist < distMax) {
                    distMax = dist;
                    chosen = b;
                }
            }
            if (chosen != null) {
                this.handler.chosenIsland = chosen;
                return State.SUCCESS;
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