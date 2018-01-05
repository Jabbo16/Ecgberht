package ecgberht.Build;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;

import bwapi.Pair;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

public class Build extends Action {

	public Build(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			for(Pair<Unit, Pair<UnitType, TilePosition>> u : ((GameState)this.handler).workerBuild) {
				if(u.first.getPosition().getApproxDistance(u.second.second.toPosition()) < 5) {
					Unit chosen = u.first;
					if(!chosen.canBuild()) {
						return State.FAILURE;
					}
					if(chosen.build(u.second.first,u.second.second)) {
						return State.SUCCESS;
					}
					return State.FAILURE;
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
