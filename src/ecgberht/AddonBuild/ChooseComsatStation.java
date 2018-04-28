package ecgberht.AddonBuild;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Academy;
import org.openbw.bwapi4j.unit.CommandCenter;
import org.openbw.bwapi4j.unit.ResearchingFacility;

import ecgberht.GameState;

public class ChooseComsatStation extends Action {

	public ChooseComsatStation(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(!((GameState)this.handler).CCs.isEmpty()) {
				for(CommandCenter c : ((GameState)this.handler).CCs.values()) {
					if (!c.isTraining() && c.getAddon() == null) {
						for(ResearchingFacility u : ((GameState)this.handler).UBs) {
							if(u instanceof Academy) {
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
