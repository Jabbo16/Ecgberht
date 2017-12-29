package ecgberht.Bunker;

import java.util.List;
import java.util.Map.Entry;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;
import ecgberht.Squad;
import bwapi.Pair;
import bwapi.Unit;
import bwapi.UnitType;

public class ChooseMarineToEnter extends Action {

	public ChooseMarineToEnter(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).squads.isEmpty()) {
				return State.FAILURE;
			}
			for(Pair<Unit,List<Unit> > b : ((GameState)this.handler).DBs) {
				if(b.first.getTilePosition().equals(((GameState)this.handler).chosenBunker.getTilePosition())) {
					Pair<String,Unit> closest = null;
					for(Entry<String, Squad> s : ((GameState)this.handler).squads.entrySet()) {
						for(Unit u : s.getValue().members) {
							if(u.getType() == UnitType.Terran_Marine) {
								if ((closest == null || b.first.getDistance(u) < b.first.getDistance(closest.second))) {
									closest = new Pair<String,Unit>(s.getKey(),u);
								}
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
