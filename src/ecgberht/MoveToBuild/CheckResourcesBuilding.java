package ecgberht.MoveToBuild;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;

import bwapi.Pair;
import bwapi.TilePosition;
import bwapi.Unit;

public class CheckResourcesBuilding extends Conditional {

	public CheckResourcesBuilding(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			Pair<Integer,Integer> cash = ((GameState)this.handler).getCash();
			Unit chosen = ((GameState)this.handler).chosenWorker;
			TilePosition start = chosen.getTilePosition();
			TilePosition end = ((GameState)this.handler).chosenPosition;
			if(cash.first + ((GameState)this.handler).getMineralsWhenReaching(chosen, start, end) >= (((GameState)this.handler).chosenToBuild.mineralPrice() + ((GameState)this.handler).deltaCash.first) && cash.second >= (((GameState)this.handler).chosenToBuild.gasPrice()) + ((GameState)this.handler).deltaCash.second) {
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
