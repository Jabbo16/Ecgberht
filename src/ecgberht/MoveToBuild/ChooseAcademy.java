package ecgberht.MoveToBuild;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Academy;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.util.Pair;

import ecgberht.GameState;
import ecgberht.Util;

public class ChooseAcademy extends Action {

	public ChooseAcademy(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).countUnit(UnitType.Terran_Refinery) == 0) {
				return State.FAILURE;
			}
			if(((GameState)this.handler).countUnit(UnitType.Terran_Barracks) >= ((GameState)this.handler).strat.numRaxForAca && Util.countUnitTypeSelf(UnitType.Terran_Academy) == 0) {
				for(Pair<UnitType, TilePosition> w:((GameState)this.handler).workerBuild.values()) {
					if(w.first == UnitType.Terran_Academy) {
						return State.FAILURE;
					}
				}
				for(Building w : ((GameState)this.handler).workerTask.values()) {
					if(w instanceof Academy) {
						return State.FAILURE;
					}
				}
				((GameState)this.handler).chosenToBuild = UnitType.Terran_Academy;
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
