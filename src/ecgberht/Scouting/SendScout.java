package ecgberht.Scouting;

import java.util.ArrayList;
import java.util.List;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwta.BaseLocation;
import ecgberht.GameState;

public class SendScout extends Action {

	public SendScout(String name, GameHandler gh) {
		super(name, gh);

	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).enemyBase == null) {
				if(!((GameState)this.handler).ScoutSLs.isEmpty()) {
					List<BaseLocation> aux = new ArrayList<BaseLocation>();
					for(BaseLocation b : ((GameState)this.handler).ScoutSLs) {
						if(((GameState)this.handler).bwta.isConnected(b.getTilePosition(), ((GameState)this.handler).chosenScout.getTilePosition())) {
							if(((GameState)this.handler).chosenScout.move(b.getPosition())) {
								return State.SUCCESS;
							}
						}
						else {
							aux.add(b);
						}
					}
					((GameState)this.handler).ScoutSLs.removeAll(aux);
				}
			}
			((GameState)this.handler).workerIdle.add(((GameState)this.handler).chosenScout);
			((GameState)this.handler).chosenScout.stop(false);
			((GameState)this.handler).chosenScout = null;
			return State.FAILURE;
		} catch(Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
}
