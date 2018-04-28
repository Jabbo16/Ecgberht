package ecgberht.Scanner;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.unit.ComsatStation;

import ecgberht.GameState;

public class Scan extends Action {

	public Scan(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			for(ComsatStation u: ((GameState)this.handler).CSs) {
				if(u.getEnergy() >= 50 && u.getOrder() != Order.CastScannerSweep) {
					if(u.scannerSweep(((GameState)this.handler).checkScan.toPosition())) {
						((GameState)this.handler).startCount = ((GameState)this.handler).getIH().getFrameCount();
						((GameState)this.handler).playSound("uav.mp3");
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
