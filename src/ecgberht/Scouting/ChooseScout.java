package ecgberht.Scouting;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Pair;
import bwapi.Unit;
import ecgberht.GameState;

public class ChooseScout extends Action {

	public ChooseScout(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			for (Unit u : ((GameState)this.handler).workerIdle) {
				((GameState)this.handler).chosenScout = u;
				((GameState)this.handler).workerIdle.remove(u);
				break;
			}
			if(((GameState)this.handler).chosenScout == null) {
				for (Pair<Unit,Unit> u : ((GameState)this.handler).workerTask) {
					if(u.second.getType().isMineralField() && !u.first.isCarryingMinerals()) {
						((GameState)this.handler).chosenScout = u.first;
						((GameState)this.handler).workerTask.remove(u);
					}
					break;
				}
			}
			if(((GameState)this.handler).chosenScout != null) {
				return State.SUCCESS;
			}
			return State.FAILURE;
		} catch(Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
}
