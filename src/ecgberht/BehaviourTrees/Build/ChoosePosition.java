package ecgberht.BehaviourTrees.Build;

import bwem.Base;
import ecgberht.Agents.Agent;
import ecgberht.Agents.DropShipAgent;
import ecgberht.EnemyBuilding;
import ecgberht.GameState;
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

public class ChoosePosition extends Action {

    public ChoosePosition(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (this.handler.chosenToBuild == UnitType.None) return State.FAILURE;
            Player self = this.handler.getPlayer();
            TilePosition origin;
            if (this.handler.chosenToBuild.isRefinery()) {
                if (!this.handler.vespeneGeysers.isEmpty()) {
                    for (Entry<VespeneGeyser, Boolean> g : this.handler.vespeneGeysers.entrySet()) {
                        if (!g.getValue()) {
                            this.handler.chosenPosition = g.getKey().getTilePosition();
                            return State.SUCCESS;
                        }
                    }
                }
            } else if (this.handler.chosenToBuild == UnitType.Terran_Command_Center) {
                // TODO uncomment when BWAPI island bug is fixed
                if (!this.handler.islandBases.isEmpty() && this.handler.islandCCs.size() < this.handler.islandBases.size()) {
                    if (this.handler.islandExpand) return State.FAILURE;
                    for (Agent u : this.handler.agents.values()) {
                        if (u instanceof DropShipAgent && u.statusToString().equals("IDLE")) {
                            this.handler.islandExpand = true;
                            return State.FAILURE;
                        }
                    }
                }
                TilePosition main;
                if (this.handler.mainCC != null) main = this.handler.mainCC.second.getTilePosition();
                else main = this.handler.getPlayer().getStartLocation();
                List<Base> valid = new ArrayList<>();
                if (this.handler.strat.name.equals("PlasmaWraithHell")) {
                    for (Base b : this.handler.specialBLs) {
                        if (!this.handler.CCs.containsKey(b)) {
                            this.handler.chosenPosition = b.getLocation();
                            return State.SUCCESS;
                        }
                    }
                }
                for (Base b : this.handler.BLs) {
                    if (!this.handler.CCs.containsKey(b) && Util.isConnected(b.getLocation(), main)) valid.add(b);
                }
                List<Base> remove = new ArrayList<>();
                for (Base b : valid) {
                    if (this.handler.getGame().getBWMap().isVisible(b.getLocation())
                            && !this.handler.getGame().getBWMap().isBuildable(b.getLocation(), true)) {
                        remove.add(b);
                        continue;
                    }
                    if (b.getArea() != this.handler.naturalArea) {
                        for (Unit u : this.handler.enemyCombatUnitMemory) {
                            if (this.handler.bwem.getMap().getArea(u.getTilePosition()) == null ||
                                    !(u instanceof Attacker) || u instanceof Worker) {
                                continue;
                            }
                            if (this.handler.bwem.getMap().getArea(u.getTilePosition()).equals(b.getArea())) {
                                remove.add(b);
                                break;
                            }
                        }
                        for (EnemyBuilding u : this.handler.enemyBuildingMemory.values()) {
                            if (this.handler.bwem.getMap().getArea(u.pos) == null) continue;
                            if (this.handler.bwem.getMap().getArea(u.pos).equals(b.getArea())) {
                                remove.add(b);
                                break;
                            }
                        }
                    }
                }
                valid.removeAll(remove);
                if (valid.isEmpty()) return State.FAILURE;
                this.handler.chosenPosition = valid.get(0).getLocation();
                return State.SUCCESS;
            } else {
                if (!this.handler.workerBuild.isEmpty()) {
                    for (MutablePair<UnitType, TilePosition> w : this.handler.workerBuild.values()) {
                        this.handler.testMap.updateMap(w.second, w.first, false);
                    }
                }
                if (!this.handler.chosenToBuild.equals(UnitType.Terran_Bunker) && !this.handler.chosenToBuild.equals(UnitType.Terran_Missile_Turret)) {
                    if (this.handler.strat.proxy && this.handler.chosenToBuild == UnitType.Terran_Barracks) {
                        origin = new TilePosition(this.handler.getGame().getBWMap().mapWidth() / 2, this.handler.getGame().getBWMap().mapHeight() / 2);
                    } else origin = self.getStartLocation();
                } else if (this.handler.chosenToBuild.equals(UnitType.Terran_Missile_Turret)) {
                    if (this.handler.defendPosition != null) origin = this.handler.defendPosition.toTilePosition();
                    else if (this.handler.DBs.isEmpty()) {
                        origin = Util.getClosestChokepoint(self.getStartLocation().toPosition()).getCenter().toTilePosition();
                    } else {
                        origin = this.handler.DBs.keySet().stream().findFirst().map(UnitImpl::getTilePosition).orElse(null);
                    }
                } else if (this.handler.EI.naughty && this.handler.enemyRace == Race.Zerg) {
                    origin = this.handler.getBunkerPositionAntiPool();
                    if (origin != null) {
                        this.handler.testMap = this.handler.map.clone();
                        this.handler.chosenPosition = origin;
                        return State.SUCCESS;
                    } else {
                        origin = this.handler.testMap.findBunkerPositionAntiPool();
                        if (origin != null) {
                            this.handler.testMap = this.handler.map.clone();
                            this.handler.chosenPosition = origin;
                            return State.SUCCESS;
                        } else if (this.handler.mainCC != null) origin = this.handler.mainCC.second.getTilePosition();
                        else origin = this.handler.getPlayer().getStartLocation();
                    }
                } else if (this.handler.Ts.isEmpty()) {
                    if (this.handler.mainChoke != null &&
                            !this.handler.strat.name.equals("MechGreedyFE") &&
                            !this.handler.strat.name.equals("BioGreedyFE") &&
                            !this.handler.strat.name.equals("BioMechGreedyFE")) {
                        origin = this.handler.testMap.findBunkerPosition(this.handler.mainChoke);
                        if (origin != null) {
                            this.handler.testMap = this.handler.map.clone();
                            this.handler.chosenPosition = origin;
                            return State.SUCCESS;
                        } else origin = this.handler.mainChoke.getCenter().toTilePosition();
                    } else if (this.handler.naturalChoke != null) {
                        origin = this.handler.testMap.findBunkerPosition(this.handler.naturalChoke);
                        if (origin != null) {
                            this.handler.testMap = this.handler.map.clone();
                            this.handler.chosenPosition = origin;
                            return State.SUCCESS;
                        } else origin = this.handler.mainChoke.getCenter().toTilePosition();
                    } else {
                        origin = this.handler.testMap.findBunkerPosition(this.handler.mainChoke);
                        if (origin != null) {
                            this.handler.testMap = this.handler.map.clone();
                            this.handler.chosenPosition = origin;
                            return State.SUCCESS;
                        } else origin = this.handler.mainChoke.getCenter().toTilePosition();
                    }
                } else origin = this.handler.Ts.stream().findFirst().map(UnitImpl::getTilePosition).orElse(null);
                TilePosition position = this.handler.testMap.findPositionNew(this.handler.chosenToBuild, origin);
                this.handler.testMap = this.handler.map.clone();
                if (position != null) {
                    this.handler.chosenPosition = position;
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
