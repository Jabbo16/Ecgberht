package ecgberht.MoveToBuild;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.UnitType;
import ecgberht.GameState;

public class ChooseBunker extends Action {

	public ChooseBunker(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).MBs.size() >= 1 && ((GameState)this.handler).countUnit(UnitType.Terran_Bunker) == 0) {
				((GameState)this.handler).chosenToBuild = UnitType.Terran_Bunker;
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
