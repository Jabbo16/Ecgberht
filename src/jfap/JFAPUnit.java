package jfap;

import bwapi.DamageType;
import bwapi.Game;
import bwapi.Player;
import bwapi.Race;
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
	Race race = Race.Unknown;
	
	public JFAPUnit() {
		
	}
	
	public JFAPUnit(Unit u) {
		x = u.getX();
		y = u.getY();
		UnitType auxType = u.getType();
		Player auxPlayer = u.getPlayer();
		health = u.getHitPoints();
		maxHealth = auxType.maxHitPoints();
		armor = auxPlayer.armor(auxType);
		shields = u.getShields();
		shieldArmor = auxPlayer.getUpgradeLevel(UpgradeType.Protoss_Plasma_Shields);
		maxShields = auxType.maxShields();
		speed = auxPlayer.topSpeed(auxType);
		flying = auxType.isFlyer();
		groundDamage = auxPlayer.damage(auxType.groundWeapon());
		groundCooldown = auxType.groundWeapon().damageFactor() > 0 && auxType.maxGroundHits() > 0 ? auxPlayer.weaponDamageCooldown(auxType) /
						(auxType.groundWeapon().damageFactor() * auxType.maxGroundHits()) : 0;
		groundMaxRange = auxPlayer.weaponMaxRange(auxType.groundWeapon());
		groundMinRange = auxType.groundWeapon().minRange();
		groundDamageType = auxType.groundWeapon().damageType();
		airDamage = auxPlayer.damage(auxType.airWeapon());
		airCooldown = auxType.airWeapon().damageFactor() > 0 && auxType.maxAirHits() > 0 ? auxType.airWeapon().damageCooldown() /
				auxType.airWeapon().damageFactor() * auxType.maxAirHits() : 0;
		airMaxRange = auxPlayer.weaponMaxRange(auxType.airWeapon());
		airMinRange = auxType.airWeapon().minRange();
		airDamageType = auxType.airWeapon().damageType();
		unitType = auxType;
		player = auxPlayer;
		isOrganic = auxType.isOrganic();
		score = auxType.destroyScore();
		race = auxType.getRace();
		doThings(u, JFAP.game);
	}

	private void doThings(Unit u, Game game) {
		int nextId = 0;
		id = nextId++;
		if(unitType == UnitType.Protoss_Carrier) {
			groundDamage = player.damage(UnitType.Protoss_Interceptor.groundWeapon());
			if (u != null && u.isVisible()) {
				final int interceptorCount = u.getInterceptorCount();
				if (interceptorCount > 0) {
					groundCooldown = (int)(Math.round(37.0f / interceptorCount));
				} else {
					groundDamage = 0;
					groundCooldown = 5;
				}
			} else {
				if (player != null) {
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
		else if(unitType == UnitType.Terran_Bunker) {
			groundDamage = player.damage(WeaponType.Gauss_Rifle);
			groundCooldown = UnitType.Terran_Marine.groundWeapon().damageCooldown() / 4;
			groundMaxRange = player.weaponMaxRange(UnitType.Terran_Marine.groundWeapon()) + 32;
			airDamage = groundDamage;
			airCooldown = groundCooldown;
			airMaxRange = groundMaxRange;
		}
		else if(unitType == UnitType.Protoss_Reaver) {
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
