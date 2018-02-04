package ecgberht.AddonBuild;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Unit;
import bwapi.UnitType;
import ecgberht.GameState;

public class ChooseComsatStation extends Action {

	public ChooseComsatStation(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(!((GameState)this.handler).CCs.isEmpty()) {
				for(Unit c : ((GameState)this.handler).CCs) {
					if (!c.isTraining() && c.getAddon() == null) {
						for(Unit u : ((GameState)this.handler).UBs) {
							if(u.getType() == UnitType.Terran_Academy) {
								((GameState)this.handler).chosenBuildingAddon = c;
								((GameState)this.handler).chosenAddon = UnitType.Terran_Comsat_Station;
								return State.SUCCESS;
							}
						}

					}
				}
			}
			((GameState)this.handler).chosenBuildingAddon = null;
			((GameState)this.handler).chosenAddon = null;
			return State.FAILURE;
		} catch(Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
}
