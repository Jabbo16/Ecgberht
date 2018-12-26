package ecgberht.BehaviourTrees.Training;

import bwem.Base;
import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Barracks;
import org.openbw.bwapi4j.unit.CommandCenter;

import java.util.Map;

public class ChooseSCV extends Action {

    public ChooseSCV(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            String strat = this.handler.strat.name;
            if (strat.equals("ProxyBBS") || strat.equals("ProxyEightRax")) {
                boolean notTraining = false;
                for (Barracks b : this.handler.MBs) {
                    if (!b.isTraining()) {
                        notTraining = true;
                        break;
                    }
                }
                if (notTraining) return State.FAILURE;
            }
            if (this.handler.enemyRace == Race.Zerg && this.handler.learningManager.isNaughty()
                    && Util.countBuildingAll(UnitType.Terran_Barracks) > 0
                    && Util.countBuildingAll(UnitType.Terran_Bunker) < 1 && this.handler.getCash().first < 150) {
                return State.FAILURE;
            }
            if (Util.countUnitTypeSelf(UnitType.Terran_SCV) <= 65 && Util.countUnitTypeSelf(UnitType.Terran_SCV) <= this.handler.mineralsAssigned.size() * 2 + this.handler.refineriesAssigned.size() * 3 + this.handler.strat.extraSCVs && !this.handler.CCs.isEmpty()) {
                for (Map.Entry<Base, CommandCenter> b : this.handler.islandCCs.entrySet()) {
                    if (!b.getValue().isTraining() && !b.getValue().isBuildingAddon() && Util.hasFreePatches(b.getKey())) {
                        this.handler.chosenUnit = UnitType.Terran_SCV;
                        this.handler.chosenBuilding = b.getValue();
                        return State.SUCCESS;
                    }
                }
                for (Map.Entry<Base, CommandCenter> b : this.handler.CCs.entrySet()) {
                    if (!b.getValue().isTraining() && !b.getValue().isBuildingAddon() && Util.hasFreePatches(b.getKey())) {
                        this.handler.chosenUnit = UnitType.Terran_SCV;
                        this.handler.chosenBuilding = b.getValue();
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
