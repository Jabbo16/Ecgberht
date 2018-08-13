package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Barracks;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.Refinery;
import org.openbw.bwapi4j.unit.VespeneGeyser;

import java.util.Map.Entry;

public class ChooseRefinery extends Action {

    public ChooseRefinery(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            String strat = ((GameState) this.handler).strat.name;
            if (((GameState) this.handler).getPlayer().supplyUsed() < ((GameState) this.handler).strat.supplyForFirstRefinery || ((GameState) this.handler).getCash().second >= 300) {
                return State.FAILURE;
            }
            if ((strat.equals("BioGreedyFE") || strat.equals("FullBio") || strat.equals("FullBioFE"))
                    && ((GameState) this.handler).getCash().second >= 150) {
                return State.FAILURE;
            }
            if (((GameState) this.handler).refineriesAssigned.size() == 1) {
                boolean found = false;
                for (MutablePair<UnitType, TilePosition> w : ((GameState) this.handler).workerBuild.values()) {
                    if (w.first == UnitType.Terran_Barracks) {
                        found = true;
                        break;
                    }
                }
                for (Building w : ((GameState) this.handler).workerTask.values()) {
                    if (w instanceof Barracks) {
                        found = true;
                        break;
                    }
                }
                if (((GameState) this.handler).MBs.isEmpty() && !found) return State.FAILURE;
            }
            int count = 0;
            VespeneGeyser geyser = null;
            for (Entry<VespeneGeyser, Boolean> r : ((GameState) this.handler).vespeneGeysers.entrySet()) {
                if (r.getValue()) {
                    count++;
                } else geyser = r.getKey();
            }
            if (count == ((GameState) this.handler).vespeneGeysers.size()) return State.FAILURE;
            for (MutablePair<UnitType, TilePosition> w : ((GameState) this.handler).workerBuild.values()) {
                if (w.first == UnitType.Terran_Refinery) return State.FAILURE;
            }
            for (Building w : ((GameState) this.handler).workerTask.values()) {
                if (w instanceof Refinery && w.getTilePosition().equals(geyser.getTilePosition())) return State.FAILURE;
            }
            if ((strat.equals("BioGreedyFE") || strat.equals("MechGreedyFE") || strat.equals("BioMechGreedyFE")) &&
                    !((GameState) this.handler).refineriesAssigned.isEmpty()
                    && ((GameState) this.handler).CCs.size() <= 2 && Util.countUnitTypeSelf(UnitType.Terran_SCV) < 30) {
                return State.FAILURE;
            }
            ((GameState) this.handler).chosenToBuild = UnitType.Terran_Refinery;
            return State.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
