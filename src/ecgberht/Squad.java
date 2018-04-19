package ecgberht;

import static ecgberht.Ecgberht.getGame;
import static ecgberht.Ecgberht.getGs;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import bwapi.Order;
import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;

public class Squad {

	public enum Status {
		ATTACK, IDLE, DEFENSE
	}

	public int lastFrameOrder = 0;
	public Position attack;
	public Set<Unit> members;
	public Status status;
	public String name;

	public Squad(String name) {
		this.name = name;
		members = new TreeSet<>(new UnitComparator());
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
				if(getGs().getGame().elapsedTime() > getGs().lastFrameStim + 65) {
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
				if(status == Status.IDLE) {
					if(!getGs().DBs.isEmpty()) {
						Unit bunker = getGs().DBs.keySet().iterator().next();
						if(bunker.exists() && getGs().broodWarDistance(bunker.getPosition(), sCenter) >= 170 && getGs().getArmySize() < getGs().strat.armyForAttack && !getGs().expanding && getGs().strat.name != "ProxyBBS") {
							if(u.getOrder() != Order.Move) {
								u.move(bunker.getPosition());
							}
							continue;
						}
					}
					else if(getGs().closestChoke != null && !getGs().EI.naughty && getGs().strat.name != "ProxyBBS") {
						if(getGs().broodWarDistance(getGs().closestChoke.getCenter(), sCenter) >= 200  && getGs().getArmySize() < getGs().strat.armyForAttack  && !getGs().expanding ) {
							if(u.getOrder() != Order.Move) {
								u.move(getGs().closestChoke.getCenter());
							}
							continue;
						}
					}

					if(getGs().broodWarDistance(u.getPosition(), sCenter) >= 200 && u.getOrder() != Order.Move) {
						if(getGame().isWalkable(sCenter.toWalkPosition())) {
							u.move(sCenter);
							continue;
						}
					}
				}

				// Experimental
				if(status == Status.ATTACK && getGs().getGame().isWalkable(sCenter.toWalkPosition()) && frameCount % 35 == 0) {
					if(members.size() == 1) {
						continue;
					}
					boolean gaveOrder = false;
					List<Unit> circle = getGs().getGame().getUnitsInRadius(sCenter, 280);
					Set<Unit> different = new HashSet<>();
					different.addAll(circle);
					different.addAll(members);
					circle.retainAll(members);
					different.removeAll(circle);
					if(circle.size() != members.size()) {
						for(Unit m : different) {
							if(m.equals(u)) {
								if(u.getOrderTargetPosition() != null) {
									if(!u.getOrderTargetPosition().equals(sCenter) && getGame().isWalkable(sCenter.toWalkPosition())) {
										u.attack(sCenter);
										gaveOrder = true;
										break;
									}
								}
							}
						}
					}
					if(gaveOrder) continue;
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

				Position lastTarget = (u.getTargetPosition() == null ? u.getOrderTargetPosition() : u.getTargetPosition());
				if(lastTarget != null) {
					if(lastTarget.equals(attack)) {
						continue;
					}
				}
				int framesToOrder = 18;
				if(u.getType() == UnitType.Terran_Vulture) {
					framesToOrder = 12;
				}
				if(frameCount - u.getLastCommandFrame() >= framesToOrder) {
					if(u.isIdle() && attack != Position.None && status != Status.IDLE) {
						lastTarget = (u.getTargetPosition() == null ? u.getOrderTargetPosition() : u.getTargetPosition());
						if(lastTarget != null) {
							if(!lastTarget.equals(attack)) {
								u.attack(attack);
								continue;
							}
						}
					}
					//Experimental storm dodging?
					if(u.isUnderStorm()) {
						u.move(start);
						continue;
					}
					Set<Unit> enemyToKite = new HashSet<>();
					Set<Unit> enemyToAttack = new HashSet<>();
					for(Unit e : enemy) {
						UnitType eType = e.getType();
						if(eType == UnitType.Zerg_Larva || eType == UnitType.Zerg_Overlord) continue;
						enemyToAttack.add(e);
						if(!e.getType().isFlyer() && e.getType().groundWeapon().maxRange() <= 32  && e.getType() != UnitType.Terran_Medic) {
//							if (e.isAttacking()) {
//								if(u.getUnitsInRadius(u.getType().groundWeapon().maxRange()).contains(e)) {
									//u.move(start);
									if(getGs().broodWarDistance(u.getPosition(), e.getPosition()) <= u.getType().groundWeapon().maxRange()) {
										enemyToKite.add(e);
									}
//								}
//							}
						}
					}
					for(EnemyBuilding b : getGs().enemyBuildingMemory.values()) {
						if(b.type.canAttack()) {
							enemyToAttack.add(b.unit);
						}
					}
					if(u.getGroundWeaponCooldown() > 0) {
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
					else if(attack != Position.None && !u.isStartingAttack() && !u.isAttacking()) {
//					else if(attack != Position.None && !u.isStartingAttack() && !u.isAttacking() && u.getOrder() == Order.Move) {
						if(!enemyToAttack.isEmpty()) {
							Unit target = Util.getTarget(u, enemyToAttack);
							Unit lastTargetUnit = (u.getTarget() == null ? u.getOrderTarget() : u.getTarget());
							if(lastTargetUnit != null) {
								if(!lastTargetUnit.equals(target)) {
									u.attack(target);
									continue;
								}
							}
						}
						if(u.getOrder() == Order.Move){
							u.attack(attack);
							continue;
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


	@Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof Squad)) {
            return false;
        }
        Squad s = (Squad) o;
        return name.equals(s.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
