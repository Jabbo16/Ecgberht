package ecgberht.Expansion;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;

import bwapi.Pair;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import ecgberht.GameState;

public class CheckExpansion extends Conditional {

	public CheckExpansion(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).defense) {
				if(((GameState)this.handler).expanding) {
					((GameState)this.handler).deltaCash.first -= UnitType.Terran_Command_Center.mineralPrice();
					((GameState)this.handler).deltaCash.second -= UnitType.Terran_Command_Center.gasPrice();
					((GameState)this.handler).movingToExpand = false;
					if(((GameState)this.handler).chosenBuilderBL != null) {
						((GameState)this.handler).workerBuild.add(new Pair<Unit,Pair<UnitType,TilePosition>>(((GameState)this.handler).chosenBuilderBL,new Pair <UnitType,TilePosition>(UnitType.Terran_Command_Center,((GameState)this.handler).chosenBaseLocation.getTilePosition())));
						((GameState)this.handler).chosenBuilderBL = null;
					}
					((GameState)this.handler).expanding = false;
					((GameState)this.handler).chosenBaseLocation = null;
					
				}
				return State.FAILURE;
			}
			if(!((GameState)this.handler).expanding && ((GameState)this.handler).movingToExpand) {
				((GameState)this.handler).expanding = false;
				return State.FAILURE;
			}
			if(((GameState)this.handler).expanding) {
				return State.SUCCESS;
			}
			for(Pair<Unit,Pair<UnitType,TilePosition> > w:((GameState)this.handler).workerBuild) {
				if(w.second.first == UnitType.Terran_Command_Center) {
					return State.FAILURE;
				}
			}
			for(Pair<Unit,Unit> w:((GameState)this.handler).workerTask) {
				if(w.second.getType() == UnitType.Terran_Command_Center) {
					return State.FAILURE;
				}
			}
			int workers = 0;
			for(Pair<Unit, Integer> wt : ((GameState)this.handler).mineralsAssigned) {
				workers += wt.second;
			}
			if(((GameState)this.handler).mineralsAssigned.size() * 2 <= workers && ((GameState)this.handler).getArmySize() >= ((GameState)this.handler).strat.armyForExpand) {
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
