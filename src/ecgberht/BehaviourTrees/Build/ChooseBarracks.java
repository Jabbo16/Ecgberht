package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Barracks;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.Factory;

public class ChooseBarracks extends Action {

    public ChooseBarracks(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if ((this.handler.strat.name.equals("BioGreedyFE") ||
                    this.handler.strat.name.equals("MechGreedyFE") ||
                    this.handler.strat.name.equals("BioMechGreedyFE")) &&
                    Util.countBuildingAll(UnitType.Terran_Command_Center) == 1 &&
                    Util.countBuildingAll(UnitType.Terran_Barracks) > 1 &&
                    this.handler.frameCount <= 24 * 240) {
                return State.FAILURE;
            }
            if (this.handler.learningManager.isNaughty() && this.handler.enemyRace == Race.Zerg
                    && Util.countBuildingAll(UnitType.Terran_Barracks) == 1
                    && Util.countBuildingAll(UnitType.Terran_Bunker) < 1) {
                return State.FAILURE;
            }
            if (!this.handler.strat.name.equals("ProxyBBS") && !this.handler.strat.name.equals("ProxyEightRax")) {
                if (!this.handler.MBs.isEmpty() && Util.countBuildingAll(UnitType.Terran_Barracks) == this.handler.strat.numRaxForAca && Util.countBuildingAll(UnitType.Terran_Academy) == 0) {
                    return State.FAILURE;
                }
                if (Util.countBuildingAll(UnitType.Terran_Barracks) == this.handler.strat.numRaxForAca && Util.countBuildingAll(UnitType.Terran_Refinery) == 0) {
                    return State.FAILURE;
                }
            } else if (this.handler.getPlayer().supplyUsed() < 16) return State.FAILURE;
            if (this.handler.strat.buildUnits.contains(UnitType.Terran_Factory)) {
                int count = 0;
                boolean found = false;
                for (MutablePair<UnitType, TilePosition> w : this.handler.workerBuild.values()) {
                    if (w.first == UnitType.Terran_Barracks) count++;
                    if (w.first == UnitType.Terran_Factory) found = true;
                }
                for (Building w : this.handler.workerTask.values()) {
                    if (w instanceof Barracks) count++;
                    if (w instanceof Factory) found = true;
                }
                if (!this.handler.Fs.isEmpty()) found = true;
                if (count + this.handler.MBs.size() > this.handler.strat.numRaxForFac && !found) return State.FAILURE;
            }
            if (Util.countBuildingAll(UnitType.Terran_Academy) == 0 && Util.countBuildingAll(UnitType.Terran_Barracks) >= 2) {
                return State.FAILURE;
            }
            if (Util.countBuildingAll(UnitType.Terran_Barracks) == this.handler.MBs.size()
                    && this.handler.getPlayer().minerals() >= 600) {
                this.handler.chosenToBuild = UnitType.Terran_Barracks;
                return State.SUCCESS;
            }
            if (Util.countBuildingAll(UnitType.Terran_Barracks) < this.handler.strat.raxPerCC * Util.getNumberCCs()) {
                this.handler.chosenToBuild = UnitType.Terran_Barracks;
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
