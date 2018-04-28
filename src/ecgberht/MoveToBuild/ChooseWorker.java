package ecgberht.MoveToBuild;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.Worker;

import ecgberht.GameState;

public class ChooseWorker extends Action {

	public ChooseWorker(String name, GameHandler gh) {
		super(name, gh);

	}

	@Override
	public State execute() {
		try {
			Worker closestWorker = null;
			int frame = ((GameState)this.handler).frameCount;
			Position chosen = ((GameState)this.handler).chosenPosition.toPosition();
			if(!((GameState)this.handler).workerIdle.isEmpty()) {
				for (Worker u : ((GameState)this.handler).workerIdle) {
					if(u.getLastCommandFrame() == frame) {
						continue;
					}
					if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
						closestWorker = u;
					}
				}
			}
			if(!((GameState)this.handler).workerMining.isEmpty()) {
				for (Worker u : ((GameState)this.handler).workerMining.keySet()) {
					if(u.getLastCommandFrame() == frame) {
						continue;
					}
					if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen)) && !u.isCarryingMinerals()) {
						closestWorker = u;
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