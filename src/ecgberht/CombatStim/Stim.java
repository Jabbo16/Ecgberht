package ecgberht.CombatStim;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;

import bwapi.Pair;
import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;

public class Stim extends Action {

	public Stim(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).Ms.isEmpty() || ((GameState)this.handler).getPlayer().allUnitCount(UnitType.Terran_Marine) == 0) {
				return State.FAILURE;
			}
			for(Pair<Unit, Position> m : ((GameState)this.handler).Ms) {
				if(m.first.canUseTech(TechType.Stim_Packs) && !m.first.isStimmed() && m.first.isAttacking()) {
					m.first.useTech(TechType.Stim_Packs);
				}
			}
			return State.SUCCESS;
		} catch(Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
}
