package ecgberht.Recollection;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Unit;
import ecgberht.GameState;

public class FreeWorker extends Action {

	public FreeWorker(String name, GameHandler gh) {
		super(name, gh);

	}

	@Override
	public State execute() {
		try {
			if(!((GameState)this.handler).workerIdle.isEmpty()) {
				for(Unit w:((GameState)this.handler).workerIdle) {
					if(w.isIdle()) {
						((GameState)this.handler).chosenWorker = w;
						return State.SUCCESS;
					}
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