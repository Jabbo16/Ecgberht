package ecgberht.Repair;


import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Pair;
import bwapi.Unit;
import ecgberht.GameState;

public class CheckBuildingFlames extends Action {

	public CheckBuildingFlames(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			boolean isBeingRepaired = false;
			for(Unit w : ((GameState)this.handler).DBs.keySet()) {
				int count = 0;
				if(w.getType().maxHitPoints() != w.getHitPoints()) {
					for(Pair<Unit,Unit> r : ((GameState)this.handler).repairerTask) {
						if(w.equals(r.second)) {
							count++;
						}
					}
					if(count < 2 && ((GameState)this.handler).defense) {
						((GameState)this.handler).chosenBuildingRepair = w;
						return State.SUCCESS;
					}
					else if(count == 0) {
						((GameState)this.handler).chosenBuildingRepair = w;
						return State.SUCCESS;
					}
				}
			}
			isBeingRepaired = false;
			for(Unit b : ((GameState)this.handler).Ts) {
				if(b.getType().maxHitPoints() != b.getHitPoints()) {
					for(Pair<Unit,Unit> r : ((GameState)this.handler).repairerTask) {
						if(b.equals(r.second)) {
							isBeingRepaired = true;
						}
					}
					if(!isBeingRepaired) {
						((GameState)this.handler).chosenBuildingRepair = b;
						return State.SUCCESS;
					}
				}
			}
			isBeingRepaired = false;
			for(Unit b : ((GameState)this.handler).MBs) {
				if(b.getType().maxHitPoints() != b.getHitPoints()) {
					for(Pair<Unit,Unit> r : ((GameState)this.handler).repairerTask) {
						if(b.equals(r.second)) {
							isBeingRepaired = true;
						}
					}
					if(!isBeingRepaired) {
						((GameState)this.handler).chosenBuildingRepair = b;
						return State.SUCCESS;
					}
				}
			}
			isBeingRepaired = false;
			for(Unit b : ((GameState)this.handler).Fs) {
				if(b.getType().maxHitPoints() != b.getHitPoints()) {
					for(Pair<Unit,Unit> r : ((GameState)this.handler).repairerTask) {
						if(b.equals(r.second)) {
							isBeingRepaired = true;
						}
					}
					if(!isBeingRepaired) {
						((GameState)this.handler).chosenBuildingRepair = b;
						return State.SUCCESS;
					}
				}
			}
			isBeingRepaired = false;
			for(Unit b : ((GameState)this.handler).UBs) {
				if(b.getType().maxHitPoints() != b.getHitPoints()) {
					for(Pair<Unit,Unit> r : ((GameState)this.handler).repairerTask) {
						if(b.equals(r.second)) {
							isBeingRepaired = true;
						}
					}
					if(!isBeingRepaired) {
						((GameState)this.handler).chosenBuildingRepair = b;
						return State.SUCCESS;
					}
				}
			}
			isBeingRepaired = false;
			for(Unit b : ((GameState)this.handler).SBs) {
				if(b.getType().maxHitPoints() != b.getHitPoints()) {
					for(Pair<Unit,Unit> r : ((GameState)this.handler).repairerTask) {
						if(b.equals(r.second)) {
							isBeingRepaired = true;
						}
					}
					if(!isBeingRepaired) {
						((GameState)this.handler).chosenBuildingRepair = b;
						return State.SUCCESS;
					}
				}
			}
			isBeingRepaired = false;
			for(Unit b : ((GameState)this.handler).CCs.values()) {
				if(b.getType().maxHitPoints() != b.getHitPoints()) {
					for(Pair<Unit,Unit> r : ((GameState)this.handler).repairerTask) {
						if(b.equals(r.second)) {
							isBeingRepaired = true;
						}
					}
					if(!isBeingRepaired) {
						((GameState)this.handler).chosenBuildingRepair = b;
						return State.SUCCESS;
					}
				}
			}
			isBeingRepaired = false;
			for(Unit b : ((GameState)this.handler).CSs) {
				if(b.getType().maxHitPoints() != b.getHitPoints()) {
					for(Pair<Unit,Unit> r : ((GameState)this.handler).repairerTask) {
						if(b.equals(r.second)) {
							isBeingRepaired = true;
						}
					}
					if(!isBeingRepaired) {
						((GameState)this.handler).chosenBuildingRepair = b;
						return State.SUCCESS;
					}
				}
			}
			isBeingRepaired = false;
			for(Unit b : ((GameState)this.handler).Ps) {
				if(b.getType().maxHitPoints() != b.getHitPoints()) {
					for(Pair<Unit,Unit> r : ((GameState)this.handler).repairerTask) {
						if(b.equals(r.second)) {
							isBeingRepaired = true;
						}
					}
					if(!isBeingRepaired) {
						((GameState)this.handler).chosenBuildingRepair = b;
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
