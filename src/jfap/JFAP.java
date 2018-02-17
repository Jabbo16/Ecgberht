package jfap;

import java.util.ArrayList;
import java.util.List;

import bwapi.DamageType;
import bwapi.Game;
import bwapi.Pair;
import bwapi.Position;
import bwapi.Race;
import bwapi.UnitSizeType;
import bwapi.UnitType;

public class JFAP extends AJFAP{
	public static Game game;
	private List<JFAPUnit> player1 = new ArrayList<JFAPUnit>();
	private List<JFAPUnit> player2 = new ArrayList<JFAPUnit>();
	private boolean didSomething = false;
	private int nFrames = 96;
	
	public JFAP(Game game) {
		JFAP.game = game;
	}

	@Override
	public void addUnitPlayer1(JFAPUnit fu) {
		player1.add(fu);

	}

	@Override
	public void addIfCombatUnitPlayer1(JFAPUnit fu) {
		if (fu.unitType == UnitType.Protoss_Interceptor) {
			return;
		}
		if (fu.groundDamage > 0  || fu.airDamage > 0  || fu.unitType == UnitType.Terran_Medic) {
			addUnitPlayer1(fu);
		}
	}

	@Override
	public void addUnitPlayer2(JFAPUnit fu) {
		player2.add(fu);
	}

	@Override
	public void addIfCombatUnitPlayer2(JFAPUnit fu) {
		if (fu.groundDamage > 0 || fu.airDamage > 0 || fu.unitType == UnitType.Terran_Medic) {
			addUnitPlayer2(fu);
		}
	}

	@Override
	public void simulate(int nFrames) {
		while (nFrames > 0) {
			if (player1.isEmpty() || player2.isEmpty()) {
				break;
			}	    	
			didSomething = false;
			isimulate();

			if (!didSomething) {
				break;
			}
			nFrames--;
		}
	}
	
	@Override
	public void simulate() {
		int nFrames = this.nFrames;
		while (nFrames > 0) {
			if (player1.isEmpty() || player2.isEmpty()) {
				break;
			}	    	
			didSomething = false;
			isimulate();

			if (!didSomething) {
				break;
			}
			nFrames--;
		}
	}
	
	final int score(final JFAPUnit fu) {
		if (fu.health > 0 && fu.maxHealth > 0) {
			int bunker = 0;
			if((fu.unitType == UnitType.Terran_Bunker)) {
				bunker = 1;
			}
			return ((fu.score * fu.health) / (fu.maxHealth * 2)) + bunker * UnitType.Terran_Marine.destroyScore() * 4;
		}    
		return 0;
	}

	@Override
	public Pair<Integer, Integer> playerScores() {
		Pair<Integer, Integer> res = new Pair<>(0,0);
		for (final JFAPUnit u : player1) {
			res.first += score(u);
		}
		for (final JFAPUnit u : player2) {
			res.second += score(u);
		}
		return res;
	}

	@Override
	Pair<Integer, Integer> playerScoresUnits() {
		Pair<Integer, Integer> res = new Pair<>(0,0);
		for (final JFAPUnit u : player1) {
			if (!u.unitType.isBuilding()) {
				res.first += score(u);
			}    
		}
		for (final JFAPUnit u : player2) {
			if (!u.unitType.isBuilding()) {
				res.second += score(u);
			}
		}
		return res;
	}

	@Override
	Pair<Integer, Integer> playerScoresBuildings() {
		Pair<Integer, Integer> res = new Pair<>(0,0);
		for (final JFAPUnit u : player1) {
			if (u.unitType.isBuilding()) {
				res.first += score(u);
			}    
		}
		for (final JFAPUnit u : player2) {
			if (u.unitType.isBuilding()) {
				res.second += score(u);
			}
		}
		return res;
	}

	@Override
	public Pair<List<JFAPUnit>, List<JFAPUnit>> getState() {
		return new Pair<List<JFAPUnit>, List<JFAPUnit>>(player1,player2);
	}

	@Override
	public void clear() {
		player1.clear();
		player2.clear();
	}

	@Override
	void dealDamage(JFAPUnit fu, int damage, DamageType damageType) {
		damage <<= 8;
		final int remainingShields = fu.shields - damage + (fu.shieldArmor << 8);
		if (remainingShields > 0) {
			fu.shields = remainingShields;
			return;
		} else if (fu.shields > 0) {
			damage -= fu.shields + (fu.shieldArmor << 8);
			fu.shields = 0;
		}

		if (damage == 0)
			return;

		damage -= fu.armor << 8;
		if(damageType == DamageType.Concussive) {
			if (fu.unitSize == UnitSizeType.Large) {
				damage = damage / 4;
			}
			else if (fu.unitSize == UnitSizeType.Medium) {
				damage = damage / 2;
			} 
		} else if (damageType == DamageType.Explosive) {
			if (fu.unitSize == UnitSizeType.Small)
				damage = damage / 2;
			else if (fu.unitSize == UnitSizeType.Medium) {
				damage = (damage * 3) / 4;
			}
		}
		fu.health -= Math.max(128, damage);
	}

	@Override
	int distButNotReally(JFAPUnit u1, JFAPUnit u2) {
		return (u1.x - u2.x) * (u1.x - u2.x) + (u1.y - u2.y) * (u1.y - u2.y);
	}

	@Override
	boolean isSuicideUnit(UnitType ut) {
		return (ut == UnitType.Zerg_Scourge ||
				ut == UnitType.Terran_Vulture_Spider_Mine ||
				ut == UnitType.Zerg_Infested_Terran ||
				ut == UnitType.Protoss_Scarab);
	}

	@Override
	void unitsim(JFAPUnit fu, List<JFAPUnit> enemyUnits) {
		if (fu.attackCooldownRemaining > 0) {
			didSomething = true;
			return;
		}
		JFAPUnit closestEnemy = null;
		int closestDist = 0;
		for (JFAPUnit enemy : enemyUnits) {
			if (enemy.flying) {
				if (fu.airDamage > 0) {
					final int d = distButNotReally(fu, enemy);
					if ((closestEnemy == null || d < closestDist) && d >= fu.airMinRange) {
						closestDist = d;
						closestEnemy = enemy;
					}
				}
			} else {
				if (fu.groundDamage > 0) {
					final int d = distButNotReally(fu, enemy);
					if ((closestEnemy == null || d < closestDist) && d >= fu.groundMinRange) {
						closestDist = d;
						closestEnemy = enemy;
					}
				}
			}
		}
		if (closestEnemy != null && Math.sqrt(closestDist) <= fu.speed && !(fu.x == closestEnemy.x && fu.y == closestEnemy.y)) {
			fu.x = closestEnemy.x;
			fu.y = closestEnemy.y;
			closestDist = 0;
			didSomething = true;
		}
		if (closestEnemy != null && closestDist <= (closestEnemy.flying ? fu.groundMaxRange : fu.airMinRange)) {
			if (closestEnemy.flying) {
				dealDamage(closestEnemy, fu.airDamage, fu.airDamageType);
				fu.attackCooldownRemaining = fu.airCooldown;
			}
			else {
				dealDamage(closestEnemy, fu.groundDamage, fu.groundDamageType);
				fu.attackCooldownRemaining = fu.groundCooldown;
				if (fu.elevation != -1 && closestEnemy.elevation != -1) {
					if (closestEnemy.elevation > fu.elevation) {
						fu.attackCooldownRemaining += fu.groundCooldown;
					}
				}	  
			}
			if (closestEnemy.health < 1) {
				final JFAPUnit temp = closestEnemy;
				enemyUnits.remove(closestEnemy);
				unitDeath(temp, enemyUnits);
			}
			didSomething = true;
		} else if (closestEnemy != null && Math.sqrt(closestDist) > fu.speed) {
			final int dx = closestEnemy.x - fu.x;
			final int dy = closestEnemy.y - fu.y;
			fu.x += (int)(dx * (fu.speed / Math.sqrt(dx * dx + dy * dy)));
			fu.y += (int)(dy * (fu.speed / Math.sqrt(dx * dx + dy * dy)));
			didSomething = true;
		}
	}

	@Override
	void medicsim(JFAPUnit fu, List<JFAPUnit> friendlyUnits) {
		JFAPUnit closestHealable = null;
		int closestDist = 0;
		for (JFAPUnit friendlyUnit : friendlyUnits) {
			if (friendlyUnit.isOrganic && friendlyUnit.health < friendlyUnit.maxHealth && !friendlyUnit.didHealThisFrame) {
				final int d = distButNotReally(fu, friendlyUnit);
				if (closestHealable == null || d < closestDist) {
					closestHealable = friendlyUnit;
					closestDist = d;
				}
			}
		}
		if (closestHealable != null) {
			fu.x = closestHealable.x;
			fu.y = closestHealable.y;
			closestHealable.health += 150;
			if (closestHealable.health > closestHealable.maxHealth) {
				closestHealable.health = closestHealable.maxHealth;
			}
			closestHealable.didHealThisFrame = true;
		}
	}

	@Override
	boolean suicideSim(JFAPUnit fu, List<JFAPUnit> enemyUnits) {
		JFAPUnit closestEnemy = null;
		int closestDist = 0;
		for (JFAPUnit enemy : enemyUnits) {
			if (enemy.flying) {
				if (fu.airDamage > 0) {
					final int d = distButNotReally(fu, enemy);
					if ((closestEnemy == null || d < closestDist) && d >= fu.airMinRange) {
						closestDist = d;
						closestEnemy = enemy;
					}
				}
			} else { 
				if (fu.groundDamage > 0) {

					int d = distButNotReally(fu, enemy);
					if ((closestEnemy == null || d < closestDist) && d >= fu.groundMinRange) {
						closestDist = d;
						closestEnemy = enemy;
					}
				}
			}
		}
		if (closestEnemy != null && Math.sqrt(closestDist) <= fu.speed) {
			if (closestEnemy.flying) {
				dealDamage(closestEnemy, fu.airDamage, fu.airDamageType);
			}
			else {
				dealDamage(closestEnemy, fu.groundDamage, fu.groundDamageType);
			}
			if (closestEnemy.health < 1) {
				final JFAPUnit temp = closestEnemy;
				enemyUnits.remove(closestEnemy);
				closestEnemy = null;
				unitDeath(temp, enemyUnits);
			}

			didSomething = true;
			return true;
		} else {
			if (closestEnemy != null && Math.sqrt(closestDist) > fu.speed) {

				final int dx = closestEnemy.x - fu.x;
				final int dy = closestEnemy.y - fu.y;

				fu.x += (int)(dx * (fu.speed / Math.sqrt(dx * dx + dy * dy)));
				fu.y += (int)(dy * (fu.speed / Math.sqrt(dx * dx + dy * dy)));
				didSomething = true;
			}
		}
		return false;
	}
	
	final void simUnit(JFAPUnit unit, List<JFAPUnit> friendly, List<JFAPUnit> enemy) {
		if(isSuicideUnit(unit.unitType)) {
			final boolean unitDied = suicideSim(unit, enemy);
			if (unitDied) {
				friendly.remove(unit);
			}
		} else {
			if (unit.unitType == UnitType.Terran_Medic)
				medicsim(unit, friendly);
			else {
				unitsim(unit, enemy);
			} 
		}
	}

	final void updateUnit(JFAPUnit fu) {
		if (fu.attackCooldownRemaining > 0) {
			--fu.attackCooldownRemaining;
		}

		if (fu.didHealThisFrame) {
			fu.didHealThisFrame = false;
		}

		if (fu.unitType.getRace() == Race.Zerg) {
			if (fu.health < fu.maxHealth) {
				fu.health += 4;
			}
			if (fu.health > fu.maxHealth) {
				fu.health = fu.maxHealth;
			}
		}
		else if (fu.unitType.getRace() == Race.Protoss) {
			if (fu.shields < fu.maxShields) {
				fu.shields += 7;
			}
			if (fu.shields > fu.maxShields) {
				fu.shields = fu.maxShields;
			}	
		}
	}

	@Override
	void isimulate() {
		for (JFAPUnit fu : player1) {
			simUnit(fu, player1, player2);
		}

		for (JFAPUnit fu : player2) {
			simUnit(fu, player2, player1);
		}

		for (JFAPUnit fu : player1) {
			updateUnit(fu);
		}

		for (JFAPUnit fu : player2) {
			updateUnit(fu);
		}
	}

	@Override
	void unitDeath(JFAPUnit fu, List<JFAPUnit> itsFriendlies) {
		if (fu.unitType == UnitType.Terran_Bunker) {
			convertToUnitType(fu, UnitType.Terran_Marine);
			for (int i = 0; i < 4; ++i) {
				itsFriendlies.add(fu);
			}
		}
	}

	@Override
	void convertToUnitType(JFAPUnit fu, UnitType ut) {
		EnemyData ed = new EnemyData();
		ed.lastPosition = new Position(fu.x, fu.y);
		ed.lastPlayer = fu.player;
		ed.lastType = ut;
		JFAPUnit funew = new JFAPUnit(ed);
		funew.attackCooldownRemaining = fu.attackCooldownRemaining;
		funew.elevation = fu.elevation;
		fu = funew;

	}
}
