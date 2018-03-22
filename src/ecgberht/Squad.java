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
		ATTACK, IDLE, DEFENSE
	}
	public String name;
	public Set<Unit> members;
	public Status status;
	public Position attack;
	public int lastFrameOrder = 0;
	public Squad(String name) {
		this.name = name;
		members = new HashSet<Unit>();
		status = Status.IDLE;
		attack = Position.None;
	}
	
	public void addToSquad(Unit unit) {
		this.members.add(unit);
	}

	public void giveAttackOrder(Position pos) {
		int frameCount = getGs().frameCount;
		if(frameCount - lastFrameOrder > 0 && !pos.equals(attack)) {
			attack = pos;
			lastFrameOrder = frameCount;
		}
	}
	
	public void giveStimOrder() {
		for(Unit u : members) {
			if(u.canUseTech(TechType.Stim_Packs) && !u.isStimmed() && u.isAttacking() && u.getHitPoints() >= 25) {
				u.useTech(TechType.Stim_Packs);
				if(getGs().getGame().elapsedTime() > getGs().lastFrameStim + 20) {
					getGs().playSound("stim.mp3");
					getGs().lastFrameStim = getGs().getGame().elapsedTime();
				}
				
			}
		}
	}
	
	public void microUpdateOrder() {
		try {
			if(members.isEmpty()) {
				return;
			}
			Set<Unit> enemy = getGs().enemyCombatUnitMemory;
			int frameCount = getGs().frameCount;
			Position start = getGame().self().getStartLocation().toPosition();
			Set<Unit> marinesToHeal = new HashSet<>();
			Position sCenter = getGs().getSquadCenter(this);
			for(Unit u : members) {
				if(u.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
					continue;
				}
				if(u.getType() == UnitType.Terran_Siege_Tank_Tank_Mode && u.getOrder() == Order.Sieging) {
					continue;
				}
				Position lastTarget = (u.getTargetPosition() == null ? u.getOrderTargetPosition() : u.getTargetPosition());
				if(lastTarget != null) {
					if(lastTarget.equals(attack)) {
						continue;
					}
				}
				if(!getGs().DBs.isEmpty()) {
					Unit bunker = getGs().DBs.keySet().iterator().next();
					if(status == Status.IDLE && getGs().broodWarDistance(bunker.getPosition(), sCenter) >= 130) {
						if(u.getOrder() != Order.Move) {
							u.move(bunker.getPosition());
						}
						continue;
					}
				}
				else if(getGs().closestChoke != null && !getGs().EI.naughty) {
					if(status == Status.IDLE && getGs().broodWarDistance(getGs().closestChoke.getCenter(), sCenter) >= 130) {
						if(u.getOrder() != Order.Move) {
							u.move(getGs().closestChoke.getCenter());
						}
						continue;
					}
				}
				
				if(status == Status.IDLE && getGs().broodWarDistance(u.getPosition(), sCenter) >= 100 && u.getOrder() != Order.Move) {
					if(getGame().isWalkable(sCenter.toWalkPosition())) {
						
						u.move(sCenter);
						continue;
					}
				}
				if(u.getType() == UnitType.Terran_Medic && u.getOrder() != Order.MedicHeal) {
					Unit chosen = getHealTarget(u, marinesToHeal);
					if(chosen != null) {
						u.useTech(TechType.Healing, chosen);
						marinesToHeal.add(chosen);
						continue;
					}
				}
				if(u.isIdle() && attack != Position.None && frameCount != u.getLastCommandFrame() && getGs().broodWarDistance(attack, u.getPosition()) > 500) {
					u.attack(attack);
					continue;
				}
				if(u.isAttacking() && attack == Position.None && frameCount != u.getLastCommandFrame() && getGs().broodWarDistance(sCenter, u.getPosition()) > 500) {
					u.move(sCenter);
					continue;
				}
				int framesToOrder = 18;	
				if(u.getType() == UnitType.Terran_Vulture) {
					framesToOrder = 12;
				}
				if(frameCount - u.getLastCommandFrame() >= framesToOrder) {
					if(u.isIdle() && attack != Position.None && status != Status.IDLE) {
						u.attack(attack);
						continue;
					}
					//Experimental storm dodging?
					if(u.isUnderStorm()) {
						u.move(start);
						continue;
					}
					Set<Unit> enemyToKite = new HashSet<>();
					if(u.getGroundWeaponCooldown() > 0) {
						for(Unit e : enemy) {
							if(!e.getType().isFlyer() && e.getType().groundWeapon().maxRange() <= 32  && e.getType() != UnitType.Terran_Medic) {
								if (e.isAttacking()) {
									if(u.getUnitsInRadius(u.getType().groundWeapon().maxRange()).contains(e)) {
										//u.move(start);
										enemyToKite.add(e);
									}
								}
							}
						}
						if(!enemyToKite.isEmpty()) {
							Position run = getGs().kiteAway(u,enemyToKite);
							if(run.isValid()) {
								u.move(run);
								continue;
							} else {
								u.move(getGs().getPlayer().getStartLocation().toPosition());
								continue;
							}
//							Position run = getGs().getPlayer().getStartLocation().toPosition();
							
						}
					}
					else if(attack != Position.None && !u.isStartingAttack() && !u.isAttacking() && u.getOrder() == Order.Move) {
						u.attack(attack);
						continue;
					}
				}
			}
		} catch(Exception e) {
			System.err.println("microUpdateOrder Error");
			System.err.println(e);
		}
		
	}
	
	private Unit getHealTarget(final Unit u, final Set<Unit> marinesToHeal) {
		Set<Unit> marines = getMarines();
		Unit chosen = null;
		double dist = Double.MAX_VALUE;
		for(Unit m : marines) {
			if(m.getHitPoints() == m.getType().maxHitPoints() || marinesToHeal.contains(m)) {
				continue;
			}
			double distA = getGs().broodWarDistance(m.getPosition(), u.getPosition());
			if(chosen == null || distA < dist) {
				chosen = m;
				dist = distA;
			}
		}
		return chosen;
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
	
	public Set<Unit> getMedics() {
		Set<Unit> aux = new HashSet<Unit>();
		for(Unit u : this.members) {
			if(u.getType() == UnitType.Terran_Medic) {
				aux.add(u);
			}
		}
		return aux;
	}

	public void giveMoveOrder(Position retreat) {
		int frameCount = getGs().frameCount;
		for(Unit u : members) {
			if(u.getType() == UnitType.Terran_Siege_Tank_Siege_Mode && u.getOrder() == Order.Unsieging) {
				continue;
			}
			if(u.getType() == UnitType.Terran_Siege_Tank_Tank_Mode && u.getOrder() == Order.Sieging) {
				continue;
			}
			Position lastTarget = (u.getTargetPosition() == null ? u.getOrderTargetPosition() : u.getTargetPosition());
			if(lastTarget != null) {
				if(lastTarget.equals(retreat)) {
					continue;
				}
			}
			if(attack != Position.None && frameCount != u.getLastCommandFrame()) {
				u.move(retreat);
			}
		}
	}
}
