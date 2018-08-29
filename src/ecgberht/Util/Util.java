package ecgberht.Util;

import bwem.Base;
import bwem.ChokePoint;
import ecgberht.EnemyBuilding;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.org.apache.commons.lang3.mutable.MutableInt;
import org.openbw.bwapi4j.type.*;
import org.openbw.bwapi4j.unit.*;

import java.util.ArrayList;
import java.util.HashSet;
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
        if (triangleAreaSum % (Math.pow(10, 14)) >= 0.999999999999999) triangleAreaSum = Math.ceil(triangleAreaSum);
        if (triangleAreaSum == rectArea) return true;
        else return false;
    }

    public static List<Unit> getUnitsInRectangle(Position topLeft, Position bottomRight) { //TODO test
        List<Unit> units = new ArrayList<>();
        if (!getGs().getGame().getBWMap().isVisible(topLeft.toTilePosition()) || !getGs().getGame().getBWMap().isVisible(bottomRight.toTilePosition()))
            return units;
        for (Unit u : getGs().bw.getAllUnits()) {
            if (!u.exists() || !u.isVisible()) continue;
            Position pos = u.getPosition();
            Position p2 = new Position(bottomRight.getX(), topLeft.getY());
            Position p3 = new Position(topLeft.getX(), bottomRight.getY());
            if (check(topLeft, p2, p3, bottomRight, pos)) units.add(u);
        }
        return units;
    }

    public static List<Unit> getUnitsOnTile(TilePosition tile) { //TODO test
        List<Unit> units = new ArrayList<>();
        if (!getGs().getGame().getBWMap().isVisible(tile)) return units;
        for (Unit u : getGs().bw.getAllUnits()) {
            if (!u.exists()) continue;
            if (u.getTilePosition().equals(tile)) units.add(u);
        }
        return units;
    }

    public static int countUnitTypeSelf(UnitType type) {
        int count = 0;
        for (Unit u : getGs().bw.getUnits(getGs().getPlayer())) {
            if (!u.exists()) continue;
            if (!type.isBuilding() && type != UnitType.Terran_Science_Vessel && !((PlayerUnit) u).isCompleted())
                continue;
            if (type == UnitType.Terran_Siege_Tank_Tank_Mode && u instanceof SiegeTank) count++;
            else if (u.getType() == type) count++;
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

    public static MutablePair<Double, Double> sumPosition(List<MutablePair<Double, Double>> vectors) {
        MutablePair<Double, Double> sum = new MutablePair<>(0.0, 0.0);
        for (MutablePair<Double, Double> p : vectors) {
            sum.first += p.first;
            sum.second += p.second;
        }
        return sum;
    }

    public static boolean isEnemy(Player player) {
        if (player == null) return true;
        if (!getGs().players.containsKey(player)) return true;
        return getGs().players.get(player) == -1;
    }

    public static ChokePoint getClosestChokepoint(Position pos) {
        ChokePoint closestChoke = null;
        double dist = Double.MAX_VALUE;
        for (ChokePoint choke : getGs().bwem.getMap().getChokePoints()) {
            double cDist = broodWarDistance(pos, choke.getCenter().toPosition());
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
            double cDist = getGroundDistance(pos, choke.getCenter().toPosition());
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
            double cDist = broodWarDistance(pos, base.getLocation().toPosition());
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
            double cDist = getGroundDistance(pos, base.getLocation().toPosition());
            if (cDist == 0.0) continue;
            if (closestBase == null || cDist < dist) {
                closestBase = base;
                dist = cDist;
            }
        }
        return closestBase;
    }

    public static WeaponType getWeapon(Unit attacker, Unit target) {
        UnitType attackerType = attacker.getType();
        UnitType targetType = target.getType();
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

    public static WeaponType getWeapon(UnitType attacker) {
        if (attacker == UnitType.Terran_Bunker) return UnitType.Terran_Marine.groundWeapon();
        if (attacker == UnitType.Protoss_Carrier) return UnitType.Protoss_Interceptor.airWeapon();
        if (attacker == UnitType.Protoss_Reaver) return UnitType.Protoss_Scarab.groundWeapon();
        return attacker.groundWeapon() != WeaponType.None ? attacker.groundWeapon() : attacker.airWeapon();
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
            if (broodWarDistance(u.getPosition(), sCenter) <= radius) units.add(u);
        }
        return units;
    }

    // get a target for the ranged unit to attack
    public static Unit getTarget(final Unit rangedUnit, final Set<Unit> targets) {
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

    public static String raceToString(Race race) {
        switch (race) {
            case Zerg:
                return "Zerg";
            case Terran:
                return "Terran";
            case Protoss:
                return "Protoss";
            case Random:
                return "Random";
            case Unknown:
                return "Random";
        }
        return "Unknown";
    }

    // Credits to Steamhammer (Jay Scott), emergency targeting for Proxy BBS and Plasma
    private static int getScore(final PlayerUnit attacker, final PlayerUnit target) {
        int priority = getAttackPriority(attacker, target);     // 0..12
        int range = attacker.getDistance(target);           // 0..map size in pixels
        // Let's say that 1 priority step is worth 160 pixels (5 tiles).
        // We care about unit-target range and target-order position distance.
        int score = 5 * 32 * priority - range;
        if (target.getType() == UnitType.Zerg_Egg) return score;
        WeaponType targetWeapon = Util.getWeapon(attacker, target);
        UnitType targetType = target.getType();
        // Adjust for special features.
        // This could adjust for relative speed and direction, so that we don't chase what we can't catch.
        if (range <= targetWeapon.maxRange()) {
            score += 5 * 32;
        } else if (target instanceof MobileUnit && !((MobileUnit) target).isMoving()) {
            if (target instanceof SiegeTank) {
                if (((SiegeTank) target).isSieged() || target.getOrder() == Order.Sieging || target.getOrder() == Order.Unsieging) {
                    score += 48;
                } else {
                    score += 24;
                }
            }

        } else if (target instanceof MobileUnit && ((MobileUnit) target).isBraking()) {
            score += 16;
        } else if (targetType.topSpeed() >= attacker.getType().topSpeed()) {
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

    // Credits to Steamhammer (Jay Scott), emergency targeting for Proxy BBS and Plasma
    private static int getAttackPriority(PlayerUnit rangedUnit, PlayerUnit target) {
        final UnitType targetType = target.getType();
        // Exceptions if we're a ground unit.
        if (target instanceof Burrowable) {
            if ((targetType == UnitType.Terran_Vulture_Spider_Mine && !((Burrowable) target).isBurrowed()) || targetType == UnitType.Zerg_Infested_Terran) {
                return 12;
            }
        }
        if (targetType == UnitType.Zerg_Lurker) return 12;
        if (targetType == UnitType.Zerg_Egg) return 5;
        if (targetType == UnitType.Protoss_High_Templar) return 12;
        if (targetType == UnitType.Protoss_Reaver || targetType == UnitType.Protoss_Arbiter) return 11;
        // Droppers are as bad as threats. They may be loaded and are often isolated and safer to attack.
        if (targetType == UnitType.Terran_Dropship || targetType == UnitType.Protoss_Shuttle) return 10;
        // Also as bad are other dangerous things.
        if (targetType == UnitType.Terran_Science_Vessel || targetType == UnitType.Zerg_Scourge || targetType == UnitType.Protoss_Observer) {
            return 10;
        }
        // Next are workers.
        if (targetType.isWorker()) {
            if (rangedUnit.getType() == UnitType.Terran_Vulture) return 11;
            if (target instanceof SCV) {
                // Repairing or blocking a choke makes you critical.
                if (((SCV) target).isRepairing()) return 11;
                // SCVs constructing are also important.
                if (((SCV) target).isConstructing()) return 10;
            }
            return 6;
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
        if (targetType == UnitType.Protoss_Templar_Archives) return 7;

        if (targetType.gasPrice() > 0) return 4;
        if (targetType.mineralPrice() > 0) return 3;
        // Finally everything else.
        return 1;
    }

    public static int getWeight(Unit u) {
        if (u instanceof SiegeTank) return 6;
        return 1;
    }

    public static int getGroundDistance(Position start, Position end) {
        try {
            MutableInt dist = new MutableInt();
            getGs().bwem.getMap().getPath(start, end, dist);
            return dist.intValue();
        } catch (Exception e) {
            //System.err.println("Ground Distance Exception");
            //e.printStackTrace();
            return start != null && end != null ? start.getDistance(end) : Integer.MAX_VALUE;
        }
    }

    public static boolean isConnected(TilePosition start, TilePosition end) {
        return !getGs().bwem.getMap().getPath(start.toPosition(), end.toPosition()).isEmpty();
    }

    public static Position getCentroid(Set<Unit> units) {
        Position point = new Position(0, 0);
        if (units.size() == 1) return units.iterator().next().getPosition();
        for (Unit u : units) point = point.add(u.getPosition());
        return point;
    }

    //Credits to @PurpleWaveJadien / Dan
    public static double broodWarDistance(Position a, Position b) {
        return broodWarDistance(a.getX(), a.getY(), b.getX(), b.getY());
    }

    //Credits to @PurpleWaveJadien / Dan
    public static double broodWarDistance(int x00, int y00, int x01, int y01) {
        double dx = Math.abs(x00 - x01);
        double dy = Math.abs(y00 - y01);
        double d = Math.min(dx, dy);
        double D = Math.max(dx, dy);
        if (d < D / 4) return D;
        return D - D / 16 + d * 3 / 8 - D / 64 + d * 3 / 256;
    }

    //Credits to @PurpleWaveJadien / Dan
    public static double broodWarDistance(double[] a, double[] b) {
        double dx = Math.abs(a[0] - b[0]);
        double dy = Math.abs(a[1] - b[1]);
        double d = Math.min(dx, dy);
        double D = Math.max(dx, dy);
        if (d < D / 4) return D;
        return D - D / 16 + d * 3 / 8 - D / 64 + d * 3 / 256;
    }

    public static Position chooseAttackPosition(Position p, boolean flying) {
        Position chosen = null;
        double maxScore = 0;
        for (EnemyBuilding b : getGs().enemyBuildingMemory.values()) {
            double influence = getScoreAttackPosition(b.unit);
            //double score = influence / (2 * getEuclideanDist(p, b.pos.toPosition()));
            double score = influence / (2.5 * (flying ? Util.getGroundDistance(p, b.pos.toPosition()) : b.pos.toPosition().getDistance(p)));
            if (score > maxScore) {
                chosen = b.pos.toPosition();
                maxScore = score;
            }
        }
        return chosen;
    }

    private static double getScoreAttackPosition(Building unit) {
        if (unit instanceof ResourceDepot) return 8;
        if (unit instanceof ResearchingFacility || unit instanceof TrainingFacility) return 4;
        if (unit.getType().canAttack() || unit instanceof Bunker) return 6;
        return 3;
    }

    public static Unit getClosestUnit(Unit unit, Set<Unit> enemies) {
        Unit chosen = null;
        double minDist = Double.MAX_VALUE;
        for (Unit u : enemies) {
            if (!u.exists()) continue;
            double dist = unit.getDistance(u);
            if (chosen == null || dist < minDist) {
                minDist = dist;
                chosen = u;
            }
        }
        return chosen;
    }

    public static MutablePair<Double, Double> cropPosition(MutablePair<Double, Double> unitV) {
        MutablePair<Double, Double> cropped = new MutablePair<>(unitV.first, unitV.second);
        double sizeX = getGs().getGame().getBWMap().mapWidth() * 32.0;
        double sizeY = getGs().getGame().getBWMap().mapHeight() * 32.0;
        if (cropped.first < 0.0) cropped.first = 0.0;
        else if (cropped.first >= sizeX) cropped.first = sizeX - 1;
        if (cropped.second < 0.0) cropped.second = 0.0;
        else if (cropped.second >= sizeY) cropped.second = sizeY - 1;
        return cropped;
    }

    public static Position cropPosition(Position pos) {
        MutablePair<Integer, Integer> cropped = new MutablePair<>(pos.getX(), pos.getY());
        int sizeX = getGs().getGame().getBWMap().mapWidth() * 32;
        int sizeY = getGs().getGame().getBWMap().mapHeight() * 32;
        if (cropped.first < 0.0) cropped.first = 0;
        else if (cropped.first >= sizeX) cropped.first = sizeX - 1;
        if (cropped.second < 0.0) cropped.second = 0;
        else if (cropped.second >= sizeY) cropped.second = sizeY - 1;
        return new Position(cropped.first, cropped.second);
    }

    public static Position ChoosePatrolPositionVulture(Vulture myUnit, Unit attackUnit) {
        try {
            Position myUnitPos = myUnit.getPosition();
            Position attackUnitPos = attackUnit.getPosition();
            MutablePair<Double, Double> AT = new MutablePair<>((double) attackUnitPos.getX() - myUnitPos.getX(), (double) attackUnitPos.getY() - myUnitPos.getY());
            MutablePair<Double, Double> patrolDir1 = rotatePosition(AT, Math.PI / 5.0);
            MutablePair<Double, Double> patrolDir2 = rotatePosition(AT, -Math.PI / 5.0);
            MutablePair<Double, Double> accel = new MutablePair<>(((MobileUnit) myUnit).getVelocityX(), ((MobileUnit) myUnit).getVelocityY());
            MutablePair<Double, Double> multi = new MutablePair<>(patrolDir1.first * accel.first, patrolDir1.second * accel.second);
            MutablePair<Double, Double> patrolDir = (multi.first >= 0 && multi.second >= 0) ? patrolDir1 : patrolDir2;
            patrolDir = normalize(patrolDir);
            Position prePatrol = new Position(patrolDir.first.intValue(), patrolDir.second.intValue()).multiply(new Position(myUnit.getGroundWeaponMaxRange() - 5, myUnit.getGroundWeaponMaxRange() - 5));
            return cropPosition(myUnitPos.add(prePatrol));
        } catch (Exception e) {
            System.err.println("ChoosePatrolPositionVulture Exception");
            e.printStackTrace();
            return null;
        }
    }

    private static MutablePair<Double, Double> normalize(MutablePair<Double, Double> pos) {
        double norm = Math.sqrt(pos.first * pos.first + pos.second * pos.second);
        return new MutablePair<>(pos.first / norm, pos.second / norm);
    }

    private static MutablePair<Double, Double> rotatePosition(MutablePair<Double, Double> pos, double angle) {
        final double cosAngle = Math.cos(angle);
        final double sinAngle = Math.sin(angle);
        return new MutablePair<>(pos.first * cosAngle - pos.second * sinAngle, pos.first * sinAngle + pos.second * cosAngle);
    }

    public static boolean isPositionMapEdge(Position pos) {
        return pos.getX() <= 0 || pos.getY() <= 0 || pos.getX() >= getGs().getGame().getBWMap().mapWidth() * 32
                || pos.getY() >= getGs().getGame().getBWMap().mapHeight() * 32;
    }

    public static Position improveMapEdgePosition(Position unitPos, Position pos) {
        double angle = Math.atan2(unitPos.getY(), unitPos.getX()) - Math.atan2(pos.getY(), pos.getX());
        MutablePair<Integer, Integer> improved = new MutablePair<>(pos.getX(), pos.getY());
        int mapHeight = getGs().getGame().getBWMap().mapHeight() * 32;
        int mapWidth = getGs().getGame().getBWMap().mapWidth() * 32;
        if (improved.first <= 0 && improved.second <= 0) {
            if (Math.random() < 0.5) improved.first = 5 * 32;
            else improved.second = 5 * 32;
            return new Position(improved.first, improved.second);
        }
        if (improved.first >= mapWidth && improved.second >= mapWidth) {
            if (Math.random() < 0.5) improved.first = mapWidth - 3 * 32;
            else improved.second = mapHeight - 3 * 32;
            return new Position(improved.first, improved.second);
        }
        if (improved.first <= 0) {
            improved.first = 5 * 32;
            return new Position(improved.first, improved.second);
        }
        if (improved.second <= 0) {
            improved.second = 5 * 32;
            return new Position(improved.first, improved.second);
        }
        if (improved.first >= mapWidth) {
            improved.first = mapWidth - 2 * 32;
            return new Position(improved.first, improved.second);
        }
        if (improved.second >= mapHeight) {
            improved.second = mapHeight - 2 * 32;
            return new Position(improved.first, improved.second);
        }
        return null;
    }

    public static boolean shouldIStop(Position pos) {
        for (Base b : getGs().BLs) {
            if (Util.getSquareTiles(b.getLocation(), UnitType.Terran_Command_Center).contains(pos.toTilePosition()))
                return false;
        }
        return getGs().mainChoke != null && getGs().mainChoke.getCenter().toPosition().getDistance(pos) > 32 * 2;
    }

    private static Set<TilePosition> getSquareTiles(TilePosition pos, UnitType type) {
        Set<TilePosition> tiles = new HashSet<>();
        tiles.add(pos);
        int height = type.tileHeight();
        int width = type.tileHeight();
        for (int ii = 1; ii <= height; ii++) tiles.add(pos.add(new TilePosition(0, ii)));
        for (int ii = 1; ii <= width; ii++) tiles.add(pos.add(new TilePosition(ii, 0)));
        return tiles;
    }

    public static Position getUnitCenterPosition(Position leftTop, UnitType type) {
        Position rightBottom = new Position(leftTop.getX() + type.tileWidth() * TilePosition.SIZE_IN_PIXELS, leftTop.getY() + type.tileHeight() * TilePosition.SIZE_IN_PIXELS);
        return new Position((leftTop.getX() + rightBottom.getX()) / 2, (leftTop.getY() + rightBottom.getY()) / 2);

    }
}
