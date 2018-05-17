package ecgberht.Expansion;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.Worker;
import org.openbw.bwapi4j.util.Pair;

public class Expand extends Action {

    public Expand(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Worker chosen = ((GameState) this.handler).chosenBuilderBL;

            if (!chosen.build(((GameState) this.handler).chosenBaseLocation, UnitType.Terran_Command_Center)) {
                ((GameState) this.handler).movingToExpand = false;
                ((GameState) this.handler).expanding = false;
                ((GameState) this.handler).checkUnitsBL(((GameState) this.handler).chosenBaseLocation, chosen);
                ((GameState) this.handler).chosenBaseLocation = null;
                ((GameState) this.handler).workerIdle.add(((GameState) this.handler).chosenBuilderBL);
                ((GameState) this.handler).chosenBuilderBL.stop(false);
                ((GameState) this.handler).chosenBuilderBL = null;
                ((GameState) this.handler).deltaCash.first -= UnitType.Terran_Command_Center.mineralPrice();
                ((GameState) this.handler).deltaCash.second -= UnitType.Terran_Command_Center.gasPrice();

                return State.FAILURE;
            }
            ((GameState) this.handler).movingToExpand = false;
            ((GameState) this.handler).workerBuild.put((SCV) chosen, new Pair<UnitType, TilePosition>(UnitType.Terran_Command_Center, ((GameState) this.handler).chosenBaseLocation));
            ((GameState) this.handler).expanding = false;
            ((GameState) this.handler).chosenBaseLocation = null;
            ((GameState) this.handler).chosenBuilderBL = null;
            return State.SUCCESS;

        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            System.err.println(e);
            return State.ERROR;
        }
    }

}
