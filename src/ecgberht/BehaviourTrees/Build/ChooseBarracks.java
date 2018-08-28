package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Barracks;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.Factory;

public class ChooseBarracks extends Action {

    public ChooseBarracks(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if ((((GameState) this.handler).strat.name.equals("BioGreedyFE") ||
                    ((GameState) this.handler).strat.name.equals("MechGreedyFE") ||
                    ((GameState) this.handler).strat.name.equals("BioMechGreedyFE")) &&
                    ((GameState) this.handler).countBuildingAll(UnitType.Terran_Command_Center) == 1 &&
                    ((GameState) this.handler).countBuildingAll(UnitType.Terran_Barracks) > 1 &&
                    ((GameState) this.handler).frameCount <= 24 * 240) {
                return State.FAILURE;
            }

            if (!((GameState) this.handler).strat.name.equals("ProxyBBS")) {
                if (!((GameState) this.handler).MBs.isEmpty() && ((GameState) this.handler).countBuildingAll(UnitType.Terran_Barracks) == ((GameState) this.handler).strat.numRaxForAca && ((GameState) this.handler).countBuildingAll(UnitType.Terran_Academy) == 0) {
                    return State.FAILURE;
                }
                if (((GameState) this.handler).countBuildingAll(UnitType.Terran_Barracks) == ((GameState) this.handler).strat.numRaxForAca && ((GameState) this.handler).countBuildingAll(UnitType.Terran_Refinery) == 0) {
                    return State.FAILURE;
                }
            } else {
                if (((GameState) this.handler).getPlayer().supplyUsed() < 16) return State.FAILURE;
            }
            if (((GameState) this.handler).strat.buildUnits.contains(UnitType.Terran_Factory)) {
                int count = 0;
                boolean found = false;
                for (MutablePair<UnitType, TilePosition> w : ((GameState) this.handler).workerBuild.values()) {
                    if (w.first == UnitType.Terran_Barracks) count++;
                    if (w.first == UnitType.Terran_Factory) found = true;
                }
                for (Building w : ((GameState) this.handler).workerTask.values()) {
                    if (w instanceof Barracks) count++;
                    if (w instanceof Factory) found = true;
                }
                if (!((GameState) this.handler).Fs.isEmpty()) found = true;
                if (count + ((GameState) this.handler).MBs.size() > ((GameState) this.handler).strat.numRaxForFac && !found) {
                    return State.FAILURE;
                }
            }
            if (((GameState) this.handler).countBuildingAll(UnitType.Terran_Barracks) == ((GameState) this.handler).MBs.size()
                    && ((GameState) this.handler).getPlayer().minerals() >= 600) {
                ((GameState) this.handler).chosenToBuild = UnitType.Terran_Barracks;
                return State.SUCCESS;
            }
            if (((GameState) this.handler).countBuildingAll(UnitType.Terran_Barracks) < ((GameState) this.handler).strat.raxPerCC * ((GameState) this.handler).CCs.size()) {
                ((GameState) this.handler).chosenToBuild = UnitType.Terran_Barracks;
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
