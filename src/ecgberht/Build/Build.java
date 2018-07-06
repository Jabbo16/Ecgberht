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
            //List<SCV> toRemove = new ArrayList<>();
            for (Entry<SCV, Pair<UnitType, TilePosition>> u : ((GameState) this.handler).workerBuild.entrySet()) {
                if (u.getKey().getOrder() != Order.PlaceBuilding && ((GameState) this.handler).canAfford(u.getValue().first)) {
                    if (u.getValue().first == UnitType.Terran_Bunker) {
                        SCV chosen = u.getKey();
                        if (!chosen.build(u.getValue().second, u.getValue().first)) {
                            ((GameState) this.handler).deltaCash.first -= u.getValue().first.mineralPrice();
                            ((GameState) this.handler).deltaCash.second -= u.getValue().first.gasPrice();
                            ((GameState) this.handler).workerBuild.remove(chosen);
                            chosen.stop(false);
                            ((GameState) this.handler).workerIdle.add(chosen);
                        }
                    } else {
                        SCV chosen = u.getKey();
                        chosen.build(u.getValue().second, u.getValue().first);
                    }

                }
            }
                /*if ((u.getKey().getOrder() != Order.PlaceBuilding || ((GameState) this.handler).frameCount % 24*10 == 0)
                        && u.getKey().getDistance(u.getValue().second.toPosition()) <= 130) {
                    SCV chosen = u.getKey();
                    *//*if (((GameState) this.handler).canAfford(u.getValue().first) && !chosen.build(u.getValue().second, u.getValue().first)) {
                        ((GameState) this.handler).deltaCash.first -= u.getValue().first.mineralPrice();
                        ((GameState) this.handler).deltaCash.second -= u.getValue().first.gasPrice();
                        toRemove.add(chosen);
                    }*//*
                    if (((GameState) this.handler).canAfford(u.getValue().first)) chosen.build(u.getValue().second, u.getValue().first);
                } else if (u.getKey().isIdle() && ((GameState) this.handler).canAfford(u.getValue().first)) {
                    SCV chosen = u.getKey();
                    ((GameState) this.handler).deltaCash.first -= u.getValue().first.mineralPrice();
                    ((GameState) this.handler).deltaCash.second -= u.getValue().first.gasPrice();
                    toRemove.add(chosen);
                }
            }
            for (SCV s : toRemove) {
                ((GameState) this.handler).workerBuild.remove(s);
                s.move(((GameState)this.handler).getNearestCC(s.getPosition()));
                ((GameState) this.handler).workerIdle.add(s);
            }*/
            return State.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
