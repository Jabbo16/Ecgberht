package ecgberht.Agents;

import static ecgberht.Ecgberht.getGs;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import bwapi.Pair;
import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import ecgberht.EnemyBuilding;

public class Vulture {
	
	public Vulture(Unit unit) {
		this.unit = unit;
	}
	
	enum Status{
		ATTACK, KITE, COMBAT, IDLE, RETREAT
	}
	
	public Unit unit = null;
	UnitType type = UnitType.Terran_Vulture;
	boolean minesResearched = false;
	int mines = 3;
	Position attackPos = Position.None;
	Unit attackUnit = null;
	Status status = Status.IDLE;
	int frameLastOrder = 0;
	int actualFrame = 0;
	Set<Unit> closeEnemies = new HashSet<>();
	Set<Unit> closeWorkers = new HashSet<>();
	public void placeMine(Position pos) {
		unit.useTech(TechType.Spider_Mines, pos);
	}
	
	public String statusToString() {
		if(status == Status.ATTACK) {
			return "Attack";
		}
		if(status == Status.KITE) {
			return "Kite";
		}
		if(status == Status.COMBAT) {
			return "Combat";
		}
		if(status == Status.RETREAT) {
			return "Retreat";
		}
		if(status == Status.IDLE) {
			return "Idle";
		}
		return "None";
	}
	
	public boolean runAgent() {
		try {
			boolean remove = false;
			if(unit.getHitPoints() <= 15) {
				Position cc = getGs().MainCC.getPosition();
				if(cc != null) {
					unit.move(cc);
					
				} else {
					unit.move(getGs().getPlayer().getStartLocation().toPosition());
				}
				getGs().addToSquad(unit);
				return true;
			}
			actualFrame = getGs().getGame().getFrameCount();
			closeEnemies.clear();
			closeWorkers.clear();
			if(frameLastOrder == actualFrame) {
				return remove;
			}
			if (actualFrame % getGs().getGame().getLatencyFrames() == 0) {
				return remove;
			}
			Status old = status;
			getNewStatus();
			if(old == status && status != Status.COMBAT && status != Status.ATTACK) {
				return remove;
			}
			if(status != Status.COMBAT) {
				attackUnit = null;
			}
			if(status == Status.ATTACK && unit.isIdle()) {
				Pair<Integer, Integer> pos = getGs().inMap.getPosition(unit.getTilePosition(), true);
				if(pos != null) {
					if(pos.first != null && pos.second != null) {
						unit.attack(new Position(pos.first,pos.second));
						return remove;
					}
				}
			}
			switch(status) {
			case ATTACK:
				attack();
				break;

			case COMBAT:
				combat();
				break;
				
			case KITE:
				kite();
				break;
				
			case RETREAT:
				retreat();
				break;
				
			default:
				break;
				
			}
			return remove;
		} catch(Exception e) {
			System.err.println("Exception Vulture");
			System.err.println(e);
		}
		return false;
	}

	private void combat() {
		Unit toAttack = getUnitToAttack(unit, closeEnemies);
		if(toAttack != null) {
			if(attackUnit != null) {
				if(attackUnit.equals(toAttack)) {
					return;
				}
			}
			unit.attack(toAttack);
			attackUnit = toAttack;
		}
		else {
			if(!closeWorkers.isEmpty()) {
				toAttack = getUnitToAttack(unit, closeWorkers);
				if(toAttack != null) {
					if(attackUnit != null) {
						if(attackUnit.equals(toAttack)) {
							return;
						}
					}
				}
			}
			unit.attack(toAttack);
			attackUnit = toAttack;
		}
		attackPos = Position.None;
	}

	private void getNewStatus() {
		
		Position myPos = unit.getPosition();
		if(getGs().enemyCombatUnitMemory.isEmpty()) {
			status = Status.ATTACK;
			return;
		}
		for(Unit u : getGs().enemyCombatUnitMemory) {
			if(u.getType().isWorker() && !u.isAttacking()) {
				closeWorkers.add(u);
			}
			if(getGs().broodWarDistance(u.getPosition(), myPos) < 600) {
				closeEnemies.add(u);
			}
		}
		for(EnemyBuilding u : getGs().enemyBuildingMemory.values()) {
			if((u.type.canAttack() || u.type == UnitType.Terran_Bunker) && u.unit.isCompleted()) {
				if(getGs().broodWarDistance(myPos, u.pos.toPosition()) <= 600) {
					closeEnemies.add(u.unit);
				}
			}
			
		}
		if(closeEnemies.isEmpty()) {
			status = Status.ATTACK;
			return;
		}
		else {
			boolean meleeOnly = checkOnlyMelees();
			int sim = 80;
			if(meleeOnly) {
				sim = 5;
			}
			if(!getGs().simulateHarass(unit, closeEnemies, sim)) {
				status = Status.RETREAT;
				return;
			}
			int cd = unit.getGroundWeaponCooldown();
			if(status == Status.COMBAT || status == Status.ATTACK) {
				if(cd > 0) {
					status = Status.KITE;
					return;
				}
			}
			if(status == Status.KITE) {
				if(cd == 0) {
					status = Status.COMBAT;
					return;
				}
			}
		}
		
	}

	private boolean checkOnlyMelees() {
		for(Unit e : closeEnemies) {
			if(e.getType().groundWeapon().maxRange() > 32 || e.getType() == UnitType.Terran_Bunker) {
				return false;
			}
		}
		return true;
	}

	private void retreat() {
		Unit CC = getGs().MainCC;
		if(CC != null){
			unit.move(CC.getPosition());
		} else {
			unit.move(getGs().getPlayer().getStartLocation().toPosition());
		}
		attackPos = Position.None;
		attackUnit = null;
	}

	private void kite() {
		Position kite = getGs().kiteAway(unit, closeEnemies);
		unit.move(kite);
		attackPos = Position.None;
		attackUnit = null;
	}

	private void attack() {
		Position newAttackPos = null;
		if(attackPos == Position.None) {
			newAttackPos = selectNewAttack();
			attackPos = newAttackPos;
			if(attackPos == null || !attackPos.isValid()) {
				attackUnit = null;
				attackPos = Position.None;
				return;
			}
			unit.attack(newAttackPos);
			attackUnit = null;
			return;
		}
		else if(attackPos.equals(newAttackPos)) {
			return;
		}
		
	}

	private Position selectNewAttack() {
		if(getGs().enemyBase != null) {
			return getGs().enemyBase.getPosition();
		}
		else {
			return getGs().EnemyBLs.get(1).getPosition();
		}
	}
	
	private Unit getUnitToAttack(Unit myUnit, Set<Unit> enemies) {
		Unit chosen = null;
		double distB = Double.MAX_VALUE;
		for(Unit u : enemies) {
			if(u.getType().isFlyer()) {
				continue;
			}
			double distA = getGs().broodWarDistance(myUnit.getPosition(), u.getPosition());
			if(chosen == null || distA < distB) {
				chosen = u;
				distB = distA;
			}
		}
		
		if(chosen != null) {
			return chosen;
		}
		
		return null;
	}
	
	@Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof Vulture)) {
            return false;
        }
        Vulture vulture = (Vulture) o;
        return unit.equals(vulture.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit);
    }
}
