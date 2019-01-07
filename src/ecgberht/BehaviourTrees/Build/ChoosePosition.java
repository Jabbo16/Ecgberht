package ecgberht.BehaviourTrees.Build;

import bwem.Base;
import ecgberht.Agents.Agent;
import ecgberht.Agents.DropShipAgent;
import ecgberht.GameState;
import ecgberht.UnitInfo;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ChoosePosition extends Action {

    public ChoosePosition(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.chosenToBuild == UnitType.None) return State.FAILURE;
            Player self = gameState.getPlayer();
            TilePosition origin;
            if (gameState.chosenToBuild.isRefinery()) {
                if (!gameState.vespeneGeysers.isEmpty()) {
                    for (Entry<VespeneGeyser, Boolean> g : gameState.vespeneGeysers.entrySet()) {
                        if (!g.getValue()) {
                            gameState.chosenPosition = g.getKey().getTilePosition();
                            return State.SUCCESS;
                        }
                    }
                }
            } else if (gameState.chosenToBuild == UnitType.Terran_Command_Center) {
                if (!gameState.islandBases.isEmpty() && gameState.islandCCs.size() < gameState.islandBases.size()) {
                    if (gameState.islandExpand) return State.FAILURE;
                    for (Agent u : gameState.agents.values()) {
                        if (u instanceof DropShipAgent && u.statusToString().equals("IDLE")) {
                            gameState.islandExpand = true;
                            return State.FAILURE;
                        }
                    }
                }
                TilePosition main;
                if (gameState.mainCC != null) main = gameState.mainCC.second.getTilePosition();
                else main = gameState.getPlayer().getStartLocation();
                List<Base> valid = new ArrayList<>();
                if (gameState.getStrat().name.equals("PlasmaWraithHell")) {
                    for (Base b : gameState.specialBLs) {
                        if (!gameState.CCs.containsKey(b)) {
                            gameState.chosenPosition = b.getLocation();
                            return State.SUCCESS;
                        }
                    }
                }
                for (Base b : gameState.BLs) {
                    if (!gameState.CCs.containsKey(b) && Util.isConnected(b.getLocation(), main)) valid.add(b);
                }
                List<Base> remove = new ArrayList<>();
                for (Base b : valid) {
                    if (gameState.getGame().getBWMap().isVisible(b.getLocation())
                            && !gameState.getGame().getBWMap().isBuildable(b.getLocation(), true)) {
                        remove.add(b);
                        continue;
                    }
                    if (b.getArea() != gameState.naturalArea) {
                        for (Unit u : gameState.enemyCombatUnitMemory) {
                            if (gameState.bwem.getMap().getArea(u.getTilePosition()) == null ||
                                    !(u instanceof Attacker) || u instanceof Worker) {
                                continue;
                            }
                            if (gameState.bwem.getMap().getArea(u.getTilePosition()).equals(b.getArea())) {
                                remove.add(b);
                                break;
                            }
                        }
                        for (UnitInfo u : gameState.unitStorage.getEnemyUnits().values().stream().filter(u -> u.unitType.isBuilding()).collect(Collectors.toSet())) {
                            if (gameState.bwem.getMap().getArea(u.tileposition) == null) continue;
                            if (gameState.bwem.getMap().getArea(u.tileposition).equals(b.getArea())) {
                                remove.add(b);
                                break;
                            }
                        }
                    }
                }
                valid.removeAll(remove);
                if (valid.isEmpty()) return State.FAILURE;
                gameState.chosenPosition = valid.get(0).getLocation();
                return State.SUCCESS;
            } else {
                if (!gameState.workerBuild.isEmpty()) {
                    for (MutablePair<UnitType, TilePosition> w : gameState.workerBuild.values()) {
                        gameState.testMap.updateMap(w.second, w.first, false);
                    }
                }
                if (!gameState.chosenToBuild.equals(UnitType.Terran_Bunker) && !gameState.chosenToBuild.equals(UnitType.Terran_Missile_Turret)) {
                    if (gameState.getStrat().proxy && gameState.chosenToBuild == UnitType.Terran_Barracks) {
                        origin = new TilePosition(gameState.getGame().getBWMap().mapWidth() / 2, gameState.getGame().getBWMap().mapHeight() / 2);
                    } else if (gameState.mainCC != null && gameState.mainCC.first != null) {
                        origin = gameState.mainCC.first.getLocation();
                    } else origin = self.getStartLocation();
                } else if (gameState.chosenToBuild.equals(UnitType.Terran_Missile_Turret)) {
                    if (gameState.defendPosition != null) origin = gameState.defendPosition.toTilePosition();
                    else if (gameState.DBs.isEmpty()) {
                        origin = Util.getClosestChokepoint(self.getStartLocation().toPosition()).getCenter().toTilePosition();
                    } else {
                        origin = gameState.DBs.keySet().stream().findFirst().map(UnitImpl::getTilePosition).orElse(null);
                    }
                } else if (gameState.learningManager.isNaughty() && gameState.enemyRace == Race.Zerg) {
                    origin = gameState.getBunkerPositionAntiPool();
                    if (origin != null) {
                        gameState.testMap = gameState.map.clone();
                        gameState.chosenPosition = origin;
                        return State.SUCCESS;
                    } else {
                        origin = gameState.testMap.findBunkerPositionAntiPool();
                        if (origin != null) {
                            gameState.testMap = gameState.map.clone();
                            gameState.chosenPosition = origin;
                            return State.SUCCESS;
                        } else if (gameState.mainCC != null) origin = gameState.mainCC.second.getTilePosition();
                        else origin = gameState.getPlayer().getStartLocation();
                    }
                } else if (gameState.Ts.isEmpty()) {
                    if (gameState.defendPosition != null && gameState.naturalChoke != null
                            && gameState.defendPosition.equals(gameState.naturalChoke.getCenter().toPosition())) {
                        origin = gameState.testMap.findBunkerPosition(gameState.naturalChoke);
                        if (origin != null) {
                            gameState.testMap = gameState.map.clone();
                            gameState.chosenPosition = origin;
                            return State.SUCCESS;
                        }
                    }
                    if (gameState.mainChoke != null &&
                            !gameState.getStrat().name.equals("MechGreedyFE") &&
                            !gameState.getStrat().name.equals("BioGreedyFE") &&
                            !gameState.getStrat().name.equals("14CC") &&
                            !gameState.getStrat().name.equals("BioMechGreedyFE")) {
                        origin = gameState.testMap.findBunkerPosition(gameState.mainChoke);
                        if (origin != null) {
                            gameState.testMap = gameState.map.clone();
                            gameState.chosenPosition = origin;
                            return State.SUCCESS;
                        } else origin = gameState.mainChoke.getCenter().toTilePosition();
                    } else if (gameState.naturalChoke != null) {
                        origin = gameState.testMap.findBunkerPosition(gameState.naturalChoke);
                        if (origin != null) {
                            gameState.testMap = gameState.map.clone();
                            gameState.chosenPosition = origin;
                            return State.SUCCESS;
                        } else origin = gameState.mainChoke.getCenter().toTilePosition();
                    } else {
                        origin = gameState.testMap.findBunkerPosition(gameState.mainChoke);
                        if (origin != null) {
                            gameState.testMap = gameState.map.clone();
                            gameState.chosenPosition = origin;
                            return State.SUCCESS;
                        } else origin = gameState.mainChoke.getCenter().toTilePosition();
                    }
                } else origin = gameState.Ts.stream().findFirst().map(UnitImpl::getTilePosition).orElse(null);
                TilePosition position = gameState.testMap.findPositionNew(gameState.chosenToBuild, origin);
                gameState.testMap = gameState.map.clone();
                if (position != null) {
                    gameState.chosenPosition = position;
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
