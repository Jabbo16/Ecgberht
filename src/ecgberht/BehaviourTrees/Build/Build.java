package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.SCV;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class Build extends Action {

    public Build(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            List<SCV> toRemove = new ArrayList<>();
            for (Entry<SCV, MutablePair<UnitType, TilePosition>> u : ((GameState) this.handler).workerBuild.entrySet()) {
                if (u.getKey().getOrder() != Order.PlaceBuilding && ((GameState) this.handler).canAfford(u.getValue().first)) {
                    SCV chosen = u.getKey();
                    if (u.getValue().first == UnitType.Terran_Bunker) {
                        if (!chosen.build(u.getValue().second, u.getValue().first)) {
                            ((GameState) this.handler).deltaCash.first -= u.getValue().first.mineralPrice();
                            ((GameState) this.handler).deltaCash.second -= u.getValue().first.gasPrice();
                            toRemove.add(chosen);
                            chosen.stop(false);
                            ((GameState) this.handler).workerIdle.add(chosen);
                        }
                    } else if (u.getKey().getOrder() == Order.PlayerGuard) { // TODO test
                        if (Math.random() < 0.8) chosen.build(u.getValue().second, u.getValue().first);
                        else chosen.move(u.getKey().getPosition().add(new Position(32, 0)));
                    } else chosen.build(u.getValue().second, u.getValue().first);
                }
            }
            for (SCV s : toRemove) ((GameState) this.handler).workerBuild.remove(s);
            return State.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
