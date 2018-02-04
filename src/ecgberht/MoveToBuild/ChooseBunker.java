package ecgberht.MoveToBuild;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Pair;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import ecgberht.GameState;

public class ChooseBunker extends Action {

	public ChooseBunker(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).MBs.size() >= 1 && ((GameState)this.handler).DBs.isEmpty()) {
				for(Pair<Unit,Pair<UnitType,TilePosition> > w : ((GameState)this.handler).workerBuild) {
					if(w.second.first == UnitType.Terran_Bunker) {
						return State.FAILURE;
					}
				}
				for(Pair<Unit,Unit> w:((GameState)this.handler).workerTask) {
					if(w.second.getType() == UnitType.Terran_Bunker) {
						return State.FAILURE;
					}
				}
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
