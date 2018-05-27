package ecgberht.Expansion;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.CommandCenter;
import org.openbw.bwapi4j.util.Pair;

public class CheckExpansion extends Conditional {

    public CheckExpansion(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).defense && ((GameState) this.handler).expanding) {
                ((GameState) this.handler).chosenBaseLocation = null;
                ((GameState) this.handler).movingToExpand = false;
                if (((GameState) this.handler).chosenBuilderBL != null) {
                    ((GameState) this.handler).chosenBuilderBL.stop(false);
                    ((GameState) this.handler).workerIdle.add(((GameState) this.handler).chosenBuilderBL);
                    ((GameState) this.handler).chosenBuilderBL = null;
                }
                ((GameState) this.handler).expanding = false;
                ((GameState) this.handler).deltaCash.first -= UnitType.Terran_Command_Center.mineralPrice();
                ((GameState) this.handler).deltaCash.second -= UnitType.Terran_Command_Center.gasPrice();
                return State.FAILURE;
            }
            if (((GameState) this.handler).expanding) {
                return State.SUCCESS;
            }
            for (Pair<UnitType, TilePosition> w : ((GameState) this.handler).workerBuild.values()) {
                if (w.first == UnitType.Terran_Command_Center) {
                    return State.FAILURE;
                }
            }
            for (Building w : ((GameState) this.handler).workerTask.values()) {
                if (w instanceof CommandCenter) {
                    return State.FAILURE;
                }
            }
            int workers = 0;
            for (Integer wt : ((GameState) this.handler).mineralsAssigned.values()) {
                workers += wt;
            }
            if (((GameState) this.handler).mineralsAssigned.size() * 2 <= workers && ((GameState) this.handler).getArmySize() >= ((GameState) this.handler).strat.armyForExpand) {
                return State.SUCCESS;
            }
            if (((GameState) this.handler).iReallyWantToExpand) return State.SUCCESS;

            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }

}
