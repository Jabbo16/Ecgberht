package ecgberht.MoveToBuild;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Pair;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import ecgberht.GameState;

public class Move extends Action {

	public Move(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			Unit chosen = ((GameState)this.handler).chosenWorker;
			if(!chosen.canMove()) {
				return State.FAILURE;
			}
			boolean success = false;
			if(((GameState)this.handler).chosenToBuild == UnitType.Terran_Refinery) {
				success = chosen.build(((GameState)this.handler).chosenToBuild,((GameState)this.handler).chosenPosition);
			}
			else {
				Position realEnd = ((GameState)this.handler).getCenterFromBuilding(((GameState)this.handler).chosenPosition.toPosition(), ((GameState)this.handler).chosenToBuild);
				success = chosen.move(realEnd);
			}
			if(success) {
				if(((GameState)this.handler).workerIdle.contains(chosen)) {
					((GameState)this.handler).workerIdle.remove(chosen);
				} else {
					for(Pair<Unit,Unit> u:((GameState)this.handler).workerTask) {
						if(u.first.equals(chosen)) {
							((GameState)this.handler).workerTask.remove(u);
							if(((GameState)this.handler).mineralsAssigned.containsKey(u.second)) {
								((GameState)this.handler).mining--;
								((GameState)this.handler).mineralsAssigned.put(u.second, ((GameState)this.handler).mineralsAssigned.get(u.second) - 1);
							}
							break;
						}
					}
				}
				((GameState)this.handler).workerBuild.add(new Pair<Unit,Pair<UnitType,TilePosition>>(chosen,new Pair <UnitType,TilePosition>(((GameState)this.handler).chosenToBuild,((GameState)this.handler).chosenPosition)));
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
