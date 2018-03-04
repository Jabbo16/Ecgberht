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
			
			int frameCount = getGame().getFrameCount();
			if(this.members.isEmpty() || frameCount % getGs().getGame().getLatencyFrames() == 0) {
				return;
			}
			Position sCenter = getGs().getSquadCenter(this);
			Position mainCC = getGs().MainCC.getPosition();
			Set<Unit> closeSim = new HashSet<>();
			Set<Unit> copy = new HashSet<>();
			for(Unit u : getGs().enemyCombatUnitMemory) {
				if(getGs().broodWarDistance(sCenter, u.getPosition()) <= 600) {
					copy.add(u);
					if(u.getType().isWorker() && !u.isAttacking()) {
						continue;
					}
					closeSim.add(u);
				}
			}
//			for(EnemyBuilding u : getGs().enemyBuildingMemory.values()) {
//				if(u.type.canAttack() || u.type == UnitType.Terran_Bunker) {
//					if(getGs().broodWarDistance(sCenter, u.pos.toPosition()) <= 600) {
//						copy.add(u.unit);
//						closeSim.add(u.unit);
//					}
//				}
//				
//			}
			Set<Unit> aux = new HashSet<>();
			if(getGs().squads.size() > 1) {
				for(Squad s : getGs().squads.values()) {
					if(s.name != this.name) {
						if(getGs().broodWarDistance(sCenter, getGs().getSquadCenter(s)) <= 400) {
							aux.addAll(s.members);
						}
					}
				}
			}
			
			aux.addAll(this.members);
			boolean win = true;
			if(!closeSim.isEmpty()) {
				win = getGs().simulateBattle(aux, closeSim, 200);
			}
			Set<Unit> marinesToHeal = new HashSet<>();
			
			for(Unit u : members) {
				if(u.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
					continue;
				}
				if(u.getType() == UnitType.Terran_Siege_Tank_Tank_Mode && u.getOrder() == Order.Sieging) {
					continue;
				}
				if(frameCount == u.getLastCommandFrame()) {
					continue;
				}
				Position lastTarget = u.getOrderTargetPosition();
				Position lastCommandTarget = u.getLastCommand().getTargetPosition();
				
				if((u.getOrder() == Order.AttackMove || u.getOrder() == Order.Move ) && copy.isEmpty()) {
					continue;
				}
				
				if(getGs().broodWarDistance(sCenter, u.getPosition()) > 120 && getGame().isWalkable(sCenter.toWalkPosition()) && closeSim.isEmpty()) {
					u.move(sCenter);
					continue;
				}
				
				
				if(u.isIdle() && attack != Position.None && getGs().broodWarDistance(sCenter, attack) > 150) {
					if(lastTarget != null) {
						if(attack.equals(lastTarget)) {
							continue;
						}
					}
					if(lastCommandTarget != null) {
						if(attack.equals(lastCommandTarget)) {
							continue;
						}
					}
					u.attack(attack);
					continue;
				}
				if(u.getType() == UnitType.Terran_Medic && u.getOrder() != Order.MedicHeal && u.getOrder() != Order.MedicHealToIdle) {
					Unit chosen = getHealTarget(u, marinesToHeal);
					if(chosen != null) {
						u.useTech(TechType.Healing, chosen);
						marinesToHeal.add(chosen);
						continue;
					}
				}
				
				if(frameCount - u.getLastCommandFrame() >= 1) {
					if(u.isIdle() && attack != Position.None && status != Status.IDLE) {
						if(lastTarget != null) {
							if(attack.equals(lastTarget)) {
								continue;
							}
						}
						if(lastCommandTarget != null) {
							if(attack.equals(lastCommandTarget)) {
								continue;
							}
						}
						u.attack(attack);
						continue;
					}
					//Experimental storm dodging?
					if(u.isUnderStorm()) {
						if(lastTarget != null) {
							if(mainCC.equals(lastTarget)) {
								continue;
							}
						}
						if(lastCommandTarget != null) {
							if(mainCC.equals(lastCommandTarget)) {
								continue;
							}
						}
						u.move(mainCC);
						continue;
					}
					if(win) {
						Set<Unit> enemyToKite = new HashSet<>();
						if(u.getGroundWeaponCooldown() > 0) {
							for(Unit e : copy) {
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
									continue;
								} else {
									if(lastTarget != null) {
										if(mainCC.equals(lastTarget)) {
											continue;
										}
									}
									if(lastCommandTarget != null) {
										if(mainCC.equals(lastCommandTarget)) {
											continue;
										}
									}
									u.move(mainCC);
									continue;
								}
//								Position run = getGs().getPlayer().getStartLocation().toPosition();
								
							}
						}
						else if(attack != Position.None && !u.isStartingAttack() && !u.isAttacking() && u.getOrder() == Order.Move) {
							if(lastTarget != null) {
								if(attack.equals(lastTarget)) {
									continue;
								}
							}
							if(lastCommandTarget != null) {
								if(attack.equals(lastCommandTarget)) {
									continue;
								}
							}
							u.attack(attack);
							continue;
						}
					}else {
						if(getGs().broodWarDistance(sCenter, mainCC) > 50) {
							if(u.getOrder() == Order.Move) {
								if(lastTarget != null) {
									if(mainCC.equals(lastTarget)) {
										continue;
									}
								}
								if(lastCommandTarget != null) {
									if(mainCC.equals(lastCommandTarget)) {
										continue;
									}
								}
								u.move(mainCC);
								continue;
							}
						}
						
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
