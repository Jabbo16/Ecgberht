package ecgberht.Defense;



import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;

import bwapi.Pair;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.Region;
import ecgberht.GameState;
import ecgberht.Squad;
import ecgberht.Squad.Status;

public class CheckPerimeter extends Conditional {

	public CheckPerimeter(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {

		try {
			((GameState)this.handler).enemyInBase.clear();
			
			if(((GameState)this.handler).enemyCombatUnitMemory.isEmpty()) {
				for(Squad u : ((GameState)this.handler).squads.values()) {
					if(u.status == Status.DEFENSE) {
						Position closestCC = ((GameState)this.handler).getNearestCC(((GameState)this.handler).getSquadCenter(u));
						if(closestCC != null) {
							if(!BWTA.getRegion(((GameState)this.handler).getSquadCenter(u)).getCenter().equals(BWTA.getRegion(closestCC).getCenter())){
								if(!((GameState)this.handler).DBs.isEmpty() && ((GameState)this.handler).CCs.size() == 1) {
									u.giveMoveOrder(((GameState)this.handler).DBs.keySet().iterator().next().getPosition());
								}
								else {
									u.giveMoveOrder(BWTA.getNearestChokepoint(((GameState)this.handler).getSquadCenter(u)).getCenter());
								}
								u.status = Status.IDLE;
								u.attack = Position.None;
							}
						}
					}
				}
				((GameState)this.handler).defense = false;
				return State.FAILURE;
			}
			for(Unit u : ((GameState)this.handler).enemyCombatUnitMemory) {
				if((!u.getType().isBuilding() || u.getType() == UnitType.Protoss_Pylon || u.getType().canAttack()) && u.getType() != UnitType.Zerg_Scourge) {
					for(Unit c : ((GameState)this.handler).CCs.values()) {
						if(((GameState)this.handler).broodWarDistance(u.getPosition(), c.getPosition()) < 500) {
							((GameState)this.handler).enemyInBase.add(u);
							continue;
						}
					}
					for(Unit c : ((GameState)this.handler).DBs.keySet()) {
						if(((GameState)this.handler).broodWarDistance(u.getPosition(), c.getPosition()) < 200) {
							((GameState)this.handler).enemyInBase.add(u);
							continue;
						}
					}
					for(Unit c : ((GameState)this.handler).SBs) {
						if(((GameState)this.handler).broodWarDistance(u.getPosition(), c.getPosition()) < 200) {
							((GameState)this.handler).enemyInBase.add(u);
							continue;
						}
					}
					for(Unit c : ((GameState)this.handler).MBs) {
						if(((GameState)this.handler).broodWarDistance(u.getPosition(), c.getPosition()) < 200) {
							((GameState)this.handler).enemyInBase.add(u);
							continue;
						}
					}
				}
			}
			boolean overlordCheck = true;
			for(Unit u : ((GameState)this.handler).enemyInBase) {
				if(u.getType().canAttack() || u.getType() == UnitType.Protoss_Shuttle || u.getType() == UnitType.Terran_Dropship) {
					overlordCheck = false;
					break;
				}
			}
			if(!((GameState)this.handler).enemyInBase.isEmpty() && !overlordCheck) {
				((GameState)this.handler).defense = true;
				return State.SUCCESS;
			}
			for(Pair<Unit, Position> u : ((GameState)this.handler).workerDefenders) {
				Position closestCC = ((GameState)this.handler).getNearestCC(u.first.getPosition());
				if(closestCC != null) {
					if(!BWTA.getRegion(u.first.getPosition()).getCenter().equals(BWTA.getRegion(closestCC).getCenter())){
						u.first.move(closestCC);
					}
				}
			}
			for(Squad u : ((GameState)this.handler).squads.values()) {
				if(u.status == Status.DEFENSE) {
					Position closestCC = ((GameState)this.handler).getNearestCC(((GameState)this.handler).getSquadCenter(u));
					if(closestCC != null) {
						Region squad = BWTA.getRegion(((GameState)this.handler).getSquadCenter(u));
						Region regCC = BWTA.getRegion(closestCC);
						if(squad != null && regCC != null) {
							if(!squad.getCenter().equals(regCC.getCenter())){
								if(!((GameState)this.handler).DBs.isEmpty() && ((GameState)this.handler).CCs.size() == 1) {
									u.giveMoveOrder(((GameState)this.handler).DBs.keySet().iterator().next().getPosition());
								}
								else {
									u.giveMoveOrder(BWTA.getNearestChokepoint(((GameState)this.handler).getSquadCenter(u)).getCenter());
								}
								u.status = Status.IDLE;
								u.attack = Position.None;
							}
						}
						u.status = Status.IDLE;
						u.attack = Position.None;
					}
					u.status = Status.IDLE;
					u.attack = Position.None;
				}
			}
			((GameState)this.handler).defense = false;
			return State.FAILURE;
		} catch(Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
}
