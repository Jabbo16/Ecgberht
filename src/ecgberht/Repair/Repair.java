package ecgberht.Repair;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;

import bwapi.Pair;
import bwapi.Unit;

public class Repair extends Action {

	public Repair(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).chosenRepairer.repair(((GameState)this.handler).chosenBuildingRepair)) {
				if(((GameState)this.handler).workerIdle.contains(((GameState)this.handler).chosenRepairer)) {
					((GameState)this.handler).workerIdle.remove(((GameState)this.handler).chosenRepairer);
				} else {
					for(Pair<Unit,Unit> u:((GameState)this.handler).workerTask) {
						if(u.first.equals(((GameState)this.handler).chosenRepairer)) {
							((GameState)this.handler).workerTask.remove(u);
							for(Pair<Unit,Integer> m:((GameState)this.handler).mineralsAssigned) {
								if(m.first.equals(u.second)) {
									((GameState)this.handler).mining--;
									((GameState)this.handler).mineralsAssigned.get(((GameState)this.handler).mineralsAssigned.indexOf(m)).second--;
								}
							}
							break;
						}
					}
				}
				((GameState)this.handler).repairerTask.add(new Pair<Unit,Unit>(((GameState)this.handler).chosenRepairer,((GameState)this.handler).chosenBuildingRepair));
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
