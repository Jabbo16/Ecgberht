package ecgberht.BuildingLot;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Pair;
import bwapi.Unit;
import ecgberht.GameState;

public class FinishBuilding extends Action {

	public FinishBuilding(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			Unit chosen = ((GameState)this.handler).chosenWorker;
			if(chosen.rightClick(((GameState)this.handler).chosenBuildingLot)) {
				if(((GameState)this.handler).workerIdle.contains(chosen)) {
					((GameState)this.handler).workerIdle.remove(chosen);
				} else {
					for(Pair<Unit,Unit> u:((GameState)this.handler).workerTask) {
						if(u.first.equals(chosen)) {
							((GameState)this.handler).workerTask.remove(u);
							if(((GameState)this.handler).mineralsAssigned.containsKey(u.second)) {
								((GameState)this.handler).mining--;
								((GameState)this.handler).mineralsAssigned.put(u.second, ((GameState)this.handler).mineralsAssigned.get(u.second) - 1);
							}
							break;
						}
					}
				}
				((GameState)this.handler).workerTask.add(new Pair<Unit,Unit>(chosen,((GameState)this.handler).chosenBuildingLot));
				((GameState)this.handler).chosenWorker = null;
				((GameState)this.handler).buildingLot.remove(((GameState)this.handler).chosenBuildingLot);
				((GameState)this.handler).chosenBuildingLot = null;
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
