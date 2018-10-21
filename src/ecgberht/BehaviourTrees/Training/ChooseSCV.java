package ecgberht.BehaviourTrees.Training;

import bwem.Base;
import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Barracks;
import org.openbw.bwapi4j.unit.CommandCenter;

import java.util.Map;

public class ChooseSCV extends Action {

    public ChooseSCV(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            String strat = ((GameState) this.handler).strat.name;
            if (strat.equals("ProxyBBS") || strat.equals("EightRax")) {
                boolean notTraining = false;
                for (Barracks b : ((GameState) this.handler).MBs) {
                    if (!b.isTraining()) {
                        notTraining = true;
                        break;
                    }
                }
                if (notTraining) return State.FAILURE;
            }
            if (((GameState) this.handler).enemyRace == Race.Zerg && ((GameState) this.handler).EI.naughty) {
                if (Util.countBuildingAll(UnitType.Terran_Barracks) > 0 && Util.countBuildingAll(UnitType.Terran_Bunker) < 1 && ((GameState) this.handler).getCash().first < 150) {
                    return State.FAILURE;
                }
            }
            if (Util.countUnitTypeSelf(UnitType.Terran_SCV) <= 65 && Util.countUnitTypeSelf(UnitType.Terran_SCV) < ((GameState) this.handler).mineralsAssigned.size() * 2 + ((GameState) this.handler).refineriesAssigned.size() * 3 + 2 && !((GameState) this.handler).CCs.isEmpty()) {
                for (Map.Entry<Base, CommandCenter> b : ((GameState) this.handler).islandCCs.entrySet()) {
                    if (!b.getValue().isTraining() && !b.getValue().isBuildingAddon() && Util.hasFreePatches(b.getKey())) {
                        ((GameState) this.handler).chosenUnit = UnitType.Terran_SCV;
                        ((GameState) this.handler).chosenBuilding = b.getValue();
                        return State.SUCCESS;
                    }
                }
                for (Map.Entry<Base, CommandCenter> b : ((GameState) this.handler).CCs.entrySet()) { // TODO train only island scvs from island CCs, test
                    if (!b.getValue().isTraining() && !b.getValue().isBuildingAddon() && Util.hasFreePatches(b.getKey())) {
                        ((GameState) this.handler).chosenUnit = UnitType.Terran_SCV;
                        ((GameState) this.handler).chosenBuilding = b.getValue();
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
