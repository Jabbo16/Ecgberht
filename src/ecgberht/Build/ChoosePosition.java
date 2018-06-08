package ecgberht.Build;

import ecgberht.GameState;
import ecgberht.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Bunker;
import org.openbw.bwapi4j.unit.MissileTurret;
import org.openbw.bwapi4j.unit.VespeneGeyser;
import org.openbw.bwapi4j.util.Pair;

import java.util.Map.Entry;

public class ChoosePosition extends Action {

    public ChoosePosition(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Player self = ((GameState) this.handler).getPlayer();
            TilePosition origin = null;
            if (((GameState) this.handler).chosenToBuild.isRefinery()) {
                if (!((GameState) this.handler).vespeneGeysers.isEmpty()) {
                    for (Entry<VespeneGeyser, Boolean> g : ((GameState) this.handler).vespeneGeysers.entrySet()) {
                        if (!g.getValue()) {
                            ((GameState) this.handler).chosenPosition = g.getKey().getTilePosition();
                            return State.SUCCESS;
                        }

                    }
                }

            } else {
                if (!((GameState) this.handler).workerBuild.isEmpty()) {
                    for (Pair<UnitType, TilePosition> w : ((GameState) this.handler).workerBuild.values()) {
                        ((GameState) this.handler).testMap.updateMap(w.second, w.first, false);
                    }
                }

                if (!((GameState) this.handler).chosenToBuild.equals(UnitType.Terran_Bunker) && !((GameState) this.handler).chosenToBuild.equals(UnitType.Terran_Missile_Turret)) {
                    if (((GameState) this.handler).strat.proxy && ((GameState) this.handler).chosenToBuild == UnitType.Terran_Barracks) {
                        origin = new TilePosition(((GameState) this.handler).getGame().getBWMap().mapWidth() / 2, ((GameState) this.handler).getGame().getBWMap().mapHeight() / 2);
                    } else {
                        //origin = BWTA.getRegion(jugador.getStartLocation()).getCenter().toTilePosition();
                        origin = self.getStartLocation();
                    }
                } else {
                    if (((GameState) this.handler).chosenToBuild.equals(UnitType.Terran_Missile_Turret)) {
                        if (((GameState) this.handler).DBs.isEmpty()) {
                            origin = Util.getClosestChokepoint(self.getStartLocation().toPosition()).getCenter().toTilePosition();
                        } else {
                            for (Bunker b : ((GameState) this.handler).DBs.keySet()) {
                                origin = b.getTilePosition();
                                break;
                            }
                        }
                    } else {
                        if (((GameState) this.handler).EI.naughty && ((GameState) this.handler).enemyRace == Race.Zerg) {
                            origin = ((GameState) this.handler).testMap.findBunkerPositionAntiPool();
                            if (origin != null) {
                                ((GameState) this.handler).testMap = ((GameState) this.handler).map.clone();
                                ((GameState) this.handler).chosenPosition = origin;
                                return State.SUCCESS;
                            } else {
                                origin = ((GameState) this.handler).getBunkerPositionAntiPool();
                                if (origin != null) {
                                    ((GameState) this.handler).testMap = ((GameState) this.handler).map.clone();
                                    ((GameState) this.handler).chosenPosition = origin;
                                    return State.SUCCESS;
                                } else {
                                    if (((GameState) this.handler).MainCC != null) {
                                        origin = ((GameState) this.handler).MainCC.second.getTilePosition();
                                    } else {
                                        origin = ((GameState) this.handler).getPlayer().getStartLocation();
                                    }
                                }
                            }
                        } else {
                            if (((GameState) this.handler).Ts.isEmpty()) {
                                if (((GameState) this.handler).mainChoke != null) {
                                    origin = ((GameState) this.handler).testMap.findBunkerPosition(((GameState) this.handler).mainChoke);
                                    if (origin != null) {
                                        ((GameState) this.handler).testMap = ((GameState) this.handler).map.clone();
                                        ((GameState) this.handler).chosenPosition = origin;
                                        return State.SUCCESS;
                                    } else {
                                        origin = ((GameState) this.handler).mainChoke.getCenter().toTilePosition();
                                    }

                                }
                            } else {
                                for (MissileTurret b : ((GameState) this.handler).Ts) {
                                    origin = b.getTilePosition();
                                    break;
                                }
                            }
                        }

                    }

                }
                TilePosition position = ((GameState) this.handler).testMap.findPosition(((GameState) this.handler).chosenToBuild, origin);
                ((GameState) this.handler).testMap = ((GameState) this.handler).map.clone();
                if (position != null) {
                    ((GameState) this.handler).chosenPosition = position;
                    return State.SUCCESS;
                }
            }

            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
