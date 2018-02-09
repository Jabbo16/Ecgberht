package ecgberht.Repair;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Pair;
import bwapi.Position;
import bwapi.Unit;
import ecgberht.GameState;

public class ChooseRepairer extends Action {

	public ChooseRepairer(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			Unit closestWorker = null;
			Position chosen = ((GameState)this.handler).chosenBuildingRepair.getPosition();
			for (Unit u : ((GameState)this.handler).workerIdle) {
				if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
					closestWorker = u;
				}
			}
			for (Pair<Unit,Unit> u : ((GameState)this.handler).workerTask) {
				if ((closestWorker == null || u.first.getDistance(chosen) < closestWorker.getDistance(chosen)) && u.second.getType().isMineralField() && !u.first.isCarryingMinerals()) {
					closestWorker = u.first;
				}
			}
			if(closestWorker != null) {
				((GameState)this.handler).chosenRepairer = closestWorker;
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