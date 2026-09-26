package ecgberht.BehaviourTrees.Build;
import ecgberht.brain.*;

import bwem.Base;
import ecgberht.Agents.Agent;
import ecgberht.Agents.DropShipAgent;
import ecgberht.GameState;
import ecgberht.UnitInfo;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ChoosePosition extends BrainAction {

    public ChoosePosition(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.chosenToBuild == UnitType.None) return BrainStatus.FAILURE;
            Player self = gameState.getPlayer();
            TilePosition origin;
            if (gameState.chosenToBuild.isRefinery()) {
                if (!gameState.vespeneGeysers.isEmpty()) {
                    for (Entry<VespeneGeyser, Boolean> g : gameState.vespeneGeysers.entrySet()) {
                        if (!g.getValue()) {
                            gameState.chosenPosition = g.getKey().getTilePosition();
                            return BrainStatus.SUCCESS;
                        }
                    }
                }
            } else if (gameState.chosenToBuild == UnitType.Terran_Command_Center) {
                if (!gameState.islandBases.isEmpty() && gameState.islandCCs.size() < gameState.islandBases.size()) {
                    if (gameState.islandExpand) return BrainStatus.FAILURE;
                    for (Agent u : gameState.mil.agents.values()) {
                        if (u instanceof DropShipAgent && u.statusToString().equals("IDLE")) {
                            gameState.islandExpand = true;
                            return BrainStatus.FAILURE;
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
                            return BrainStatus.SUCCESS;
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
                if (valid.isEmpty()) return BrainStatus.FAILURE;
                gameState.chosenPosition = valid.get(0).getLocation();
                return BrainStatus.SUCCESS;
            } else {
                if (!gameState.eco.workerBuild.isEmpty()) {
                    for (MutablePair<UnitType, TilePosition> w : gameState.eco.workerBuild.values()) {
                        gameState.testMap.updateMap(w.second, w.first, false);
                    }
                }
                if (!gameState.chosenToBuild.equals(UnitType.Terran_Bunker) && !gameState.chosenToBuild.equals(UnitType.Terran_Missile_Turret)) {
                    if (gameState.getStrat().proxy && gameState.chosenToBuild == UnitType.Terran_Barracks) {
                        origin = gameState.mapCenter.toTilePosition();
                    } else if (gameState.mainCC != null && gameState.mainCC.first != null) {
                        origin = gameState.mainCC.first.getLocation();
                    } else origin = self.getStartLocation();
                } else if (gameState.chosenToBuild.equals(UnitType.Terran_Missile_Turret)) {
                    if (gameState.mil.defendPosition != null) origin = gameState.mil.defendPosition.toTilePosition();
                    else if (gameState.tech.DBs.isEmpty()) {
                        origin = Util.getClosestChokepoint(self.getStartLocation().toPosition()).getCenter().toTilePosition();
                    } else {
                        origin = gameState.tech.DBs.keySet().stream().findFirst().map(UnitImpl::getTilePosition).orElse(null);
                    }
                } else if (gameState.learningManager.isNaughty() && gameState.enemyRace == Race.Zerg) {
                    origin = gameState.getBunkerPositionAntiPool();
                    if (origin != null) {
                        gameState.testMap = gameState.map.clone();
                        gameState.chosenPosition = origin;
                        return BrainStatus.SUCCESS;
                    } else {
                        origin = gameState.testMap.findBunkerPositionAntiPool();
                        if (origin != null) {
                            gameState.testMap = gameState.map.clone();
                            gameState.chosenPosition = origin;
                            return BrainStatus.SUCCESS;
                        } else if (gameState.mainCC != null) origin = gameState.mainCC.second.getTilePosition();
                        else origin = gameState.getPlayer().getStartLocation();
                    }
                } else if (gameState.tech.Ts.isEmpty()) {
                    if (gameState.mil.defendPosition != null && gameState.naturalChoke != null
                            && gameState.mil.defendPosition.equals(gameState.naturalChoke.getCenter().toPosition())) {
                        origin = gameState.testMap.findBunkerPosition(gameState.naturalChoke);
                        if (origin != null) {
                            gameState.testMap = gameState.map.clone();
                            gameState.chosenPosition = origin;
                            return BrainStatus.SUCCESS;
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
                            return BrainStatus.SUCCESS;
                        } else origin = gameState.mainChoke.getCenter().toTilePosition();
                    } else if (gameState.naturalChoke != null) {
                        origin = gameState.testMap.findBunkerPosition(gameState.naturalChoke);
                        if (origin != null) {
                            gameState.testMap = gameState.map.clone();
                            gameState.chosenPosition = origin;
                            return BrainStatus.SUCCESS;
                        } else origin = gameState.mainChoke.getCenter().toTilePosition();
                    } else {
                        origin = gameState.testMap.findBunkerPosition(gameState.mainChoke);
                        if (origin != null) {
                            gameState.testMap = gameState.map.clone();
                            gameState.chosenPosition = origin;
                            return BrainStatus.SUCCESS;
                        } else origin = gameState.mainChoke.getCenter().toTilePosition();
                    }
                } else origin = gameState.tech.Ts.stream().findFirst().map(UnitImpl::getTilePosition).orElse(null);
                TilePosition position = gameState.testMap.findPositionNew(gameState.chosenToBuild, origin);
                gameState.testMap = gameState.map.clone();
                if (position != null) {
                    gameState.chosenPosition = position;
                    return BrainStatus.SUCCESS;
                }
            }
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}






