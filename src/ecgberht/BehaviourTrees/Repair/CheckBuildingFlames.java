package ecgberht.BehaviourTrees.Repair;


import bwem.Base;
import bwem.area.Area;
import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.Squad;
import ecgberht.UnitInfo;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class CheckBuildingFlames extends Action {

    public CheckBuildingFlames(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            List<SCV> toRemove = new ArrayList<>();
            for (Entry<SCV, Mechanical> u : gameState.repairerTask.entrySet()) {
                if (u.getValue().maxHitPoints() != u.getValue().getHitPoints()) {
                    if (u.getKey().getOrder() != Order.Follow && u.getKey().getOrder() != Order.Repair) {
                        u.getKey().rightClick(u.getValue(), false);
                    }
                } else if (Util.countBuildingAll(UnitType.Terran_Command_Center) < 2 && u.getValue() instanceof Bunker &&
                        IntelligenceAgency.getEnemyStrat() == IntelligenceAgency.EnemyStrats.ZealotRush && gameState.frameCount >= 24 * 60 * 2.2) {
                    if (u.getKey().getDistance(u.getValue()) > 3 * 32) u.getKey().move(u.getValue().getPosition());
                } else if (Util.countBuildingAll(UnitType.Terran_Command_Center) == 2 && gameState.CCs.size() < 2 && u.getValue() instanceof Bunker) {
                    if (u.getKey().getDistance(u.getValue()) > 3 * 32) u.getKey().move(u.getValue().getPosition());
                } else {
                    u.getKey().stop(false);
                    gameState.workerIdle.add(u.getKey());
                    toRemove.add(u.getKey());
                }
            }
            for (SCV s : toRemove) gameState.repairerTask.remove(s);
            boolean isBeingRepaired;
            boolean cheesed = IntelligenceAgency.getEnemyStrat() == IntelligenceAgency.EnemyStrats.ZealotRush && gameState.frameCount >= 24 * 60 * 2.2;
            boolean fastExpanding = gameState.strat.name.contains("GreedyFE") && Util.countBuildingAll(UnitType.Terran_Command_Center) == 2 && gameState.CCs.size() < 2 && gameState.firstExpand;
            for (Bunker w : gameState.DBs.keySet()) {
                int count = 0;
                if (UnitType.Terran_Bunker.maxHitPoints() != w.getHitPoints() ||
                        (cheesed && Util.countBuildingAll(UnitType.Terran_Command_Center) < 2) || fastExpanding) {
                    for (Mechanical r : gameState.repairerTask.values()) {
                        if (w.equals(r)) count++;
                    }
                    if (count < 2 && (gameState.defense || cheesed || fastExpanding)) {
                        gameState.chosenUnitRepair = w;
                        return State.SUCCESS;
                    }
                    if (count == 0) {
                        gameState.chosenUnitRepair = w;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (MissileTurret b : gameState.Ts) {
                if (UnitType.Terran_Missile_Turret.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : gameState.repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true; // TODO check to add break?
                        }
                    }
                    if (!isBeingRepaired) {
                        gameState.chosenUnitRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (Squad s : gameState.sqManager.squads.values()) {
                if (s.status != Squad.Status.IDLE) continue;
                for (UnitInfo u : s.members) {
                    if (u.unit instanceof Mechanical && u.health != u.unitType.maxHitPoints()) {
                        Area unitArea = gameState.bwem.getMap().getArea(u.tileposition);
                        for (Base b : gameState.CCs.keySet()) {
                            if (unitArea != null && b.getArea().equals(unitArea)) {
                                for (Mechanical r : gameState.repairerTask.values()) {
                                    if (u.unit.equals(r)) {
                                        isBeingRepaired = true;
                                    }
                                }
                                if (!isBeingRepaired) {
                                    gameState.chosenUnitRepair = (Mechanical) u.unit;
                                    return State.SUCCESS;
                                }
                            }
                        }
                    }
                }
            }
            if (!gameState.strat.proxy) {
                isBeingRepaired = false;
                for (Barracks b : gameState.MBs) {
                    if (UnitType.Terran_Barracks.maxHitPoints() != b.getHitPoints()) {
                        for (Mechanical r : gameState.repairerTask.values()) {
                            if (b.equals(r)) {
                                isBeingRepaired = true;
                            }
                        }
                        if (!isBeingRepaired) {
                            gameState.chosenUnitRepair = b;
                            return State.SUCCESS;
                        }
                    }
                }
            }
            isBeingRepaired = false;
            for (Factory b : gameState.Fs) {
                if (UnitType.Terran_Factory.maxHitPoints() != b.getHitPoints()) {
                    if (b.equals(gameState.proxyBuilding)) continue;
                    for (Mechanical r : gameState.repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        gameState.chosenUnitRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (ResearchingFacility b : gameState.UBs) {
                if (b.getType().maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : gameState.repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        gameState.chosenUnitRepair = (Mechanical) b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (SupplyDepot b : gameState.SBs) {
                if (UnitType.Terran_Supply_Depot.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : gameState.repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        gameState.chosenUnitRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (CommandCenter b : gameState.CCs.values()) {
                if (UnitType.Terran_Command_Center.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : gameState.repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        gameState.chosenUnitRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (ComsatStation b : gameState.CSs) {
                if (UnitType.Terran_Comsat_Station.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : gameState.repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        gameState.chosenUnitRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (Starport b : gameState.Ps) {
                if (UnitType.Terran_Starport.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : gameState.repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        gameState.chosenUnitRepair = b;
                        return State.SUCCESS;
                    }
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
