package ecgberht.Scanner;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;

import bwapi.Unit;

public class CheckScan extends Conditional {

	public CheckScan(String name, GameHandler gh) {
		super(name, gh);
	}
	
	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).CSs.isEmpty()) {
				return State.FAILURE;
			}
			if(((GameState)this.handler).getGame().elapsedTime() - ((GameState)this.handler).startCount > 1) {
				for (Unit u : ((GameState)this.handler).getGame().enemy().getUnits()) {
					if((u.isCloaked() || u.isBurrowed()) && !u.isDetected()) {
						((GameState)this.handler).checkScan = u.getTilePosition();
						return State.SUCCESS;
					}
				}
			}
			return State.FAILURE;
		} catch (Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
}
