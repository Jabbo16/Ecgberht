package ecgberht.BehaviourTrees.Repair;


import bwem.Base;
import bwem.area.Area;
import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.Squad;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class CheckBuildingFlames extends Action {

    public CheckBuildingFlames(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            List<SCV> toRemove = new ArrayList<>();
            for (Entry<SCV, Mechanical> u : ((GameState) this.handler).repairerTask.entrySet()) {
                if (u.getValue().maxHitPoints() != u.getValue().getHitPoints()) {
                    if (u.getKey().getOrder() != Order.Follow && u.getKey().getOrder() != Order.Repair) {
                        u.getKey().rightClick(u.getValue(), false);
                    }
                } else if (((GameState) this.handler).countBuildingAll(UnitType.Terran_Command_Center) < 2 && u.getValue() instanceof Bunker &&
                        IntelligenceAgency.getEnemyStrat() == IntelligenceAgency.EnemyStrats.ZealotRush) {
                    if (u.getKey().getDistance(u.getValue()) > 3 * 32) u.getKey().move(u.getValue().getPosition());
                } else {
                    u.getKey().stop(false);
                    ((GameState) this.handler).workerIdle.add(u.getKey());
                    toRemove.add(u.getKey());
                }
            }
            for (SCV s : toRemove) ((GameState) this.handler).repairerTask.remove(s);
            boolean isBeingRepaired;
            boolean cheesed = IntelligenceAgency.getEnemyStrat() == IntelligenceAgency.EnemyStrats.ZealotRush;
            for (Bunker w : ((GameState) this.handler).DBs.keySet()) {
                int count = 0;
                if (UnitType.Terran_Bunker.maxHitPoints() != w.getHitPoints() ||
                        (cheesed && ((GameState) this.handler).countBuildingAll(UnitType.Terran_Command_Center) < 2)) {
                    for (Mechanical r : ((GameState) this.handler).repairerTask.values()) {
                        if (w.equals(r)) count++;
                    }
                    if (count < 2 && (((GameState) this.handler).defense || cheesed)) {
                        ((GameState) this.handler).chosenUnitRepair = w;
                        return State.SUCCESS;
                    }
                    if (count == 0) {
                        ((GameState) this.handler).chosenUnitRepair = w;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (MissileTurret b : ((GameState) this.handler).Ts) {
                if (UnitType.Terran_Missile_Turret.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : ((GameState) this.handler).repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        ((GameState) this.handler).chosenUnitRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (Squad s : ((GameState) this.handler).sqManager.squads.values()) {
                if (s.status != Squad.Status.IDLE) continue;
                for (Unit u : s.members) {
                    if (u instanceof Mechanical && ((Mechanical) u).getHitPoints() != u.getType().maxHitPoints()) {
                        Area unitArea = ((GameState) this.handler).bwem.getMap().getArea(u.getTilePosition());
                        for (Base b : ((GameState) this.handler).CCs.keySet()) {
                            if (unitArea != null && b.getArea().equals(unitArea)) {
                                for (Mechanical r : ((GameState) this.handler).repairerTask.values()) {
                                    if (u.equals(r)) {
                                        isBeingRepaired = true;
                                    }
                                }
                                if (!isBeingRepaired) {
                                    ((GameState) this.handler).chosenUnitRepair = (Mechanical) u;
                                    return State.SUCCESS;
                                }
                            }
                        }
                    }
                }
            }
            isBeingRepaired = false;
            for (Barracks b : ((GameState) this.handler).MBs) {
                if (UnitType.Terran_Barracks.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : ((GameState) this.handler).repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        ((GameState) this.handler).chosenUnitRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (Factory b : ((GameState) this.handler).Fs) {
                if (UnitType.Terran_Factory.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : ((GameState) this.handler).repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        ((GameState) this.handler).chosenUnitRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (ResearchingFacility b : ((GameState) this.handler).UBs) {
                if (b.getType().maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : ((GameState) this.handler).repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        ((GameState) this.handler).chosenUnitRepair = (Mechanical) b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (SupplyDepot b : ((GameState) this.handler).SBs) {
                if (UnitType.Terran_Supply_Depot.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : ((GameState) this.handler).repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        ((GameState) this.handler).chosenUnitRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (CommandCenter b : ((GameState) this.handler).CCs.values()) {
                if (UnitType.Terran_Command_Center.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : ((GameState) this.handler).repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        ((GameState) this.handler).chosenUnitRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (ComsatStation b : ((GameState) this.handler).CSs) {
                if (UnitType.Terran_Comsat_Station.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : ((GameState) this.handler).repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        ((GameState) this.handler).chosenUnitRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (Starport b : ((GameState) this.handler).Ps) {
                if (UnitType.Terran_Starport.maxHitPoints() != b.getHitPoints()) {
                    for (Mechanical r : ((GameState) this.handler).repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        ((GameState) this.handler).chosenUnitRepair = b;
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
