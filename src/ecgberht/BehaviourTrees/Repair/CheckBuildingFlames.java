package ecgberht.BehaviourTrees.Repair;


import bwem.Base;
import bwem.area.Area;
import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.Squad;
import ecgberht.UnitStorage;
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
            for (Entry<SCV, Mechanical> u : this.handler.repairerTask.entrySet()) {
                if (u.getValue().maxHitPoints() != u.getValue().getHitPoints()) {
                    if (u.getKey().getOrder() != Order.Follow && u.getKey().getOrder() != Order.Repair) {
                        u.getKey().rightClick(u.getValue(), false);
                    }
                } else if (Util.countBuildingAll(UnitType.Terran_Command_Center) < 2 && u.getValue() instanceof Bunker &&
                        IntelligenceAgency.getEnemyStrat() == IntelligenceAgency.EnemyStrats.ZealotRush && this.handler.frameCount >= 24 * 60 * 2.2) {
                    if (u.getKey().getDistance(u.getValue()) > 3 * 32) u.getKey().move(u.getValue().getPosition());
                } else if (Util.countBuildingAll(UnitType.Terran_Command_Center) == 2 && this.handler.CCs.size() < 2 && u.getValue() instanceof Bunker) {
                    if (u.getKey().getDistance(u.getValue()) > 3 * 32) u.getKey().move(u.getValue().getPosition());
                } else {
                    u.getKey().stop(false);
                    this.handler.workerIdle.add(u.getKey());
                    toRemove.add(u.getKey());
                }
            }
            for (SCV s : toRemove) this.handler.repairerTask.remove(s);
            boolean isBeingRepaired;
            boolean cheesed = IntelligenceAgency.getEnemyStrat() == IntelligenceAgency.EnemyStrats.ZealotRush && this.handler.frameCount >= 24 * 60 * 2.2;
            boolean fastExpanding = this.handler.strat.name.contains("GreedyFE") && Util.countBuildingAll(UnitType.Terran_Command_Center) == 2 && this.handler.CCs.size() < 2 && this.handler.firstExpand;
            for (Bunker w : this.handler.DBs.keySet()) {
                int count = 0;
                if (UnitType.Terran_Bunker.maxHitPoints() != w.getHitPoints() ||
                        (cheesed && Util.countBuildingAll(UnitType.Terran_Command_Center) < 2) || fastExpanding) {
                    for (Mechanical r : this.handler.repairerTask.values()) {
                        if (w.equals(r)) count++;
                    }
                    if (count < 2 && (this.handler.defense || cheesed || fastExpanding)) {
                        this.handler.chosenUnitRepair = w;
                        return State.SUCCESS;
                    }
                    if (count == 0) {
                        this.handler.chosenUnitRepair = w;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (MissileTurret b : this.handler.Ts) {
                if (UnitType.Terran_Missile_Turret.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : this.handler.repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true; // TODO check to add break?
                        }
                    }
                    if (!isBeingRepaired) {
                        this.handler.chosenUnitRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (Squad s : this.handler.sqManager.squads.values()) {
                if (s.status != Squad.Status.IDLE) continue;
                for (UnitStorage.UnitInfo u : s.members) {
                    if (u.unit instanceof Mechanical && u.health != u.unitType.maxHitPoints()) {
                        Area unitArea = this.handler.bwem.getMap().getArea(u.tileposition);
                        for (Base b : this.handler.CCs.keySet()) {
                            if (unitArea != null && b.getArea().equals(unitArea)) {
                                for (Mechanical r : this.handler.repairerTask.values()) {
                                    if (u.equals(r)) {
                                        isBeingRepaired = true;
                                    }
                                }
                                if (!isBeingRepaired) {
                                    this.handler.chosenUnitRepair = (Mechanical) u.unit;
                                    return State.SUCCESS;
                                }
                            }
                        }
                    }
                }
            }
            if (!this.handler.strat.proxy) {
                isBeingRepaired = false;
                for (Barracks b : this.handler.MBs) {
                    if (UnitType.Terran_Barracks.maxHitPoints() != b.getHitPoints()) {
                        for (Mechanical r : this.handler.repairerTask.values()) {
                            if (b.equals(r)) {
                                isBeingRepaired = true;
                            }
                        }
                        if (!isBeingRepaired) {
                            this.handler.chosenUnitRepair = b;
                            return State.SUCCESS;
                        }
                    }
                }
            }
            isBeingRepaired = false;
            for (Factory b : this.handler.Fs) {
                if (UnitType.Terran_Factory.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : this.handler.repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        this.handler.chosenUnitRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (ResearchingFacility b : this.handler.UBs) {
                if (b.getType().maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : this.handler.repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        this.handler.chosenUnitRepair = (Mechanical) b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (SupplyDepot b : this.handler.SBs) {
                if (UnitType.Terran_Supply_Depot.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : this.handler.repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        this.handler.chosenUnitRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (CommandCenter b : this.handler.CCs.values()) {
                if (UnitType.Terran_Command_Center.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : this.handler.repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        this.handler.chosenUnitRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (ComsatStation b : this.handler.CSs) {
                if (UnitType.Terran_Comsat_Station.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : this.handler.repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        this.handler.chosenUnitRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (Starport b : this.handler.Ps) {
                if (UnitType.Terran_Starport.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : this.handler.repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        this.handler.chosenUnitRepair = b;
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
