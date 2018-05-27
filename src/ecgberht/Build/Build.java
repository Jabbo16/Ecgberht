package ecgberht.Build;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.util.Pair;

import java.util.Map.Entry;

public class Build extends Action {

    public Build(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            for (Entry<SCV, Pair<UnitType, TilePosition>> u : ((GameState) this.handler).workerBuild.entrySet()) {
                if (u.getKey().getOrder() != Order.PlaceBuilding) {
                    SCV chosen = u.getKey();
                    if (chosen.build(u.getValue().second, u.getValue().first)) {
                        continue;
                    }
                }
            }
            return State.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
