package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.TrainingFacility;

public class TrainUnit extends Action {

    public TrainUnit(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).chosenUnit == UnitType.None) return State.FAILURE;
           /* if (((GameState) this.handler).strat.techToResearch.contains(TechType.Tank_Siege_Mode)) {
                boolean mShop = false;
                for (ResearchingFacility u : ((GameState) this.handler).UBs) {
                    if (u instanceof MachineShop) {
                        mShop = true;
                        break;
                    }
                }
                if (!((GameState) this.handler).Fs.isEmpty() && mShop && (!((GameState) this.handler).getPlayer().isResearching(TechType.Tank_Siege_Mode) &&
                        !((GameState) this.handler).getPlayer().hasResearched(TechType.Tank_Siege_Mode)) && ((GameState) this.handler).vulturesTrained >= 2) {
                    ((GameState) this.handler).chosenBuilding = null;
                    ((GameState) this.handler).chosenToBuild = null;
                    return State.FAILURE;
                }
            }*/
            TrainingFacility chosen = ((GameState) this.handler).chosenBuilding;
            if (((GameState) this.handler).strat.name.equals("ProxyBBS")) {
                if (Util.countBuildingAll(UnitType.Terran_Barracks) == 2 &&
                        Util.countBuildingAll(UnitType.Terran_Supply_Depot) == 0) {
                    ((GameState) this.handler).chosenBuilding = null;
                    ((GameState) this.handler).chosenToBuild = UnitType.None;
                    return State.FAILURE;
                }
                if (((GameState) this.handler).getSupply() > 0) {
                    chosen.train(((GameState) this.handler).chosenUnit);
                    return State.SUCCESS;
                }
            }
            if (((GameState) this.handler).strat.name.equals("EightRax")) {
                if (Util.countBuildingAll(UnitType.Terran_Barracks) == 0 &&
                        ((GameState) this.handler).supplyMan.getSupplyUsed() >= 16) {
                    ((GameState) this.handler).chosenBuilding = null;
                    ((GameState) this.handler).chosenToBuild = UnitType.None;
                    return State.FAILURE;
                }
                /*if (Util.countBuildingAll(UnitType.Terran_Barracks) == 1 &&
                        Util.countBuildingAll(UnitType.Terran_Supply_Depot) == 0) {
                    ((GameState) this.handler).chosenBuilding = null;
                    ((GameState) this.handler).chosenToBuild = UnitType.None;
                    return State.FAILURE;
                }*/
                if (((GameState) this.handler).getSupply() > 0) {
                    chosen.train(((GameState) this.handler).chosenUnit);
                    return State.SUCCESS;
                }
            }

            if (((GameState) this.handler).getSupply() > 4 || ((GameState) this.handler).checkSupply() ||
                    ((GameState) this.handler).getPlayer().supplyTotal() >= 400) {
                if (!((GameState) this.handler).defense && ((GameState) this.handler).chosenToBuild == UnitType.Terran_Command_Center) {
                    boolean found = false;
                    for (MutablePair<UnitType, TilePosition> w : ((GameState) this.handler).workerBuild.values()) {
                        if (w.first == UnitType.Terran_Command_Center) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        ((GameState) this.handler).chosenBuilding = null;
                        ((GameState) this.handler).chosenToBuild = UnitType.None;
                        return State.FAILURE;
                    }
                }
                chosen.train(((GameState) this.handler).chosenUnit);
                return State.SUCCESS;
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
