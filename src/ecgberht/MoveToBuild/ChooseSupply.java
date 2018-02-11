package ecgberht.MoveToBuild;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Pair;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import ecgberht.GameState;

public class ChooseSupply extends Action {

	public ChooseSupply(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).getPlayer().supplyTotal() >= 400) {
				return State.FAILURE;
			}
			//if(((GameState)this.handler).countUnit2(UnitType.Terran_Barracks) < 2 && ((GameState)this.handler).strat.name == "ProxyBBS") {
			if(((GameState)this.handler).getPlayer().allUnitCount(UnitType.Terran_Barracks) < 2 && ((GameState)this.handler).strat.name == "ProxyBBS") {
				return State.FAILURE;
			}
			if(((GameState)this.handler).EI.naughty && ((GameState)this.handler).MBs.isEmpty() && ((GameState)this.handler).enemyRace == Race.Zerg) {
				if(!((GameState)this.handler).SBs.isEmpty() && !((GameState)this.handler).DBs.isEmpty()) {
					boolean found_bunker = false;
					for(Pair<Unit,Pair<UnitType,TilePosition> > w:((GameState)this.handler).workerBuild) {
						if(w.second.first == UnitType.Terran_Bunker) {
							found_bunker = true;
						}
					}
					if(!found_bunker) {
						for(Pair<Unit,Unit> w:((GameState)this.handler).workerTask) {
							if(w.second.getType() == UnitType.Terran_Bunker) {
								found_bunker = true;
							}
						}
					}
					if(!found_bunker) {
						return State.FAILURE;
					}
				}
				boolean found_rax = false;
				for(Pair<Unit,Pair<UnitType,TilePosition> > w:((GameState)this.handler).workerBuild) {
					if(w.second.first == UnitType.Terran_Barracks) {
						found_rax = true;
					}
				}
				if(!found_rax) {
					for(Pair<Unit,Unit> w:((GameState)this.handler).workerTask) {
						if(w.second.getType() == UnitType.Terran_Barracks) {
							found_rax = true;
						}
					}
				}
				if(!found_rax) {
					return State.FAILURE;
				}
				
			}
			if(((GameState)this.handler).getSupply() <= 4 * ((GameState)this.handler).getCombatUnitsBuildings()) {
				for(Pair<Unit,Pair<UnitType,TilePosition> > w:((GameState)this.handler).workerBuild) {
					if(w.second.first == UnitType.Terran_Supply_Depot) {
						return State.FAILURE;
					}
				}
				for(Pair<Unit,Unit> w:((GameState)this.handler).workerTask) {
					if(w.second.getType() == UnitType.Terran_Supply_Depot) {
						return State.FAILURE;
					}
				}
				((GameState)this.handler).chosenToBuild = UnitType.Terran_Supply_Depot;
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
