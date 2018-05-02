package ecgberht.Defense;

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.Bunker;
import org.openbw.bwapi4j.unit.CommandCenter;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.unit.Worker;

import bwta.BWTA;
import bwta.Region;
import ecgberht.EnemyBuilding;
import ecgberht.GameState;
import ecgberht.Squad;
import ecgberht.Squad.Status;
import ecgberht.UnitComparator;
import ecgberht.Util;

public class CheckPerimeter extends Conditional {

	public CheckPerimeter(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {

		try {
			((GameState)this.handler).enemyInBase.clear();
			((GameState)this.handler).defense = false;
			Set<Unit> enemyInvaders = new TreeSet<>(new UnitComparator());
			enemyInvaders.addAll(((GameState)this.handler).enemyCombatUnitMemory);
			for(EnemyBuilding u : ((GameState)this.handler).enemyBuildingMemory.values()) {
				if(u.type.canAttack() || u.type == UnitType.Protoss_Pylon || u.type.canProduce() || u.type.isRefinery()) {
					enemyInvaders.add(u.unit);
				}
			}
			for(Unit u : enemyInvaders) {
				UnitType uType = Util.getType((PlayerUnit)u);
				if(u instanceof Building || ((uType.canAttack() || uType.isSpellcaster()) && uType != UnitType.Zerg_Scourge && uType != UnitType.Protoss_Corsair)) {
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
					for(Building c : ((GameState)this.handler).workerTask.values()) {
						if(!(c instanceof Bunker) && !(c instanceof CommandCenter)) continue;
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

			if(!((GameState)this.handler).enemyInBase.isEmpty()) {
				if((((GameState)this.handler).getArmySize() >= 50 && ((GameState)this.handler).getArmySize() / ((GameState)this.handler).enemyInBase.size() > 10)) {
					return State.FAILURE;
				}
				((GameState)this.handler).defense = true;
				return State.SUCCESS;
			}
			int cFrame = ((GameState)this.handler).frameCount;
			for(Worker u : ((GameState)this.handler).workerDefenders.keySet()) {
				if(u.getLastCommandFrame() == cFrame) {
					continue;
				}
				Position closestCC = ((GameState)this.handler).getNearestCC(u.getPosition());
				if(closestCC != null) {
					if(!((GameState)this.handler).bwta.getRegion(u.getPosition()).getCenter().equals(((GameState)this.handler).bwta.getRegion(closestCC).getCenter())){
						u.move(closestCC);
					}
				}
			}
			for(Squad u : ((GameState)this.handler).squads.values()) {
				if(u.status == Status.DEFENSE) {
					Position closestCC = ((GameState)this.handler).getNearestCC(((GameState)this.handler).getSquadCenter(u));
					if(closestCC != null) {
						Region squad = ((GameState)this.handler).bwta.getRegion(((GameState)this.handler).getSquadCenter(u));
						Region regCC =  ((GameState)this.handler).bwta.getRegion(closestCC);
						if(squad != null && regCC != null) {
							if(!squad.getCenter().equals(regCC.getCenter())){
								if(!((GameState)this.handler).DBs.isEmpty() && ((GameState)this.handler).CCs.size() == 1) {
									u.giveMoveOrder(((GameState)this.handler).DBs.keySet().iterator().next().getPosition());
								}
								else {
									u.giveMoveOrder(Util.getClosestChokepoint(((GameState)this.handler).getSquadCenter(u)).getCenter());
								}
								u.status = Status.IDLE;
								u.attack = Position.None;
								continue;
							}
						}
						u.status = Status.IDLE;
						u.attack = Position.None;
						continue;
					}
					u.status = Status.IDLE;
					u.attack = Position.None;
					continue;
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