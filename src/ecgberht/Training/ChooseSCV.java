package ecgberht.Training;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Unit;
import bwapi.UnitType;
import ecgberht.GameState;

public class ChooseSCV extends Action {

	public ChooseSCV(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).strat.name == "ProxyBBS") {
				boolean notTraining = false;
				for(Unit b:((GameState)this.handler).MBs) {
					if(!b.isTraining()) {
						notTraining = true;
						break;
					}
				}
				if(notTraining) {
					return State.FAILURE;
				}
			}
			if(((GameState)this.handler).getPlayer().allUnitCount(UnitType.Terran_SCV) < 50 && ((GameState)this.handler).getPlayer().allUnitCount(UnitType.Terran_SCV) < ((GameState)this.handler).mineralsAssigned.size() * 2 + 3 && !((GameState)this.handler).CCs.isEmpty()) {
				for(Unit b:((GameState)this.handler).CCs.values()) {
					if(!b.isTraining() && !b.isConstructing()) {
						((GameState)this.handler).chosenUnit = UnitType.Terran_SCV;
						((GameState)this.handler).chosenBuilding = b;
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
