package ecgberht.Recollection;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Pair;
import bwapi.Unit;
import ecgberht.GameState;

public class CollectGas extends Action {

	public CollectGas(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).getPlayer().gas() >= 400) {
				return State.FAILURE;
			}
			Unit chosen = ((GameState)this.handler).chosenWorker;
			if(!((GameState)this.handler).refineriesAssigned.isEmpty()) {
				Unit closestGeyser = null;
				int index = 0;
				int count = 0;
				for (Pair<Pair<Unit,Integer>,Boolean> g : ((GameState)this.handler).refineriesAssigned) {
					if ((closestGeyser == null || chosen.getDistance(g.first.first) < chosen.getDistance(closestGeyser)) && g.first.second < 3 && ((GameState)this.handler).mining > 3 && g.second) {
						closestGeyser = g.first.first;
						index = count;
					}
					count++;
				}
				if (closestGeyser != null) {
					((GameState)this.handler).refineriesAssigned.get(index).first.second++;
					((GameState)this.handler).workerIdle.remove(chosen);
					((GameState)this.handler).workerTask.add(new Pair<Unit, Unit>(chosen, closestGeyser));
					chosen.gather(closestGeyser, false);
					((GameState)this.handler).chosenWorker = null;
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
