package ecgberht.Building;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;

import bwapi.Pair;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

public class Build extends Action {

	public Build(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			Unit chosen = ((GameState)this.handler).chosenWorker;
			if(!chosen.canBuild()) {
				return State.FAILURE;
			}
			if(chosen.build(((GameState)this.handler).chosenToBuild, ((GameState)this.handler).chosenPosition)) {
				if(((GameState)this.handler).workerIdle.contains(chosen)) {
					((GameState)this.handler).workerIdle.remove(chosen);
				} else {
					for(Pair<Unit,Unit> u:((GameState)this.handler).workerTask) {
						if(u.first.equals(chosen)) {
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
				((GameState)this.handler).workerBuild.add(new Pair<Unit,Pair<UnitType,TilePosition> >(chosen,new Pair <UnitType,TilePosition>(((GameState)this.handler).chosenToBuild,((GameState)this.handler).chosenPosition)));
				((GameState)this.handler).deltaCash.first += ((GameState)this.handler).chosenToBuild.mineralPrice();
				((GameState)this.handler).deltaCash.second += ((GameState)this.handler).chosenToBuild.gasPrice();
				((GameState)this.handler).chosenWorker = null;
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
