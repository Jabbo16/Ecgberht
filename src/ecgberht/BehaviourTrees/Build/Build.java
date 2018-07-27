package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.util.Pair;

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
            for (Entry<SCV, Pair<UnitType, TilePosition>> u : ((GameState) this.handler).workerBuild.entrySet()) {
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
                    } else if (u.getValue().first == UnitType.Terran_Command_Center && ((GameState) this.handler).bwem.getMap().getArea(u.getValue().second).getAccessibleNeighbors().isEmpty()) {
                        //System.out.println(((GameState)this.handler).getGame().canBuildHere(u.getValue().second,u.getValue().first,u.getKey()));
                        boolean buildT = chosen.build(u.getValue().second, u.getValue().first);
                      /*  ((GameState)this.handler).getGame().getMapDrawer().drawBoxMap(u.getValue().second.toPosition(), u.getValue().second.toPosition().add(new Position(32,32)), Color.ORANGE);
                        System.out.println("Build was " + buildT);
                        System.out.println("Type its " + u.getValue().first);
                        System.out.println("Tile its " + u.getValue().second);*/
                    } else {
                        chosen.build(u.getValue().second, u.getValue().first);
                    }

                }
            }
            for (SCV s : toRemove) ((GameState) this.handler).workerBuild.remove(s);
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
            */
            return State.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
