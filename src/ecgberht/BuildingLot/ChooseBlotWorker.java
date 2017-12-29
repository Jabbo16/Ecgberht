package ecgberht.BuildingLot;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;

import bwapi.Pair;
import bwapi.Position;
import bwapi.Unit;

public class ChooseBlotWorker extends Action {

	public ChooseBlotWorker(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			Unit closestWorker = null;
			Position chosen = ((GameState)this.handler).chosenBuildingLot.getPosition();
			if(!((GameState)this.handler).workerIdle.isEmpty()) {
				for (Unit u : ((GameState)this.handler).workerIdle) {
					if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
						closestWorker = u;
					}
				}
			}
			if(!((GameState)this.handler).workerTask.isEmpty()) {
				for (Pair<Unit,Unit> u : ((GameState)this.handler).workerTask) {
					if ((closestWorker == null || u.first.getDistance(chosen) < closestWorker.getDistance(chosen)) && u.second.getType().isNeutral()) {
						closestWorker = u.first;
					}
				}
			}
			if(closestWorker != null) {
				((GameState)this.handler).chosenWorker = closestWorker;
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
