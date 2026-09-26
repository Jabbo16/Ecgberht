package ecgberht.BehaviourTrees.Repair;
import ecgberht.brain.*;


import bwem.Area;
import bwem.Base;
import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.Squad;
import ecgberht.UnitInfo;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class CheckBuildingFlames extends BrainAction {

    public CheckBuildingFlames(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            List<SCV> toRemove = new ArrayList<>();
            for (Entry<SCV, Mechanical> u : gameState.eco.repairerTask.entrySet()) {
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
            for (SCV s : toRemove) gameState.eco.repairerTask.remove(s);
            boolean isBeingRepaired;
            boolean cheesed = IntelligenceAgency.getEnemyStrat() == IntelligenceAgency.EnemyStrats.ZealotRush && gameState.frameCount >= 24 * 60 * 2.2;
            boolean fastExpanding = gameState.getStrat().name.contains("GreedyFE") && Util.countBuildingAll(UnitType.Terran_Command_Center) == 2 && gameState.CCs.size() < 2 && gameState.firstExpand;
            for (Bunker w : gameState.tech.DBs.keySet()) {
                int count = 0;
                if (UnitType.Terran_Bunker.maxHitPoints() != w.getHitPoints() ||
                        (cheesed && Util.countBuildingAll(UnitType.Terran_Command_Center) < 2) || fastExpanding) {
                    for (Mechanical r : gameState.eco.repairerTask.values()) {
                        if (w.equals(r)) count++;
                    }
                    if (count < 2 && (gameState.mil.defense || cheesed || fastExpanding)) {
                        gameState.chosenUnitRepair = w;
                        return BrainStatus.SUCCESS;
                    }
                    if (count == 0) {
                        gameState.chosenUnitRepair = w;
                        return BrainStatus.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (MissileTurret b : gameState.tech.Ts) {
                if (UnitType.Terran_Missile_Turret.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : gameState.eco.repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true; // TODO check to add break?
                        }
                    }
                    if (!isBeingRepaired) {
                        gameState.chosenUnitRepair = b;
                        return BrainStatus.SUCCESS;
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
                                for (Mechanical r : gameState.eco.repairerTask.values()) {
                                    if (u.unit.equals(r)) {
                                        isBeingRepaired = true;
                                    }
                                }
                                if (!isBeingRepaired) {
                                    gameState.chosenUnitRepair = (Mechanical) u.unit;
                                    return BrainStatus.SUCCESS;
                                }
                            }
                        }
                    }
                }
            }
            if (!gameState.getStrat().proxy) {
                isBeingRepaired = false;
                for (Barracks b : gameState.tech.MBs) {
                    if (UnitType.Terran_Barracks.maxHitPoints() != b.getHitPoints()) {
                        for (Mechanical r : gameState.eco.repairerTask.values()) {
                            if (b.equals(r)) {
                                isBeingRepaired = true;
                            }
                        }
                        if (!isBeingRepaired) {
                            gameState.chosenUnitRepair = b;
                            return BrainStatus.SUCCESS;
                        }
                    }
                }
            }
            isBeingRepaired = false;
            for (Factory b : gameState.tech.Fs) {
                if (b.equals(gameState.proxyBuilding)) continue;
                if (UnitType.Terran_Factory.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : gameState.eco.repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        gameState.chosenUnitRepair = b;
                        return BrainStatus.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (ResearchingFacility b : gameState.tech.UBs) {
                if (b.getType().maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : gameState.eco.repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        gameState.chosenUnitRepair = (Mechanical) b;
                        return BrainStatus.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (SupplyDepot b : gameState.SBs) {
                if (UnitType.Terran_Supply_Depot.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : gameState.eco.repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        gameState.chosenUnitRepair = b;
                        return BrainStatus.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (CommandCenter b : gameState.CCs.values()) {
                if (UnitType.Terran_Command_Center.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : gameState.eco.repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        gameState.chosenUnitRepair = b;
                        return BrainStatus.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (ComsatStation b : gameState.tech.CSs) {
                if (UnitType.Terran_Comsat_Station.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : gameState.eco.repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        gameState.chosenUnitRepair = b;
                        return BrainStatus.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (Starport b : gameState.tech.Ps) {
                if (UnitType.Terran_Starport.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : gameState.eco.repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        gameState.chosenUnitRepair = b;
                        return BrainStatus.SUCCESS;
                    }
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






