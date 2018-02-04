package ecgberht.CombatStim;

import java.util.Map.Entry;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.UnitType;
import ecgberht.GameState;
import ecgberht.Squad;

public class Stim extends Action {

	public Stim(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).squads.isEmpty() || ((GameState)this.handler).getPlayer().allUnitCount(UnitType.Terran_Marine) == 0) {
				return State.FAILURE;
			}
			for(Entry<String,Squad> s : ((GameState)this.handler).squads.entrySet()) {
				s.getValue().giveStimOrder();
			}
			return State.SUCCESS;
		} catch(Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
}
