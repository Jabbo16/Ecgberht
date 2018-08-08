package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.CommandCenter;

public class ChooseExpand extends Action {

    public ChooseExpand(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            for (MutablePair<UnitType, TilePosition> w : ((GameState) this.handler).workerBuild.values()) {
                if (w.first == UnitType.Terran_Command_Center) {
                    return State.FAILURE;
                }
            }
            for (Building w : ((GameState) this.handler).workerTask.values()) {
                if (w instanceof CommandCenter) {
                    return State.FAILURE;
                }
            }
            if (((GameState) this.handler).strat.name.equals("PlasmaWraithHell") && Util.countUnitTypeSelf(UnitType.Terran_Command_Center) > 2) {
                return State.FAILURE;
            }

            if (((GameState) this.handler).iReallyWantToExpand || ((GameState) this.handler).getCash().first >= 500) {
                ((GameState) this.handler).chosenToBuild = UnitType.Terran_Command_Center;
                return State.SUCCESS;
            }
            if (((GameState) this.handler).strat.name.equals("BioGreedyFE") ||
                    ((GameState) this.handler).strat.name.equals("MechGreedyFE") ||
                    ((GameState) this.handler).strat.name.equals("BioMechGreedyFE") ||
                    ((GameState) this.handler).strat.name.equals("PlasmaWraithHell")) {
                if (!((GameState) this.handler).MBs.isEmpty() && ((GameState) this.handler).CCs.size() == 1) {
                    ((GameState) this.handler).chosenToBuild = UnitType.Terran_Command_Center;
                    return State.SUCCESS;
                }
            }
            int workers = 0;
            for (Integer wt : ((GameState) this.handler).mineralsAssigned.values()) {
                workers += wt;
            }
            if (((GameState) this.handler).mineralsAssigned.size() * 2 <= workers &&
                    ((GameState) this.handler).getArmySize() >= ((GameState) this.handler).strat.armyForExpand) {
                ((GameState) this.handler).chosenToBuild = UnitType.Terran_Command_Center;
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
