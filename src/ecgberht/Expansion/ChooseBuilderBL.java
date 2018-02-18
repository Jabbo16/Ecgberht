package ecgberht.Expansion;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Pair;
import bwapi.TilePosition;
import bwapi.Unit;
import ecgberht.GameState;

public class ChooseBuilderBL extends Action {

	public ChooseBuilderBL(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).chosenBuilderBL != null) {
				return State.SUCCESS;
			}
			Unit closestWorker = null;
			TilePosition chosen = ((GameState)this.handler).chosenBaseLocation.getTilePosition();
			if(!((GameState)this.handler).workerIdle.isEmpty()) {
				for (Unit u : ((GameState)this.handler).workerIdle) {
					if ((closestWorker == null || u.getDistance(chosen.toPosition()) < closestWorker.getDistance(chosen.toPosition()))) {
						closestWorker = u;
					}
				}
			}
			if(!((GameState)this.handler).workerTask.isEmpty()) {
				for (Pair<Unit,Unit> u : ((GameState)this.handler).workerTask) {
					if ((closestWorker == null || u.first.getDistance(chosen.toPosition()) < closestWorker.getDistance(chosen.toPosition())) && u.second.getType().isMineralField() && !u.first.isCarryingMinerals()) {
						closestWorker = u.first;
					}
				}
			}
			if(closestWorker != null) {
				if(!((GameState)this.handler).workerTask.isEmpty() && ((GameState)this.handler).workerIdle.contains(closestWorker)) {
					((GameState)this.handler).workerIdle.remove(closestWorker);
				} else if(!((GameState)this.handler).workerTask.isEmpty()) {
					for(Pair<Unit,Unit> u:((GameState)this.handler).workerTask) {
						if(u.first.equals(closestWorker)) {
							((GameState)this.handler).workerTask.remove(u);
							for(Pair<Unit,Integer> m:((GameState)this.handler).mineralsAssigned) {
								if(m.first.equals(u.second)) {
									((GameState)this.handler).mining--;
									((GameState)this.handler).mineralsAssigned.get(((GameState)this.handler).mineralsAssigned.indexOf(m)).second--;
								}
							}
							break;
						}
					}
				}
				if(((GameState)this.handler).chosenWorker != null && ((GameState)this.handler).chosenWorker.equals(closestWorker)) {
					((GameState)this.handler).chosenWorker = null;
				}
				((GameState)this.handler).chosenBuilderBL = closestWorker;
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