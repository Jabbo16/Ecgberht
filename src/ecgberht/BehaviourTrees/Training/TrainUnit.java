package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.TrainingFacility;

public class TrainUnit extends Action {

    public TrainUnit(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (this.handler.chosenUnit == UnitType.None) return State.FAILURE;
            TrainingFacility chosen = this.handler.chosenBuilding;
            if (this.handler.strat.name.equals("ProxyBBS")) {
                if (Util.countBuildingAll(UnitType.Terran_Barracks) == 2 &&
                        Util.countBuildingAll(UnitType.Terran_Supply_Depot) == 0) {
                    this.handler.chosenBuilding = null;
                    this.handler.chosenToBuild = UnitType.None;
                    return State.FAILURE;
                }
                if (this.handler.getSupply() > 0) {
                    chosen.train(this.handler.chosenUnit);
                    return State.SUCCESS;
                }
            }
            if (this.handler.strat.name.equals("ProxyEightRax")) {
                if (Util.countBuildingAll(UnitType.Terran_Barracks) == 0 &&
                        this.handler.supplyMan.getSupplyUsed() >= 16) {
                    this.handler.chosenBuilding = null;
                    this.handler.chosenToBuild = UnitType.None;
                    return State.FAILURE;
                }
                if (this.handler.getSupply() > 0) {
                    chosen.train(this.handler.chosenUnit);
                    return State.SUCCESS;
                }
            }
            if (this.handler.getSupply() > 4 || this.handler.checkSupply() || this.handler.getPlayer().supplyTotal() >= 400) {
                if (!this.handler.defense && this.handler.chosenToBuild == UnitType.Terran_Command_Center) {
                    boolean found = false;
                    for (MutablePair<UnitType, TilePosition> w : this.handler.workerBuild.values()) {
                        if (w.first == UnitType.Terran_Command_Center) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        this.handler.chosenBuilding = null;
                        this.handler.chosenUnit = UnitType.None;
                        return State.FAILURE;
                    }
                }
                chosen.train(this.handler.chosenUnit);
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
