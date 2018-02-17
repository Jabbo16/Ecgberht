package jfap;

import bwapi.DamageType;
import bwapi.Game;
import bwapi.Player;
import bwapi.Unit;
import bwapi.UnitSizeType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwapi.WeaponType;

public class JFAPUnit {
	
	int id = 0;
	int x = 0, y = 0;
	int health = 0;
	int maxHealth = 0;
	int armor = 0;
	int shields = 0;
	int shieldArmor = 0;
	int maxShields = 0;
	double speed = 0;
	boolean flying = false;
	int elevation = -1;
	UnitSizeType unitSize;
	int groundDamage = 0;
	int groundCooldown = 0;
	int groundMaxRange = 0;
	int groundMinRange = 0;
	DamageType groundDamageType;
	int airDamage = 0;
	int airCooldown = 0;
	int airMaxRange = 0;
	int airMinRange = 0;
	DamageType airDamageType;
	UnitType unitType;
	Player player = null;
	boolean isOrganic = false;
	boolean didHealThisFrame = false;
	int score = 0;
	int attackCooldownRemaining = 0;
	
	public JFAPUnit(final Unit u) {
		this(new EnemyData(u));
	}
	
	public JFAPUnit(EnemyData ed) {
		x = ed.lastPosition.getX();
		y = ed.lastPosition.getY();
		health = ed.expectedHealth();
		maxHealth = ed.lastType.maxHitPoints();
		armor = ed.lastPlayer.armor(ed.lastType);
		shields = ed.expectedShields();
		shieldArmor = ed.lastPlayer.getUpgradeLevel(UpgradeType.Protoss_Plasma_Shields);
		maxShields = ed.lastType.maxShields();
		speed = ed.lastPlayer.topSpeed(ed.lastType);
		flying = ed.lastType.isFlyer();
		groundDamage = ed.lastPlayer.damage(ed.lastType.groundWeapon());
		groundCooldown = ed.lastType.groundWeapon().damageFactor() > 0 && ed.lastType.maxGroundHits() > 0 ? ed.lastPlayer.weaponDamageCooldown(ed.lastType) /
						(ed.lastType.groundWeapon().damageFactor() * ed.lastType.maxGroundHits()) : 0;
		groundMaxRange = ed.lastPlayer.weaponMaxRange(ed.lastType.groundWeapon());
		groundMinRange = ed.lastType.groundWeapon().minRange();
		groundDamageType = ed.lastType.groundWeapon().damageType();
		airDamage = ed.lastPlayer.damage(ed.lastType.airWeapon());
		airCooldown = ed.lastType.airWeapon().damageFactor() > 0 && ed.lastType.maxAirHits() > 0 ? ed.lastType.airWeapon().damageCooldown() /
					  ed.lastType.airWeapon().damageFactor() * ed.lastType.maxAirHits() : 0;
		airMaxRange = ed.lastPlayer.weaponMaxRange(ed.lastType.airWeapon());
		airMinRange = ed.lastType.airWeapon().minRange();
		airDamageType = ed.lastType.airWeapon().damageType();
		unitType = ed.lastType;
		player = ed.lastPlayer;
		isOrganic = ed.lastType.isOrganic();
		score = ed.lastType.destroyScore();
		doThings(ed, JFAP.game);
	}

	private void doThings(EnemyData ed, Game game) {
		int nextId = 0;
		id = nextId++;
		if(ed.lastType == UnitType.Protoss_Carrier) {
			groundDamage = ed.lastPlayer.damage(UnitType.Protoss_Interceptor.groundWeapon());
			if (ed.u != null && ed.u.isVisible()) {
				final int interceptorCount = ed.u.getInterceptorCount();
				if (interceptorCount > 0) {
					groundCooldown = (int)(Math.round(37.0f / interceptorCount));
				} else {
					groundDamage = 0;
					groundCooldown = 5;
				}
			} else {
				if (ed.lastPlayer != null) {
					groundCooldown = (int)(Math.round(37.0f / (ed.lastPlayer.getUpgradeLevel(UpgradeType.Carrier_Capacity) == 1 ? 8 : 4)));
				} else {
					groundCooldown = (int)(Math.round(37.0f / 8));
				}
			}
			groundDamageType = UnitType.Protoss_Interceptor.groundWeapon().damageType();
			groundMaxRange = 32 * 8;
			airDamage = groundDamage;
			airDamageType = groundDamageType;
			airCooldown = groundCooldown;
			airMaxRange = groundMaxRange;
		}
		else if(ed.lastType == UnitType.Terran_Bunker) {
			groundDamage = ed.lastPlayer.damage(WeaponType.Gauss_Rifle);
			groundCooldown = UnitType.Terran_Marine.groundWeapon().damageCooldown() / 4;
			groundMaxRange = ed.lastPlayer.weaponMaxRange(UnitType.Terran_Marine.groundWeapon()) + 32;
			airDamage = groundDamage;
			airCooldown = groundCooldown;
			airMaxRange = groundMaxRange;
		}
		else if(ed.lastType == UnitType.Protoss_Reaver) {
			groundDamage = ed.lastPlayer.damage(WeaponType.Scarab);
		}
		if (ed.u != null && ed.u.isStimmed()) {
			groundCooldown /= 2;
			airCooldown /= 2;
		}
		if (ed.u != null && ed.u.isVisible() && !ed.u.isFlying()) {
			elevation = game.getGroundHeight(ed.u.getTilePosition());
		}
		groundMaxRange *= groundMaxRange;
		groundMinRange *= groundMinRange;
		airMaxRange *= airMaxRange;
		airMinRange *= airMinRange;
		health <<= 8;
		maxHealth <<= 8;
		shields <<= 8;
		maxShields <<= 8;
	}	 
}
