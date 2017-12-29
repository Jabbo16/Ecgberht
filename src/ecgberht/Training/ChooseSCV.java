package ecgberht.Training;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;

import bwapi.Unit;
import bwapi.UnitType;

public class ChooseSCV extends Action {

	public ChooseSCV(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if((((GameState)this.handler).workerIdle.size() + ((GameState)this.handler).workerTask.size() + ((GameState)this.handler).workerBuild.size())  + ((GameState)this.handler).workerDefenders.size() < ((GameState)this.handler).mineralsAssigned.size() * 2 + 3 && !((GameState)this.handler).CCs.isEmpty()) {
				for(Unit b:((GameState)this.handler).CCs) {
					if(!b.isTraining()) {
						((GameState)this.handler).chosenUnit = UnitType.Terran_SCV;
						((GameState)this.handler).chosenBuilding = b;
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
