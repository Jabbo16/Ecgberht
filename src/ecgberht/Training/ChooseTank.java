package ecgberht.Training;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;
import bwapi.Unit;
import bwapi.UnitType;


public class ChooseTank extends Action {

	public ChooseTank(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(!((GameState)this.handler).Fs.isEmpty() && ((GameState)this.handler).getPlayer().allUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) + ((GameState)this.handler).getPlayer().allUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) * 7 < ((GameState)this.handler).getPlayer().allUnitCount(UnitType.Terran_Marine) + ((GameState)this.handler).getPlayer().allUnitCount(UnitType.Terran_Medic)) {	
				for(Unit b:((GameState)this.handler).Fs) {
					if(!b.isTraining() && b.canTrain()) {
						((GameState)this.handler).chosenUnit = UnitType.Terran_Siege_Tank_Tank_Mode;
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
