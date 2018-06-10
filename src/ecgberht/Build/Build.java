package ecgberht.Build;

import bwapi.*;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

public class Build extends Action {

    public Build(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            for (Pair<Unit, Pair<UnitType, TilePosition>> u : ((GameState) this.handler).workerBuild) {
                if (u.first.getOrder() != Order.PlaceBuilding && u.first.canBuild(u.second.first)) {
                    Unit chosen = u.first;
                    if (chosen.build(u.second.first, u.second.second)) {
                        continue;
                    }
                }
            }
            return State.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            System.err.println(e);
            return State.ERROR;
        }
    }
}
