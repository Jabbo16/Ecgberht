package ecgberht.Bunker;

import java.util.List;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Pair;
import bwapi.Unit;
import ecgberht.GameState;

public class ChooseBunkerToLoad extends Action {

	public ChooseBunkerToLoad(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			for(Pair<Unit,List<Unit> > b : ((GameState)this.handler).DBs) {
				if(b.second.size()<4) {
					((GameState)this.handler).chosenBunker = b.first;
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
