package ecgberht.Bunker;

import java.util.Map.Entry;
import java.util.Set;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Unit;
import ecgberht.GameState;

public class ChooseBunkerToLoad extends Action {

	public ChooseBunkerToLoad(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			for(Entry<Unit, Set<Unit>> b : ((GameState)this.handler).DBs.entrySet()) {
				if(b.getValue().size() < 4) {
					((GameState)this.handler).chosenBunker = b.getKey();
					return State.SUCCESS;
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
