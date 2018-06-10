package ecgberht.Scanner;

import bwapi.Order;
import bwapi.TechType;
import bwapi.Unit;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

public class Scan extends Action {

	public Scan(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			for (Unit u : ((GameState) this.handler).CSs) {
				if (u.getEnergy() >= 50 && u.getOrder() != Order.CastScannerSweep) {
					if (u.useTech(TechType.Scanner_Sweep, ((GameState) this.handler).checkScan.toPosition())) {
						((GameState) this.handler).startCount = ((GameState) this.handler).getGame().elapsedTime();
						((GameState) this.handler).playSound("uav.mp3");
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
