package ecgberht.Training;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;

import bwapi.Unit;
import bwapi.UnitType;


public class ChooseMarine extends Action {

	public ChooseMarine(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(!((GameState)this.handler).MBs.isEmpty()) {
				for(Unit b:((GameState)this.handler).MBs) {
					if(!b.isTraining()) {
						((GameState)this.handler).chosenUnit = UnitType.Terran_Marine;
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
