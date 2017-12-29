package ecgberht.Scanner;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;

import bwapi.TechType;
import bwapi.Unit;

public class Scan extends Action {

	public Scan(String name, GameHandler gh) {
		super(name, gh);
	}
	
	@Override
	public State execute() {
		try {
			for(Unit u: ((GameState)this.handler).CSs) {
				if(u.getEnergy() > 50 && u.useTech(TechType.Scanner_Sweep,((GameState)this.handler).checkScan.toPosition())) {
					((GameState)this.handler).startCount = ((GameState)this.handler).getGame().elapsedTime();
					return State.SUCCESS;
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
