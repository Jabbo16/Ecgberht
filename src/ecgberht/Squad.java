package ecgberht;

import static ecgberht.Ecgberht.getGame;
import static ecgberht.Ecgberht.getGs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bwapi.Order;
import bwapi.Pair;
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
		int frameCount = getGame().getFrameCount();
		if(frameCount - lastFrameOrder > 0 && !pos.equals(attack)) {
			for(Unit u : members) {
				if(u.getType() == UnitType.Terran_Siege_Tank_Siege_Mode && u.getOrder() == Order.Unsieging) {
					continue;
				}
				if(u.getType() == UnitType.Terran_Siege_Tank_Tank_Mode && u.getOrder() == Order.Sieging) {
					continue;
				}
				if(frameCount - u.getLastCommandFrame() > 24 ) {
					if(!u.isStartingAttack() && !u.isAttacking() || u.isIdle()) {
						u.attack(pos);
						continue;
					}
				}
			}
			attack = pos;
			lastFrameOrder = frameCount;
		}
	}
	
	public void giveStimOrder() {
		for(Unit u : members) {
			if(u.canUseTech(TechType.Stim_Packs) && !u.isStimmed() && u.isAttacking() && u.getHitPoints() >= 25) {
				u.useTech(TechType.Stim_Packs);
			}
		}
	}
	
	public void microUpdateOrder() {
		try {
			Set<Unit> enemy = getGs().enemyCombatUnitMemory;
			int frameCount = getGame().getFrameCount();
			Position start = getGame().self().getStartLocation().toPosition();
			Set<Unit> marinesToHeal = new HashSet<>();
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
				if(frameCount - u.getLastCommandFrame() >= 18) {
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
							Position run = kiteAway(u,enemyToKite);
							if(run.isValid()) {
								u.move(run);
							} else {
								u.move(getGs().getPlayer().getStartLocation().toPosition());
							}
//							Position run = getGs().getPlayer().getStartLocation().toPosition();
							
						}
					}
					else if(attack != Position.None && !u.isStartingAttack() && !u.isAttacking() && u.getOrder() == Order.Move) {
						u.attack(attack);
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
		int frameCount = getGame().getFrameCount();
		for(Unit u : members) {
			if(u.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
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
	
	// Credits to @Yegers for a better kite method
	private Position kiteAway(final Unit unit, final Set<Unit> enemies) {
	    if (enemies.isEmpty()) {
	        return null;
	    }
	    final Position ownPosition = unit.getPosition();
	    //TODO add walls
	    final List<Pair<Double, Double>> vectors = new ArrayList<>();

	    double minDistance = Double.MAX_VALUE;
	    for (final Unit enemy : enemies) {
	        final Position enemyPosition = enemy.getPosition();
	        final Pair<Double, Double> unitV = new Pair<>((double)(ownPosition.getX() - enemyPosition.getX()),(double) (ownPosition.getY() - enemyPosition.getY()));
	        final double distance = ownPosition.getDistance(enemyPosition);
	        if (distance < minDistance) {
	            minDistance = distance;
	        }
	        unitV.first = (1/distance) * unitV.first;
	        unitV.second = (1/distance) * unitV.second;
	        vectors.add(unitV);
	    }
	    minDistance = 2 * minDistance * minDistance;
	    for (final Pair<Double, Double> vector : vectors){
	        vector.first *= minDistance;
	        vector.second *= minDistance;
	    }
	    Pair<Double,Double> sumAll = Util.sumPosition(vectors);
	    return Util.sumPosition(ownPosition, new Position((int)(sumAll.first / vectors.size()),(int) (sumAll.second / vectors.size())));
	}
}
