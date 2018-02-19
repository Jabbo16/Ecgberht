package ecgberht.Expansion;

import java.util.ArrayList;
import java.util.List;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import ecgberht.EnemyBuilding;
import ecgberht.GameState;

public class ChooseBaseLocation extends Action {

	public ChooseBaseLocation(String name, GameHandler gh) {
		super(name, gh);

	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).chosenBaseLocation != null) {
				return State.SUCCESS;
			}
			TilePosition main = null;
			if(((GameState)this.handler).MainCC != null) {
				main = ((GameState)this.handler).MainCC.getTilePosition();
			}
			else {
				main = ((GameState)this.handler).getPlayer().getStartLocation();
			}
			List<BaseLocation> valid = new ArrayList<>();
			for(BaseLocation b : ((GameState)this.handler).BLs) {
				if(!((GameState)this.handler).CCs.containsKey(b.getRegion().getCenter()) && BWTA.isConnected(b.getTilePosition(), main)) {
					valid.add(b);
				}
			}
			List<BaseLocation> remove = new ArrayList<>();
			for(BaseLocation b : valid) {
				for(Unit u : ((GameState)this.handler).enemyCombatUnitMemory) {
					if(BWTA.getRegion(u.getPosition()) == null || !u.getType().canAttack() || u.getType().isWorker()) {
						continue;
					}
					if(BWTA.getRegion(u.getPosition()).getCenter().equals(BWTA.getRegion(b.getPosition()).getCenter())) {
						remove.add(b);
						break;
					}
				}
				for(EnemyBuilding u : ((GameState)this.handler).enemyBuildingMemory.values()) {
					if(BWTA.getRegion(u.pos) == null) {
						continue;
					}
					if(BWTA.getRegion(u.pos).getCenter().equals(BWTA.getRegion(b.getPosition()).getCenter())) {
						remove.add(b);
						break;
					}
				}
			}
			valid.removeAll(remove);
			
			if(valid.isEmpty()) {
				((GameState)this.handler).chosenBaseLocation = null;
				((GameState)this.handler).movingToExpand = false;
				((GameState)this.handler).chosenBuilderBL.stop();
				((GameState)this.handler).workerIdle.add(((GameState)this.handler).chosenBuilderBL);
				((GameState)this.handler).chosenBuilderBL = null;
				((GameState)this.handler).expanding = false;
				((GameState)this.handler).deltaCash.first -= UnitType.Terran_Command_Center.mineralPrice();
				((GameState)this.handler).deltaCash.second -= UnitType.Terran_Command_Center.gasPrice();
				return State.FAILURE;
			}
			((GameState)this.handler).chosenBaseLocation = valid.get(0).getTilePosition();
//			System.out.println("----------------------------");
//			System.out.println(((GameState)this.handler).chosenBaseLocation);
//			System.out.println("Expanding : " + ((GameState)this.handler).expanding);
//			System.out.println("Moving : " + ((GameState)this.handler).movingToExpand);
//			System.out.println("DELTA " + ((GameState)this.handler).deltaCash.first);
//			System.out.println("----------------------------");
			return State.SUCCESS;
		} catch(Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
}