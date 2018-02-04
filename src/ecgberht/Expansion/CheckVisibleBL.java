package ecgberht.Expansion;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Unit;
import bwapi.UnitType;
import ecgberht.GameState;

public class CheckVisibleBL extends Action {

	public CheckVisibleBL(String name, GameHandler gh) {
		super(name, gh);

	}

	@Override
	public State execute() {
		try {
			for(Unit u : ((GameState)this.handler).getGame().enemy().getUnits()) {
				if(u.getDistance(((GameState)this.handler).chosenBaseLocation.toPosition()) < 300) {
					((GameState)this.handler).chosenBaseLocation = null;
					((GameState)this.handler).movingToExpand = false;
					((GameState)this.handler).chosenBuilderBL.stop();
					((GameState)this.handler).workerIdle.add(((GameState)this.handler).chosenBuilderBL);
					((GameState)this.handler).chosenBuilderBL = null;
					((GameState)this.handler).expanding = false;
					((GameState)this.handler).deltaCash.first -= UnitType.Terran_Command_Center.mineralPrice();
					((GameState)this.handler).deltaCash.second -= UnitType.Terran_Command_Center.gasPrice();
					return State.FAILURE;
				}
			}
			if(((GameState)this.handler).getGame().isVisible(((GameState)this.handler).chosenBaseLocation)) {
				if(((GameState)this.handler).chosenBuilderBL.getDistance(((GameState)this.handler).chosenBaseLocation.toPosition()) < 10) {
					return State.SUCCESS;
				}
			}
			return State.FAILURE;
		} catch(Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
}
