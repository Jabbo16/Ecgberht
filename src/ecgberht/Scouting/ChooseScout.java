package ecgberht.Scouting;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;

import bwapi.Pair;
import bwapi.Unit;

public class ChooseScout extends Action {

	public ChooseScout(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			for (Unit u : ((GameState)this.handler).workerIdle) {
				((GameState)this.handler).choosenScout = u;
				((GameState)this.handler).workerIdle.remove(u);
				break;
			}
			if(((GameState)this.handler).choosenScout == null) {
				for (Pair<Unit,Unit> u : ((GameState)this.handler).workerTask) {
					if(u.second.getType().isNeutral()) {
						((GameState)this.handler).choosenScout = u.first;
						((GameState)this.handler).workerTask.remove(u);
					}
					break;
				}
			}
			if(((GameState)this.handler).choosenScout != null) {
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
