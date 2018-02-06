package ecgberht;

import static ecgberht.Ecgberht.getGame;
import static ecgberht.Ecgberht.getGs;

import java.util.HashSet;
import java.util.Set;

import bwapi.Order;
import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
public class Squad {

	public enum Status {
		ATTACK, KITE, RETREAT, MOVE, IDLE, DEFENSE
	}
	public String name;
	public Set<Unit> members;
	public Status estado;
	public Position attack;
	public int lastFrameOrder = 0;
	public Squad(String name) {
		this.name = name;
		members = new HashSet<Unit>();
		estado = Status.IDLE;
		attack = Position.None;
	}

	public void addToSquad(Unit unit) {
		this.members.add(unit);
	}

	public void giveAttackOrder(Position pos) {
		for(Unit u : members) {
			if(getGame().getFrameCount() - u.getLastCommandFrame() > 32 ) {
				if(u.getType() == UnitType.Terran_Siege_Tank_Siege_Mode && u.getOrder() == Order.Unsieging) {
					continue;
				}
				if(u.getType() == UnitType.Terran_Siege_Tank_Tank_Mode && u.getOrder() == Order.Sieging) {
					continue;
				}
				if(!u.isStartingAttack() && !u.isAttacking() || u.isIdle()) {
					u.attack(pos);
					continue;
				}
//				if(u.isIdle()) {
//					u.attack(pos);
//					continue;
//				}
//				if (estado == Status.IDLE || !pos.equals(attack)) {
//					if (u.getGroundWeaponCooldown() > 0) {
//						for(Unit e : getGs().enemyCombatUnitMemory) {
//							if(e.getType() == UnitType.Zerg_Zergling || e.getType() == UnitType.Protoss_Zealot) {
//								if(u.getUnitsInRadius(UnitType.Terran_Marine.groundWeapon().maxRange()).contains(e)) {
//									u.move(getGame().self().getStartLocation().toPosition());
//								}
//							}
//						}
//					}
//					else if(!u.isStartingAttack() && !u.isAttacking() && (u.isIdle() || u.isMoving())) {
//						u.attack(pos);
//					}
//				} else if(!u.getOrderTargetPosition().equals(attack)) {
//					u.attack(pos);
//				}
			}
		}
		attack = pos;
		lastFrameOrder = getGame().getFrameCount();
	}
	
	public void giveStimOrder() {
		for(Unit u : members) {
			if(u.canUseTech(TechType.Stim_Packs) && !u.isStimmed() && u.isAttacking()) {
				u.useTech(TechType.Stim_Packs);
			}
		}
	}
	public void microUpdateOrder() {
		for(Unit u : members) {
			if(u.isIdle() && attack != null && getGame().getFrameCount() != u.getLastCommandFrame()) {
				u.attack(attack);
				continue;
			}
			if(getGame().getFrameCount() - u.getLastCommandFrame() > 32) {
				if(u.isIdle() && attack != null) {
					u.attack(attack);
					continue;
				}
				if (u.getGroundWeaponCooldown() > 0) {
					for(Unit e : getGs().enemyCombatUnitMemory) {
						if(e.getType() == UnitType.Zerg_Zergling || e.getType() == UnitType.Protoss_Zealot) {
							if(u.getUnitsInRadius(UnitType.Terran_Marine.groundWeapon().maxRange()).contains(e)) {
								u.move(getGame().self().getStartLocation().toPosition());
							}
						}
					}
				}
				else if(attack != null && !u.isStartingAttack() && !u.isAttacking() && (u.isIdle() || u.isMoving())) {
					u.attack(attack);
				}
			}
		}
		
	}
	public Set<Unit> getTanks() {
		Set<Unit> aux = new HashSet<Unit>();
		for(Unit u : members) {
			if(u.getType() == UnitType.Terran_Siege_Tank_Siege_Mode || u.getType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
				aux.add(u);
			}
		}
		return aux;
	}

	public Set<Unit> getMarines() {
		Set<Unit> aux = new HashSet<Unit>();
		for(Unit u : this.members) {
			if(u.getType() == UnitType.Terran_Marine) {
				aux.add(u);
			}
		}
		return aux;
	}
	
}
