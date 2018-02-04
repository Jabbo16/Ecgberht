package ecgberht.MoveToBuild;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Pair;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import ecgberht.GameState;

public class ChooseTurret extends Action {

	public ChooseTurret(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).getArmySize() < ((GameState)this.handler).strat.armyForTurret) {
				return State.FAILURE;
			}
			boolean tech = false;
			for(Unit ub : ((GameState)this.handler).UBs) {
				if(ub.getType() == UnitType.Terran_Engineering_Bay) {
					tech = true;
					break;
				}
			}
			if(tech && ((GameState)this.handler).Ts.isEmpty()) {
				for(Pair<Unit,Pair<UnitType,TilePosition> > w:((GameState)this.handler).workerBuild) {
					if(w.second.first == UnitType.Terran_Missile_Turret) {
						return State.FAILURE;
					}
				}
				for(Pair<Unit,Unit> w:((GameState)this.handler).workerTask) {
					if(w.second.getType() == UnitType.Terran_Missile_Turret) {
						return State.FAILURE;
					}
				}
				((GameState)this.handler).chosenToBuild = UnitType.Terran_Missile_Turret;
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
