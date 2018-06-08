package ecgberht;

import bwem.Base;
import bwem.ChokePoint;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.type.WeaponType;
import org.openbw.bwapi4j.unit.*;
import org.openbw.bwapi4j.util.Pair;

import java.util.ArrayList;
import java.util.List;

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
        if (triangleAreaSum % (Math.pow(10, 14)) >= 0.999999999999999) triangleAreaSum = Math.ceil(triangleAreaSum);
        if (triangleAreaSum == rectArea) return true;
        else return false;
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
        for (Position p : positions) sum = new Position(sum.getX() + p.getX(), sum.getY() + p.getY());
        return sum;
    }

    public static TilePosition sumTilePosition(TilePosition... tilepositions) {
        TilePosition sum = new TilePosition(0, 0);
        for (TilePosition p : tilepositions) sum = new TilePosition(sum.getX() + p.getX(), sum.getY() + p.getY());
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
        if (player == null) return true;
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
        if (unit instanceof Zergling) return UnitType.Zerg_Zergling;
        if (unit instanceof Hydralisk) return UnitType.Zerg_Hydralisk;
        if (unit instanceof SporeColony) return UnitType.Zerg_Spore_Colony;
        if (unit instanceof SunkenColony) return UnitType.Zerg_Sunken_Colony;
        if (unit instanceof Mutalisk) return UnitType.Zerg_Mutalisk;
        if (unit instanceof Lurker) return UnitType.Zerg_Lurker;
        if (unit instanceof Queen) return UnitType.Zerg_Queen;
        if (unit instanceof Ultralisk) return UnitType.Zerg_Ultralisk;
        if (unit instanceof Guardian) return UnitType.Zerg_Guardian;
        if (unit instanceof Defiler) return UnitType.Zerg_Defiler;
        if (unit instanceof GreaterSpire) return UnitType.Zerg_Greater_Spire;
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
        if (unit instanceof Archon) return UnitType.Protoss_Archon;
        return unit.getInitialType();
    }

    public static UnitType getType(PlayerUnit unit) { // TODO TEST
        Race race = unit.getPlayer().getRace();
        UnitType type = UnitType.Unknown;
        if (race == Race.Terran) type = getTerranType(unit);
        if (type != UnitType.Unknown) return type;
        if (race == Race.Zerg) type = getZergType(unit);
        if (type != UnitType.Unknown) return type;
        if (race == Race.Protoss) {
            type = getProtossType(unit);
            if (type.getRace() != race) return type.getRace() == Race.Zerg ? getZergType(unit) : getTerranType(unit);
        }
        return unit.getInitialType();
    }

    public static WeaponType getWeapon(Unit attacker, Unit target) {
        UnitType attackerType = getType((PlayerUnit) attacker);
        UnitType targetType = getType(((PlayerUnit) target));
        if (attackerType == UnitType.Terran_Bunker) return getWeapon(UnitType.Terran_Marine, targetType);
        if (attackerType == UnitType.Protoss_Carrier) return getWeapon(UnitType.Protoss_Interceptor, targetType);
        if (attackerType == UnitType.Protoss_Reaver) return getWeapon(UnitType.Protoss_Scarab, targetType);
        return target.isFlying() ? attackerType.airWeapon() : attackerType.groundWeapon();
    }

    private static WeaponType getWeapon(UnitType attacker, UnitType target) {
        if (attacker == UnitType.Terran_Bunker) return getWeapon(UnitType.Terran_Marine, target);
        if (attacker == UnitType.Protoss_Carrier) return getWeapon(UnitType.Protoss_Interceptor, target);
        if (attacker == UnitType.Protoss_Reaver) return getWeapon(UnitType.Protoss_Scarab, target);
        return target.isFlyer() ? attacker.airWeapon() : attacker.groundWeapon();
    }

    private static boolean canAttack(PlayerUnit attacker, PlayerUnit target) {
        if (attacker.isLockedDown() || !(attacker instanceof Attacker)) return false;
        WeaponType weapon = getWeapon(attacker, target);
        return !(weapon == null);
    }

    private static boolean isInWeaponRange(PlayerUnit attacker, PlayerUnit target, int dist) {
        if (!(attacker instanceof Attacker)) return false;
        WeaponType weapon = getWeapon(attacker, target);
        if (weapon == null) return false;
        return weapon.maxRange() <= dist;
    }

    public static List<Unit> getFriendlyUnitsInRadius(Position sCenter, int radius) {
        List<Unit> units = new ArrayList<>();
        for (Unit u : getGs().bw.getUnits(getGs().getPlayer())) {
            if (getGs().broodWarDistance(u.getPosition(), sCenter) <= radius) units.add(u);
        }
        return units;
    }
}
