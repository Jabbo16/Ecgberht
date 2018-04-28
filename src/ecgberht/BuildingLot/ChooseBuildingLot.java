package ecgberht.BuildingLot;

import java.util.ArrayList;
import java.util.List;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.Bunker;
import org.openbw.bwapi4j.unit.MissileTurret;
import org.openbw.bwapi4j.unit.PlayerUnit;

import ecgberht.GameState;
import ecgberht.Util;

public class ChooseBuildingLot extends Action {

	public ChooseBuildingLot(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			Building savedTurret = null;
			List<Building> aux = new ArrayList<>();
			for(Building b : ((GameState)this.handler).buildingLot) {
				if(!b.isUnderAttack()) {
					if(b instanceof Bunker) {
						((GameState)this.handler).chosenBuildingLot = b;
						return State.SUCCESS;
					}
					if(b instanceof MissileTurret) {
						savedTurret = b;
					}
					((GameState)this.handler).chosenBuildingLot = b;
				} else {
					if((double)b.getHitPoints() /(double) Util.getType((PlayerUnit)b).maxHitPoints() <= 0.1) {
						b.cancelConstruction();
						aux.add(b);
					}
				}
			}
			((GameState)this.handler).buildingLot.removeAll(aux);
			if(savedTurret != null) {
				((GameState)this.handler).chosenBuildingLot = savedTurret;
				return State.SUCCESS;
			}
			if(((GameState)this.handler).chosenBuildingLot != null) {
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
