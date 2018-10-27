package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Barracks;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.Refinery;
import org.openbw.bwapi4j.unit.VespeneGeyser;

import java.util.Map.Entry;

public class ChooseRefinery extends Action {

    public ChooseRefinery(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            String strat = this.handler.strat.name;
            if (this.handler.getPlayer().supplyUsed() < this.handler.strat.supplyForFirstRefinery || this.handler.getCash().second >= 300) {
                return BehavioralTree.State.FAILURE;
            }
            if ((strat.equals("BioGreedyFE") || strat.equals("FullBio") || strat.equals("FullBioFE"))
                    && this.handler.getCash().second >= 150) {
                return BehavioralTree.State.FAILURE;
            }
            if (this.handler.refineriesAssigned.size() == 1) {
                boolean found = false;
                for (MutablePair<UnitType, TilePosition> w : this.handler.workerBuild.values()) {
                    if (w.first == UnitType.Terran_Barracks) {
                        found = true;
                        break;
                    }
                }
                for (Building w : this.handler.workerTask.values()) {
                    if (w instanceof Barracks) {
                        found = true;
                        break;
                    }
                }
                if (this.handler.MBs.isEmpty() && !found) return BehavioralTree.State.FAILURE;
            }
            int count = 0;
            VespeneGeyser geyser = null;
            for (Entry<VespeneGeyser, Boolean> r : this.handler.vespeneGeysers.entrySet()) {
                if (r.getValue()) {
                    count++;
                } else geyser = r.getKey();
            }
            if (count == this.handler.vespeneGeysers.size()) return BehavioralTree.State.FAILURE;
            for (MutablePair<UnitType, TilePosition> w : this.handler.workerBuild.values()) {
                if (w.first == UnitType.Terran_Refinery) return BehavioralTree.State.FAILURE;
            }
            for (Building w : this.handler.workerTask.values()) {
                if (w instanceof Refinery && w.getTilePosition().equals(geyser.getTilePosition())) return BehavioralTree.State.FAILURE;
            }
            if ((strat.equals("BioGreedyFE") || strat.equals("MechGreedyFE") || strat.equals("BioMechGreedyFE")) &&
                    !this.handler.refineriesAssigned.isEmpty()
                    && Util.getNumberCCs() <= 2 && Util.countUnitTypeSelf(UnitType.Terran_SCV) < 30) {
                return BehavioralTree.State.FAILURE;
            }
            this.handler.chosenToBuild = UnitType.Terran_Refinery;
            return BehavioralTree.State.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}
