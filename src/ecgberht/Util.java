package ecgberht;

import static ecgberht.Ecgberht.getGs;

import java.util.List;
import java.util.Set;

import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.DamageType;
import org.openbw.bwapi4j.type.UnitSizeType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.type.WeaponType;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.util.Pair;

public class Util {

	public static Position sumPosition(Position... positions) {
		Position sum = new Position(0,0);
		for(Position p : positions) {
			sum = new Position(sum.getX() + p.getX(), sum.getY() + p.getY());
		}
		return sum;
	}

	public static TilePosition sumTilePosition(TilePosition... tilepositions) {
		TilePosition sum = new TilePosition(0,0);
		for(TilePosition p : tilepositions) {
			sum = new TilePosition(sum.getX() + p.getX(), sum.getY() + p.getY());
		}
		return sum;
	}

	public static Pair<Double,Double> sumPosition(List<Pair<Double, Double>> vectors) {
		Pair<Double,Double> sum = new Pair<>(0.0,0.0);
		for(Pair<Double, Double> p : vectors) {
			sum.first += p.first;
			sum.second += p.second;
		}
		return sum;
	}

	static WeaponType GetWeapon(Unit attacker, Unit target)
	{
		UnitType attackerType = attacker.getType();
		UnitType targetType = target.getType();
		if (attackerType == UnitType.Terran_Bunker)
		{
			return GetWeapon(UnitType.Terran_Marine, targetType);
		}
		if (attackerType == UnitType.Protoss_Carrier)
		{
			return GetWeapon(UnitType.Protoss_Interceptor, targetType);
		}
		if (attackerType == UnitType.Protoss_Reaver)
		{
			return GetWeapon(UnitType.Protoss_Scarab, targetType);
		}
		return target.isFlying() ? attackerType.airWeapon() : attackerType.groundWeapon();
	}

	static WeaponType GetWeapon(UnitType attacker, UnitType target)
	{
		if (attacker == UnitType.Terran_Bunker)
		{
			return GetWeapon(UnitType.Terran_Marine, target);
		}
		if (attacker == UnitType.Protoss_Carrier)
		{
			return GetWeapon(UnitType.Protoss_Interceptor, target);
		}
		if (attacker == UnitType.Protoss_Reaver)
		{
			return GetWeapon(UnitType.Protoss_Scarab, target);
		}
		return target.isFlyer() ? attacker.airWeapon() : attacker.groundWeapon();
	}


	// get a target for the ranged unit to attack
	static final Unit getTarget(final Unit rangedUnit, final Set<Unit> targets) {
		double highestPriority = 0.f;
		Unit bestTarget = null;

		// for each target possiblity
		for (Unit targetUnit : targets) {
			double priority = getScore(rangedUnit, targetUnit);

			// if it's a higher priority, set it
			if (bestTarget == null || priority > highestPriority) {
				highestPriority = priority;
				bestTarget = targetUnit;
			}
		}

		return bestTarget;
	}

	static int getScore(final Unit attacker, final Unit target) {
		int priority = getAttackPriority(attacker, target);     // 0..12
		int range    = (int) getGs().broodWarDistance(attacker.getPosition(), target.getPosition());           // 0..map size in pixels
		// Let's say that 1 priority step is worth 160 pixels (5 tiles).
		// We care about unit-target range and target-order position distance.
		int score = 5 * 32 * priority - range;

		WeaponType targetWeapon = Util.GetWeapon(attacker,target);
		// Adjust for special features.
		// This could adjust for relative speed and direction, so that we don't chase what we can't catch.
		if (range <= targetWeapon.maxRange())
		{
			score += 5 * 32;
		}
		else if (!target.isMoving()) {
			if (target.isSieged() || target.getOrder() == Order.Sieging || target.getOrder() == Order.Unsieging) {
				score += 48;
			}
			else {
				score += 24;
			}
		}
		else if (target.isBraking()) {
			score += 16;
		}
		else if (target.getType().topSpeed() >= attacker.getType().topSpeed()) {
			score -= 5 * 32;
		}

		// Prefer targets that are already hurt.
		if (target.getType().getRace() == Race.Protoss && target.getShields() <= 5) {
			score += 32;
		}
		if (target.getHitPoints() < target.getType().maxHitPoints()) {
			score += 24;
		}

		DamageType damage = targetWeapon.damageType();
		if (damage == DamageType.Explosive) {
			if (target.getType().size() == UnitSizeType.Large) {
				score += 32;
			}
		}
		else if (damage == DamageType.Concussive)
		{
			if (target.getType().size() == UnitSizeType.Small) {
				score += 32;
			}
		}
		return score;
	}

	//get the attack priority of a target unit
	static int getAttackPriority(Unit rangedUnit, Unit target) {
		final UnitType targetType = target.getType();
		// Exceptions if we're a ground unit.
		if ((targetType == UnitType.Terran_Vulture_Spider_Mine && !target.isBurrowed()) || targetType == UnitType.Zerg_Infested_Terran) {
			return 12;
		}
		if(targetType == UnitType.Zerg_Lurker) {
			return 12;
		}

		if (targetType == UnitType.Protoss_High_Templar) {
			return 12;
		}

		if (targetType == UnitType.Protoss_Reaver || targetType == UnitType.Protoss_Arbiter) {
			return 11;
		}

		// Droppers are as bad as threats. They may be loaded and are often isolated and safer to attack.
		if (targetType == UnitType.Terran_Dropship || targetType == UnitType.Protoss_Shuttle) {
			return 10;
		}
		// Also as bad are other dangerous things.
		if (targetType == UnitType.Terran_Science_Vessel || targetType == UnitType.Zerg_Scourge || targetType == UnitType.Protoss_Observer) {
			return 10;
		}
		// Next are workers.
		if (targetType.isWorker())  {
			if (rangedUnit.getType() == UnitType.Terran_Vulture) {
				return 11;
			}
			// Repairing or blocking a choke makes you critical.
			if (target.isRepairing()) {
				return 11;
			}
			// SCVs constructing are also important.
			if (target.isConstructing()) {
				return 10;
			}

			return 9;
		}
		// Important combat units that we may not have targeted above (esp. if we're a flyer).
		if (targetType == UnitType.Protoss_Carrier || targetType == UnitType.Terran_Siege_Tank_Tank_Mode || targetType == UnitType.Terran_Siege_Tank_Siege_Mode) {
			return 8;
		}
		// Short circuit: Give bunkers a lower priority to reduce bunker obsession.
		if (targetType == UnitType.Terran_Bunker || targetType == UnitType.Zerg_Sunken_Colony || targetType == UnitType.Protoss_Photon_Cannon)
		{
			return 6;
		}
		// Spellcasters are as important as key buildings.
		// Also remember to target other non-threat combat units.
		if (targetType.isSpellcaster() || targetType.groundWeapon() != WeaponType.None || targetType.airWeapon() != WeaponType.None) {
			return 7;
		}
		// Templar tech and spawning pool are more important.
		if (targetType == UnitType.Protoss_Templar_Archives) {
			return 7;
		}

		if (targetType.gasPrice() > 0) {
			return 4;
		}
		if (targetType.mineralPrice() > 0) {
			return 3;
		}
		// Finally everything else.
		return 1;
	}
}
