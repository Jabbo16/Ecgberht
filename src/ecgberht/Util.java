package ecgberht;

import bwem.Base;
import bwem.ChokePoint;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.*;
import org.openbw.bwapi4j.unit.*;
import org.openbw.bwapi4j.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static ecgberht.Ecgberht.getGs;

public class Util {

    private static double areaOfTriangle(Position p1, Position p2, Position pos) {
        double side1 = Math.sqrt(Math.pow(Math.abs(p1.getY() - p2.getY()), 2) + Math.pow(Math.abs(p1.getX() - p2.getX()), 2));
        double side2 = Math.sqrt(Math.pow(Math.abs(p1.getY() - pos.getY()), 2) + Math.pow(Math.abs(p1.getX() - pos.getX()), 2));
        double side3 = Math.sqrt(Math.pow(Math.abs(p2.getY() - pos.getY()), 2) + Math.pow(Math.abs(p2.getX() - pos.getX()), 2));
        double semi_perimeter = (side1 + side2 + side3) / 2;
        return Math.sqrt(semi_perimeter * (semi_perimeter - side1) * (semi_perimeter - side2) * (semi_perimeter - side3));
    }


    private static double areaOfRect(Position p1, Position p2, Position p3) {
        double side1 = Math.sqrt(Math.pow(Math.abs(p1.getY() - p2.getY()), 2) + Math.pow(Math.abs(p1.getX() - p2.getX()), 2));
        double side2 = Math.sqrt(Math.pow(Math.abs(p2.getY() - p3.getY()), 2) + Math.pow(Math.abs(p2.getX() - p3.getX()), 2));
        return side1 * side2;
    }

    private static boolean check(Position p1, Position p2, Position p3, Position p4, Position pos) {

        double triangle1Area = areaOfTriangle(p1, p2, pos);
        double triangle2Area = areaOfTriangle(p2, p3, pos);
        double triangle3Area = areaOfTriangle(p3, p4, pos);
        double triangle4Area = areaOfTriangle(p4, p1, pos);
        double rectArea = areaOfRect(p1, p2, p3);
        double triangleAreaSum = (triangle1Area + triangle2Area + triangle3Area + triangle4Area);

        if (triangleAreaSum % (Math.pow(10, 14)) >= 0.999999999999999) {
            triangleAreaSum = Math.ceil(triangleAreaSum);
        }

        if (triangleAreaSum == rectArea)
            return true;
        else
            return false;
    }

    public static List<Unit> getUnitsInRectangle(Position topLeft, Position bottomRight) { //TODO test
        List<Unit> units = new ArrayList<>();
        for (Unit u : getGs().bw.getAllUnits()) {
            if (!u.exists()) continue;
            Position pos = u.getPosition();
            Position p1 = topLeft;
            Position p2 = new Position(bottomRight.getX(), topLeft.getY());
            Position p3 = new Position(topLeft.getX(), bottomRight.getY());
            Position p4 = bottomRight;
            if (check(p1, p2, p3, p4, pos)) units.add(u);
        }
        return units;
    }

    public static List<Unit> getUnitsOnTile(TilePosition tile) { //TODO test
        List<Unit> units = new ArrayList<>();
        for (Unit u : getGs().bw.getAllUnits()) {
            if (!u.exists()) continue;
            if (u.getTilePosition().equals(tile)) units.add(u);
        }
        return units;
    }

    public static int countUnitTypeSelf(UnitType type) {
        int count = 0;
        for (Unit u : getGs().bw.getUnits(getGs().getPlayer())) {
            if (getType(((PlayerUnit) u)) == type) count++;
        }
        return count;
    }

    public static Position sumPosition(Position... positions) {
        Position sum = new Position(0, 0);
        for (Position p : positions) {
            sum = new Position(sum.getX() + p.getX(), sum.getY() + p.getY());
        }
        return sum;
    }

    public static TilePosition sumTilePosition(TilePosition... tilepositions) {
        TilePosition sum = new TilePosition(0, 0);
        for (TilePosition p : tilepositions) {
            sum = new TilePosition(sum.getX() + p.getX(), sum.getY() + p.getY());
        }
        return sum;
    }

    public static Pair<Double, Double> sumPosition(List<Pair<Double, Double>> vectors) {
        Pair<Double, Double> sum = new Pair<>(0.0, 0.0);
        for (Pair<Double, Double> p : vectors) {
            sum.first += p.first;
            sum.second += p.second;
        }
        return sum;
    }

    public static boolean isEnemy(Player player) {
        return getGs().players.get(player) == -1;
    }

    public static ChokePoint getClosestChokepoint(Position pos) {
        ChokePoint closestChoke = null;
        double dist = Double.MAX_VALUE;
        for (ChokePoint choke : getGs().bwem.getMap().getChokePoints()) {
            double cDist = getGs().broodWarDistance(pos, choke.getCenter().toPosition());
            if (closestChoke == null || cDist < dist) {
                closestChoke = choke;
                dist = cDist;
            }
        }
        return closestChoke;
    }

    public static ChokePoint getGroundDistanceClosestChoke(Position pos) {
        ChokePoint closestChoke = null;
        double dist = Double.MAX_VALUE;
        for (ChokePoint choke : getGs().bwem.getMap().getChokePoints()) {
            double cDist = getGs().bwta.getGroundDistance(pos.toTilePosition(), choke.getCenter().toTilePosition());
            if (cDist == 0.0) continue;
            if (closestChoke == null || cDist < dist) {
                closestChoke = choke;
                dist = cDist;
            }
        }
        return closestChoke;
    }

    public static Base getClosestBaseLocation(Position pos) {
        Base closestBase = null;
        double dist = Double.MAX_VALUE;
        for (Base base : getGs().bwem.getMap().getBases()) {
            double cDist = getGs().broodWarDistance(pos, base.getLocation().toPosition());
            if (closestBase == null || cDist < dist) {
                closestBase = base;
                dist = cDist;
            }
        }
        return closestBase;
    }

    public static Base getGroundDistanceClosestBase(Position pos) {
        Base closestBase = null;
        double dist = Double.MAX_VALUE;
        for (Base base : getGs().bwem.getMap().getBases()) {
            double cDist = getGs().bwta.getGroundDistance(pos.toTilePosition(), base.getLocation());
            if (cDist == 0.0) continue;
            if (closestBase == null || cDist < dist) {
                closestBase = base;
                dist = cDist;
            }
        }
        return closestBase;
    }

    private static UnitType getZergType(PlayerUnit unit) {
        if (unit instanceof Zergling) {
            return UnitType.Zerg_Zergling;
        }
        if (unit instanceof Hydralisk) {
            return UnitType.Zerg_Hydralisk;
        }
        if (unit instanceof Mutalisk) {
            return UnitType.Zerg_Mutalisk;
        }
        if (unit instanceof Lurker) {
            return UnitType.Zerg_Lurker;
        }
        if (unit instanceof Queen) {
            return UnitType.Zerg_Queen;
        }
        if (unit instanceof Ultralisk) {
            return UnitType.Zerg_Ultralisk;
        }
        if (unit instanceof Guardian) {
            return UnitType.Zerg_Guardian;
        }
        if (unit instanceof Defiler) {
            return UnitType.Zerg_Defiler;
        }
        return unit.getInitialType();
    }

    private static UnitType getTerranType(PlayerUnit unit) {
        if (unit instanceof SiegeTank) {
            SiegeTank t = (SiegeTank) unit;
            return t.isSieged() ? UnitType.Terran_Siege_Tank_Siege_Mode : UnitType.Terran_Siege_Tank_Tank_Mode;
        }
        return unit.getInitialType();
    }

    private static UnitType getProtossType(PlayerUnit unit) {
        if (unit instanceof Archon) {
            return UnitType.Protoss_Archon;
        }
        return unit.getInitialType();
    }

    public static UnitType getType(PlayerUnit unit) { // TODO TEST
        Race race = unit.getPlayer().getRace();
        UnitType type = UnitType.Unknown;
        if (race == Race.Terran) {
            type = getTerranType(unit);
        }
        if (type != UnitType.Unknown) return type;

        if (race == Race.Zerg) {
            type = getZergType(unit);
        }

        if (type != UnitType.Unknown) return type;

        if (race == Race.Protoss) {
            type = getProtossType(unit);
            if (type.getRace() != race) {
                return type.getRace() == Race.Zerg ? getZergType(unit) : getTerranType(unit);
            }
        }
        return unit.getInitialType();
    }

    public static WeaponType getWeapon(Unit attacker, Unit target) {
        UnitType attackerType = getType((PlayerUnit) attacker);
        UnitType targetType = getType(((PlayerUnit) target));
        if (attackerType == UnitType.Terran_Bunker) {
            return getWeapon(UnitType.Terran_Marine, targetType);
        }
        if (attackerType == UnitType.Protoss_Carrier) {
            return getWeapon(UnitType.Protoss_Interceptor, targetType);
        }
        if (attackerType == UnitType.Protoss_Reaver) {
            return getWeapon(UnitType.Protoss_Scarab, targetType);
        }
        return target.isFlying() ? attackerType.airWeapon() : attackerType.groundWeapon();
    }

    private static WeaponType getWeapon(UnitType attacker, UnitType target) {
        if (attacker == UnitType.Terran_Bunker) {
            return getWeapon(UnitType.Terran_Marine, target);
        }
        if (attacker == UnitType.Protoss_Carrier) {
            return getWeapon(UnitType.Protoss_Interceptor, target);
        }
        if (attacker == UnitType.Protoss_Reaver) {
            return getWeapon(UnitType.Protoss_Scarab, target);
        }
        return target.isFlyer() ? attacker.airWeapon() : attacker.groundWeapon();
    }


    // get a target for the ranged unit to attack
    public static final Unit getTarget(final Unit rangedUnit, final Set<Unit> targets) {
        double highestPriority = 0.f;
        Unit bestTarget = null;

        // for each target possibility
        for (Unit targetUnit : targets) {
            double priority = getScore((PlayerUnit) rangedUnit, (PlayerUnit) targetUnit);

            // if it's a higher priority, set it
            if (bestTarget == null || priority > highestPriority) {
                highestPriority = priority;
                bestTarget = targetUnit;
            }
        }

        return bestTarget;
    }

    private static int getScore(final PlayerUnit attacker, final PlayerUnit target) {
        int priority = getAttackPriority(attacker, target);     // 0..12
        int range = (int) getGs().broodWarDistance(attacker.getPosition(), target.getPosition());           // 0..map size in pixels
        // Let's say that 1 priority step is worth 160 pixels (5 tiles).
        // We care about unit-target range and target-order position distance.
        int score = 5 * 32 * priority - range;

        WeaponType targetWeapon = Util.getWeapon(attacker, target);
        UnitType targetType = getType(target);
        // Adjust for special features.
        // This could adjust for relative speed and direction, so that we don't chase what we can't catch.
        if (range <= targetWeapon.maxRange()) {
            score += 5 * 32;
        } else if (!((MobileUnit) target).isMoving()) {
            if (target instanceof SiegeTank) {
                if (((SiegeTank) target).isSieged() || target.getOrder() == Order.Sieging || target.getOrder() == Order.Unsieging) {
                    score += 48;
                } else {
                    score += 24;
                }
            }

        } else if (((MobileUnit) target).isBraking()) {
            score += 16;
        } else if (targetType.topSpeed() >= getType(attacker).topSpeed()) {
            score -= 5 * 32;
        }

        // Prefer targets that are already hurt.
        if (targetType.getRace() == Race.Protoss && target.getShields() <= 5) {
            score += 32;
        }
        if (target.getHitPoints() < targetType.maxHitPoints()) {
            score += 24;
        }

        DamageType damage = targetWeapon.damageType();
        if (damage == DamageType.Explosive) {
            if (targetType.size() == UnitSizeType.Large) {
                score += 32;
            }
        } else if (damage == DamageType.Concussive) {
            if (targetType.size() == UnitSizeType.Small) {
                score += 32;
            }
        }
        return score;
    }

    //get the attack priority of a target unit
    private static int getAttackPriority(PlayerUnit rangedUnit, PlayerUnit target) {
        final UnitType targetType = getType(target);
        // Exceptions if we're a ground unit.
        if (target instanceof Burrowable) {
            if ((targetType == UnitType.Terran_Vulture_Spider_Mine && !((Burrowable) target).isBurrowed()) || targetType == UnitType.Zerg_Infested_Terran) {
                return 12;
            }
        }

        if (targetType == UnitType.Zerg_Lurker) {
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
        if (targetType.isWorker()) {
            if (getType(rangedUnit) == UnitType.Terran_Vulture) {
                return 11;
            }
            if (target instanceof SCV) {
                // Repairing or blocking a choke makes you critical.
                if (((SCV) target).isRepairing()) {
                    return 11;
                }
                // SCVs constructing are also important.
                if (((SCV) target).isConstructing()) {
                    return 10;
                }
            }
            return 9;
        }
        // Important combat units that we may not have targeted above (esp. if we're a flyer).
        if (targetType == UnitType.Protoss_Carrier || targetType == UnitType.Terran_Siege_Tank_Tank_Mode || targetType == UnitType.Terran_Siege_Tank_Siege_Mode) {
            return 8;
        }
        // Short circuit: Give bunkers a lower priority to reduce bunker obsession.
        if (targetType == UnitType.Terran_Bunker || targetType == UnitType.Zerg_Sunken_Colony || targetType == UnitType.Protoss_Photon_Cannon) {
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

    public static List<Unit> getFriendlyUnitsInRadius(Position sCenter, int radius) {
        List<Unit> units = new ArrayList<>();
        for (Unit u : getGs().bw.getUnits(getGs().getPlayer())) {
            if (getGs().broodWarDistance(u.getPosition(), sCenter) <= radius) units.add(u);
        }
        return units;
    }
}
