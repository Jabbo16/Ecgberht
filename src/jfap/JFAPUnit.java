package jfap;

import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.type.*;
import org.openbw.bwapi4j.unit.*;

import java.util.Objects;

public class JFAPUnit implements Comparable<JFAPUnit> {
    int id = 0;
    Unit unit;
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
    UnitSizeType unitSize = UnitSizeType.Unknown;
    int groundDamage = 0;
    int groundCooldown = 0;
    int groundMaxRange = 0;
    int groundMinRange = 0;
    DamageType groundDamageType = DamageType.Unknown;
    int airDamage = 0;
    int airCooldown = 0;
    int airMaxRange = 0;
    int airMinRange = 0;
    DamageType airDamageType = DamageType.Unknown;
    public UnitType unitType = UnitType.Unknown;
    Player player = null;
    boolean isOrganic = false;
    boolean didHealThisFrame = false;
    int score = 0;
    int attackCooldownRemaining = 0;
    Race race = Race.Unknown;

    public JFAPUnit(Unit u) {
        PlayerUnit pU = (PlayerUnit) u;
        unit = u;
        x = u.getX();
        y = u.getY();
        id = u.getId();
        UnitType auxType = Util.getType(pU);
        Player auxPlayer = pU.getPlayer();
        health = pU.getHitPoints();
        unitSize = auxType.size();
        maxHealth = auxType.maxHitPoints();
        armor = pU.getArmor();
        shields = pU.getShields();
        shieldArmor = auxPlayer.getUpgradeLevel(UpgradeType.Protoss_Plasma_Shields);
        maxShields = auxType.maxShields();
        speed = auxType.topSpeed();
        flying = auxType.isFlyer();
        groundDamage = auxType.groundWeapon().damageAmount();
        groundCooldown = auxType.groundWeapon().damageFactor() > 0 && auxType.maxGroundHits() > 0 ? auxType.groundWeapon().damageCooldown() /
                (auxType.groundWeapon().damageFactor() * auxType.maxGroundHits()) : 0;
        groundMaxRange = auxType.groundWeapon().maxRange();
        groundMinRange = auxType.groundWeapon().minRange();
        groundDamageType = auxType.groundWeapon().damageType();
        airDamage = auxType.airWeapon().damageAmount();
        airCooldown = auxType.airWeapon().damageFactor() > 0 && auxType.maxAirHits() > 0 ? auxType.airWeapon().damageCooldown() /
                auxType.airWeapon().damageFactor() * auxType.maxAirHits() : 0;
        airMaxRange = auxType.airWeapon().maxRange();
        airMinRange = auxType.airWeapon().minRange();
        airDamageType = auxType.airWeapon().damageType();
        unitType = auxType;
        player = auxPlayer;
        isOrganic = auxType.isOrganic();
        score = auxType.destroyScore();
        race = auxType.getRace();
        doThings(u, JFAP.game);
    }

    public JFAPUnit() {
    }

    private void doThings(Unit u, BW game) {
        if (unitType == UnitType.Protoss_Carrier) {
            Carrier carrier = (Carrier) u;
            groundDamage = UnitType.Protoss_Interceptor.groundWeapon().damageAmount();
            if (u != null && u.isVisible()) {
                final int interceptorCount = carrier.getInterceptorCount();
                if (interceptorCount > 0) {
                    groundCooldown = (int) (Math.round(37.0f / interceptorCount));
                } else {
                    groundDamage = 0;
                    groundCooldown = 5;
                }
            } else {
                if (player != null) {
                    groundCooldown = (int) (Math.round(37.0f / (player.getUpgradeLevel(UpgradeType.Carrier_Capacity) == 1 ? 8 : 4)));
                } else {
                    groundCooldown = (int) (Math.round(37.0f / 8));
                }
            }
            groundDamageType = UnitType.Protoss_Interceptor.groundWeapon().damageType();
            groundMaxRange = 32 * 8;
            airDamage = groundDamage;
            airDamageType = groundDamageType;
            airCooldown = groundCooldown;
            airMaxRange = groundMaxRange;
        } else if (unitType == UnitType.Terran_Bunker) {
            groundDamage = WeaponType.Gauss_Rifle.damageAmount();
            groundCooldown = UnitType.Terran_Marine.groundWeapon().damageCooldown() / 4;
            groundMaxRange = UnitType.Terran_Marine.groundWeapon().maxRange() + 32;
            airDamage = groundDamage;
            airCooldown = groundCooldown;
            airMaxRange = groundMaxRange;
        } else if (unitType == UnitType.Protoss_Reaver) {
            groundDamage = WeaponType.Scarab.damageAmount();
        }
        if (u != null) {
            if (unitType == UnitType.Terran_Marine) {
                if (((Marine) u).isStimmed()) {
                    groundCooldown /= 2;
                    airCooldown /= 2;
                }
            } else if (unitType == UnitType.Terran_Firebat) {
                if (((Firebat) u).isStimmed()) {
                    groundCooldown /= 2;
                    airCooldown /= 2;
                }
            }
        }
        if (u != null && u.isVisible() && !u.isFlying()) {
            elevation = game.getBWMap().getGroundHeight(u.getTilePosition());
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

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof JFAPUnit)) {
            return false;
        }
        JFAPUnit jfap = (JFAPUnit) o;
        return unit.equals(jfap.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit);
    }

    @Override
    public int compareTo(JFAPUnit arg0) {
        return this.id - arg0.id;
    }
}
