package ecgberht.MoveToBuild;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;

import bwapi.Pair;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

public class ChooseRefinery extends Action {

	public ChooseRefinery(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).getPlayer().supplyUsed() < 30 || ((GameState)this.handler).getCash().second >= 300) {
				return State.FAILURE;
			}
			if(((GameState)this.handler).refineriesAssigned.size() == 1) {
				boolean found = false;
				for(Pair<Unit,Pair<UnitType,TilePosition> > w:((GameState)this.handler).workerBuild) {
					if(w.second.first == UnitType.Terran_Barracks) {
						found = true;
						break;
					}
				}
				for(Pair<Unit,Unit> w:((GameState)this.handler).workerTask) {
					if(w.second.getType() == UnitType.Terran_Barracks) {
						found = true;
						break;
					}
				}
				if(((GameState)this.handler).MBs.isEmpty() && found == false) {
					return State.FAILURE;
				}
			}
			int count = 0;
			Unit geyser = null;
			for(Pair<Pair<Unit,Integer>,Boolean> r: ((GameState)this.handler).refineriesAssigned) {
				if(r.second) {
					count++;
				}
				else{
					geyser = r.first.first;
				}
			}
			if(count == ((GameState)this.handler).refineriesAssigned.size()) {
				return State.FAILURE;
			}
			for(Pair<Unit,Pair<UnitType,TilePosition> > w:((GameState)this.handler).workerBuild) {
				if(w.second.first == UnitType.Terran_Refinery) {
					return State.FAILURE;
				}
			}
			for(Pair<Unit, Unit> w:((GameState)this.handler).workerTask) {
				if(w.second.getType() == UnitType.Terran_Refinery && w.second.getPosition().equals(geyser.getPosition())) {
					return State.FAILURE;
				}
			}

			((GameState)this.handler).chosenToBuild = UnitType.Terran_Refinery;
			return State.SUCCESS;

		} catch(Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
}
