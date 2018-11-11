package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.CommandCenter;

public class ChooseExpand extends Action {

    public ChooseExpand(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            String strat = this.handler.strat.name;
            if (strat.equals("ProxyBBS") || strat.equals("ProxyEightRax")) return State.FAILURE;
            for (MutablePair<UnitType, TilePosition> w : this.handler.workerBuild.values()) {
                if (w.first == UnitType.Terran_Command_Center) return State.FAILURE;
            }
            for (Building w : this.handler.workerTask.values()) {
                if (w instanceof CommandCenter) return State.FAILURE;
            }
            if (strat.equals("PlasmaWraithHell") && Util.countUnitTypeSelf(UnitType.Terran_Command_Center) > 2) {
                return State.FAILURE;
            }
            if (this.handler.iReallyWantToExpand || this.handler.getCash().first >= 550) {
                this.handler.chosenToBuild = UnitType.Terran_Command_Center;
                return State.SUCCESS;
            }
            if ((strat.equals("BioGreedyFE") || strat.equals("MechGreedyFE") || strat.equals("BioMechGreedyFE") ||
                    strat.equals("PlasmaWraithHell")) && !this.handler.MBs.isEmpty() && this.handler.CCs.size() == 1) {
                this.handler.chosenToBuild = UnitType.Terran_Command_Center;
                return State.SUCCESS;
            }
            int workers = this.handler.workerIdle.size();
            for (Integer wt : this.handler.mineralsAssigned.values()) workers += wt;
            if (this.handler.mineralsAssigned.size() * 2 <= workers - 1 &&
                    this.handler.getArmySize() >= this.handler.strat.armyForExpand) {
                this.handler.chosenToBuild = UnitType.Terran_Command_Center;
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
