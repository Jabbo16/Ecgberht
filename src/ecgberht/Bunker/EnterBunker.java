package ecgberht.Bunker;

import java.util.List;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Pair;
import bwapi.Unit;
import ecgberht.GameState;

public class EnterBunker extends Action {

	public EnterBunker(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			Pair<String, Unit> m = ((GameState)this.handler).chosenMarine;
			for(Pair<Unit,List<Unit> > b : ((GameState)this.handler).DBs) {
				if(b.first.equals(((GameState)this.handler).chosenBunker)) {
					if(b.first.load(m.second)) {
						b.second.add(m.second);
						((GameState)this.handler).squads.get(m.first).members.remove(m.second);
						return State.SUCCESS;
					}
				}
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
