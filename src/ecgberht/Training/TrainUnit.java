package ecgberht.Training;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Unit;
import ecgberht.GameState;

public class TrainUnit extends Action {

	public TrainUnit(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			Unit chosen = ((GameState)this.handler).chosenBuilding;
			if(((GameState)this.handler).strat.name == "ProxyBBS") {
				if(((GameState)this.handler).getSupply() > 0 ) {
					chosen.train(((GameState)this.handler).chosenUnit);
					return State.SUCCESS;
				}
			}
			if(((GameState)this.handler).getSupply() > 4 || ((GameState)this.handler).checkSupply() || ((GameState)this.handler).getPlayer().supplyTotal() >= 400) {
				chosen.train(((GameState)this.handler).chosenUnit);
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
