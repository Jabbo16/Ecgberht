package ecgberht.Bunker;

import java.util.List;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;

import bwapi.Pair;
import bwapi.Position;
import bwapi.Unit;

public class EnterBunker extends Action {

	public EnterBunker(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			Pair<Unit, Position> m = ((GameState)this.handler).chosenMarine;
			for(Pair<Unit,List<Unit> > b : ((GameState)this.handler).DBs) {
				if(b.first.equals(((GameState)this.handler).chosenBunker)) {
					if(b.first.load(m.first)) {
						b.second.add(m.first);
						((GameState)this.handler).Ms.remove(m);
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
