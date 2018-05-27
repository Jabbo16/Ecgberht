package ecgberht.Repair;


import ecgberht.GameState;
import ecgberht.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

public class CheckBuildingFlames extends Action {

    public CheckBuildingFlames(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            boolean isBeingRepaired = false;
            for (Bunker w : ((GameState) this.handler).DBs.keySet()) {
                int count = 0;
                if (UnitType.Terran_Bunker.maxHitPoints() != w.getHitPoints()) {
                    for (Building r : ((GameState) this.handler).repairerTask.values()) {
                        if (w.equals(r)) {
                            count++;
                        }
                    }
                    if (count < 2 && ((GameState) this.handler).defense) {
                        ((GameState) this.handler).chosenBuildingRepair = w;
                        return State.SUCCESS;
                    } else if (count == 0) {
                        ((GameState) this.handler).chosenBuildingRepair = w;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (MissileTurret b : ((GameState) this.handler).Ts) {
                if (UnitType.Terran_Missile_Turret.maxHitPoints() != b.getHitPoints()) {
                    for (Building r : ((GameState) this.handler).repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        ((GameState) this.handler).chosenBuildingRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (Barracks b : ((GameState) this.handler).MBs) {
                if (UnitType.Terran_Barracks.maxHitPoints() != b.getHitPoints()) {
                    for (Building r : ((GameState) this.handler).repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        ((GameState) this.handler).chosenBuildingRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (Factory b : ((GameState) this.handler).Fs) {
                if (UnitType.Terran_Factory.maxHitPoints() != b.getHitPoints()) {
                    for (Building r : ((GameState) this.handler).repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        ((GameState) this.handler).chosenBuildingRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (ResearchingFacility b : ((GameState) this.handler).UBs) {
                if (Util.getType((PlayerUnit) b).maxHitPoints() != ((PlayerUnit) b).getHitPoints()) {
                    for (Building r : ((GameState) this.handler).repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        ((GameState) this.handler).chosenBuildingRepair = (Building) b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (SupplyDepot b : ((GameState) this.handler).SBs) {
                if (UnitType.Terran_Supply_Depot.maxHitPoints() != b.getHitPoints()) {
                    for (Building r : ((GameState) this.handler).repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        ((GameState) this.handler).chosenBuildingRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (CommandCenter b : ((GameState) this.handler).CCs.values()) {
                if (UnitType.Terran_Command_Center.maxHitPoints() != b.getHitPoints()) {
                    for (Building r : ((GameState) this.handler).repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        ((GameState) this.handler).chosenBuildingRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (ComsatStation b : ((GameState) this.handler).CSs) {
                if (UnitType.Terran_Comsat_Station.maxHitPoints() != b.getHitPoints()) {
                    for (Building r : ((GameState) this.handler).repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        ((GameState) this.handler).chosenBuildingRepair = b;
                        return State.SUCCESS;
                    }
                }
            }
            isBeingRepaired = false;
            for (Starport b : ((GameState) this.handler).Ps) {
                if (UnitType.Terran_Starport.maxHitPoints() != b.getHitPoints()) {
                    for (Building r : ((GameState) this.handler).repairerTask.values()) {
                        if (b.equals(r)) {
                            isBeingRepaired = true;
                        }
                    }
                    if (!isBeingRepaired) {
                        ((GameState) this.handler).chosenBuildingRepair = b;
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
