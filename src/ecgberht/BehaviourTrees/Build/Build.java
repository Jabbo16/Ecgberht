package ecgberht.BehaviourTrees.Build;

import bwapi.*;
import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class Build extends Action {

    public Build(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            List<Unit> toRemove = new ArrayList<>();
            for (Entry<Unit, MutablePair<UnitType, TilePosition>> u : gameState.workerBuild.entrySet()) {
                if (u.getKey().getOrder() != Order.PlaceBuilding && gameState.bw.isVisible(u.getValue().second) && gameState.canAfford(u.getValue().first)) {
                    Unit chosen = u.getKey();
                    if (u.getValue().first == UnitType.Terran_Bunker) {
                        if (!chosen.build(u.getValue().first, u.getValue().second)) {
                            gameState.deltaCash.first -= u.getValue().first.mineralPrice();
                            gameState.deltaCash.second -= u.getValue().first.gasPrice();
                            toRemove.add(chosen);
                            chosen.stop(false);
                            gameState.workerIdle.add(chosen);
                        }
                    } else if (u.getKey().getOrder() == Order.PlayerGuard) {
                        if (Math.random() < 0.8) chosen.build(u.getValue().first, u.getValue().second);
                        else chosen.move(u.getKey().getPosition().add(new Position(32, 0)));
                    } else chosen.build(u.getValue().first, u.getValue().second);
                }
            }
            for (Unit s : toRemove) gameState.workerBuild.remove(s);
            return State.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
