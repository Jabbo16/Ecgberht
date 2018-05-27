package ecgberht.Expansion;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.UnitType;

public class SendBuilderBL extends Action {

    public SendBuilderBL(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {

            if (((GameState) this.handler).movingToExpand) {
                return State.SUCCESS;
            }
            if (((GameState) this.handler).chosenBuilderBL.move(((GameState) this.handler).chosenBaseLocation.toPosition())) {
                ((GameState) this.handler).movingToExpand = true;
                ((GameState) this.handler).moveUnitFromChokeWhenExpand();
                return State.SUCCESS;
            }
            ((GameState) this.handler).movingToExpand = false;
            ((GameState) this.handler).expanding = false;
            ((GameState) this.handler).chosenBaseLocation = null;
            ((GameState) this.handler).workerIdle.add(((GameState) this.handler).chosenBuilderBL);
            ((GameState) this.handler).chosenBuilderBL.stop(false);
            ((GameState) this.handler).chosenBuilderBL = null;
            ((GameState) this.handler).deltaCash.first -= UnitType.Terran_Command_Center.mineralPrice();
            ((GameState) this.handler).deltaCash.second -= UnitType.Terran_Command_Center.gasPrice();

            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }

}