package ecgberht.Scouting;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;

import bwta.BaseLocation;
import ecgberht.GameState;

public class CheckVisibleBase extends Conditional {

	public CheckVisibleBase(String name, GameHandler gh) {
		super(name, gh);

	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).choosenScout == null) {
				return State.FAILURE;
			}
			if(!((GameState)this.handler).ScoutSLs.isEmpty()) {
				for(BaseLocation b : ((GameState)this.handler).ScoutSLs) {
					if((((GameState)this.handler).getGame().isVisible(b.getTilePosition()))) {
						((GameState)this.handler).ScoutSLs.remove(b);
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
