package ecgberht.Bunker;

import java.util.List;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;

import bwapi.Pair;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public class ChooseMarineToEnter extends Action {

	public ChooseMarineToEnter(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).Ms.isEmpty()) {
				return State.FAILURE;
			}
			for(Pair<Unit,List<Unit> > b : ((GameState)this.handler).DBs) {
				if(b.first.getTilePosition().equals(((GameState)this.handler).chosenBunker.getTilePosition())) {
					Pair<Unit,Position> closest = null;
					for (Pair<Unit,Position> u :  ((GameState)this.handler).Ms) {
						if(u.first.getType() == UnitType.Terran_Marine) {
							if ((closest == null || b.first.getDistance(u.first) < b.first.getDistance(closest.first))) {
								closest = u;
							}
						}
					}
					if(closest != null) {
						((GameState)this.handler).chosenMarine = closest;
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
