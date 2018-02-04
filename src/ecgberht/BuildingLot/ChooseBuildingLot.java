package ecgberht.BuildingLot;

import java.util.ArrayList;
import java.util.List;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Unit;
import bwapi.UnitType;
import ecgberht.GameState;

public class ChooseBuildingLot extends Action {

	public ChooseBuildingLot(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			Unit savedTurret = null;
			List<Unit> aux = new ArrayList<Unit>();
			for(Unit b : ((GameState)this.handler).buildingLot) {
				if(!b.isUnderAttack()) {
					if(b.getType() == UnitType.Terran_Bunker) {
						((GameState)this.handler).chosenBuildingLot = b;
						return State.SUCCESS;
					}
					if(b.getType() == UnitType.Terran_Missile_Turret) {
						savedTurret = b;
					}
					((GameState)this.handler).chosenBuildingLot = b;
				} else {
					if((double)b.getHitPoints() /(double) b.getType().maxHitPoints() <= 0.1) {
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
