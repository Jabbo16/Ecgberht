package ecgberht.Recollection;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Pair;
import bwapi.Unit;
import ecgberht.GameState;

public class CollectMineral extends Action {

	public CollectMineral(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			Unit chosen = ((GameState)this.handler).chosenWorker;
			if(!((GameState)this.handler).mineralsAssigned.isEmpty()) {
				Unit closestMineral = null;
				int index = 0;
				int count = 0;
				for (Pair<Unit,Integer> m : ((GameState)this.handler).mineralsAssigned) {
					if ((closestMineral == null || chosen.getDistance(m.first) < chosen.getDistance(closestMineral)) && m.second < 2) {
						closestMineral = m.first;
						index = count;
					}
					count++;
				}
				if (closestMineral != null) {
					((GameState)this.handler).mineralsAssigned.get(index).second++;
					((GameState)this.handler).workerIdle.remove(chosen);
					((GameState)this.handler).workerTask.add(new Pair<Unit, Unit>(chosen, closestMineral));
					chosen.gather(closestMineral, false);
					((GameState)this.handler).chosenWorker = null;
					((GameState)this.handler).mining++;
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
