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
	
	public JFAPUnit() {
		
	}
	
	public JFAPUnit(Unit u) {
		x = u.getX();
		y = u.getY();
		health = u.getHitPoints();
		maxHealth = u.getType().maxHitPoints();
		armor = u.getPlayer().armor(u.getType());
		shields = u.getShields();
		shieldArmor = u.getPlayer().getUpgradeLevel(UpgradeType.Protoss_Plasma_Shields);
		maxShields = u.getType().maxShields();
		speed = u.getPlayer().topSpeed(u.getType());
		flying = u.getType().isFlyer();
		groundDamage = u.getPlayer().damage(u.getType().groundWeapon());
		groundCooldown = u.getType().groundWeapon().damageFactor() > 0 && u.getType().maxGroundHits() > 0 ? u.getPlayer().weaponDamageCooldown(u.getType()) /
						(u.getType().groundWeapon().damageFactor() * u.getType().maxGroundHits()) : 0;
		groundMaxRange = u.getPlayer().weaponMaxRange(u.getType().groundWeapon());
		groundMinRange = u.getType().groundWeapon().minRange();
		groundDamageType = u.getType().groundWeapon().damageType();
		airDamage = u.getPlayer().damage(u.getType().airWeapon());
		airCooldown = u.getType().airWeapon().damageFactor() > 0 && u.getType().maxAirHits() > 0 ? u.getType().airWeapon().damageCooldown() /
				u.getType().airWeapon().damageFactor() * u.getType().maxAirHits() : 0;
		airMaxRange = u.getPlayer().weaponMaxRange(u.getType().airWeapon());
		airMinRange = u.getType().airWeapon().minRange();
		airDamageType = u.getType().airWeapon().damageType();
		unitType = u.getType();
		player = u.getPlayer();
		isOrganic = u.getType().isOrganic();
		score = u.getType().destroyScore();
		doThings(u, JFAP.game);
	}

	private void doThings(Unit u, Game game) {
		int nextId = 0;
		id = nextId++;
		if(u.getType() == UnitType.Protoss_Carrier) {
			groundDamage = u.getPlayer().damage(UnitType.Protoss_Interceptor.groundWeapon());
			if (u != null && u.isVisible()) {
				final int interceptorCount = u.getInterceptorCount();
				if (interceptorCount > 0) {
					groundCooldown = (int)(Math.round(37.0f / interceptorCount));
				} else {
					groundDamage = 0;
					groundCooldown = 5;
				}
			} else {
				if (u.getPlayer() != null) {
					groundCooldown = (int)(Math.round(37.0f / (u.getPlayer().getUpgradeLevel(UpgradeType.Carrier_Capacity) == 1 ? 8 : 4)));
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
		else if(u.getType() == UnitType.Terran_Bunker) {
			groundDamage = u.getPlayer().damage(WeaponType.Gauss_Rifle);
			groundCooldown = UnitType.Terran_Marine.groundWeapon().damageCooldown() / 4;
			groundMaxRange = u.getPlayer().weaponMaxRange(UnitType.Terran_Marine.groundWeapon()) + 32;
			airDamage = groundDamage;
			airCooldown = groundCooldown;
			airMaxRange = groundMaxRange;
		}
		else if(u.getType() == UnitType.Protoss_Reaver) {
			groundDamage = u.getPlayer().damage(WeaponType.Scarab);
		}
		if (u != null && u.isStimmed()) {
			groundCooldown /= 2;
			airCooldown /= 2;
		}
		if (u != null && u.isVisible() && !u.isFlying()) {
			elevation = game.getGroundHeight(u.getTilePosition());
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
