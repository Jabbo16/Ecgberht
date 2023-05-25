package ecgberht.Util;

import bwem.Area;
import bwem.Base;
import bwem.ChokePoint;
import ecgberht.BaseManager;
import ecgberht.ConfigManager;
import ecgberht.UnitInfo;
import org.openbw.bwapi4j.*;
import org.openbw.bwapi4j.type.*;
import org.openbw.bwapi4j.unit.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        return Double.compare(triangleAreaSum, rectArea) == 0;
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

    public static WeaponType getWeapon(UnitInfo attacker, UnitInfo target) {
        UnitType attackerType = attacker.unitType;
        UnitType targetType = target.unitType;
        if (attackerType == UnitType.Terran_Bunker) return getWeapon(UnitType.Terran_Marine, targetType);
        if (attackerType == UnitType.Protoss_Carrier) return getWeapon(UnitType.Protoss_Interceptor, targetType);
        if (attackerType == UnitType.Protoss_Reaver) return getWeapon(UnitType.Protoss_Scarab, targetType);
        return target.flying ? attackerType.airWeapon() : attackerType.groundWeapon();
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

    public static boolean canAttack(UnitInfo attacker, UnitInfo target) {
        if (attacker.unit.isLockedDown() || !(attacker.unit instanceof Attacker)) return false;
        WeaponType weapon = getWeapon(attacker, target);
        return !(weapon == null);
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

    public static void sendText(String text) {
        if (!ConfigManager.getConfig().ecgConfig.humanMode) getIH().sendText(text);
    }

    public static int getGroundDistance(Position start, Position end) {
        try {
            return getGs().bwem.getMap().getPathLength(start, end);
        } catch (Exception e) {
            return start != null && end != null ? start.getDistance(end) : Integer.MAX_VALUE;
        }
    }

    public static boolean isConnected(TilePosition start, TilePosition end) {
        return !getGs().bwem.getMap().getPath(start.toPosition(), end.toPosition()).isEmpty();
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

    //Credits to @PurpleWaveJadien / Dan
    public static double broodWarDistance(Unit u, double[] b) {
        Position pos = u.getPosition();
        return broodWarDistance(pos.getX(), pos.getY(), (int) b[0], (int) b[1]);
    }

    public static Position chooseAttackPosition(Position p, boolean flying) {
        Position chosen = null;
        double maxScore = 0;
        for (UnitInfo b : getGs().unitStorage.getEnemyUnits().values().stream().filter(u -> u.unitType.isBuilding()).collect(Collectors.toSet())) {
            double influence = getScoreAttackPosition((Building) b.unit);
            //double score = influence / (2 * getEuclideanDist(p, b.pos.toPosition()));
            double score = influence / (2.5 * (flying ? b.lastPosition.getDistance(p) : Util.getGroundDistance(p, b.lastPosition)));
            if (score > maxScore) {
                chosen = b.lastPosition;
                maxScore = score;
            }
        }
        if (chosen == null && getGs().enemyMainBase == null) {
            for (BaseManager.Garrison g : getGs().baseManager.getScoutingBasesSorted()) {
                if (!flying && g.island) continue;
                return g.tile.toPosition();
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

    public static UnitInfo getClosestUnit(UnitInfo unit, Set<UnitInfo> enemies, boolean ignoreAir) {
        UnitInfo chosen = null;
        double minDist = Double.MAX_VALUE;
        for (UnitInfo u : enemies) {
            if (!u.unit.exists() || (ignoreAir && u.flying)) continue;
            double dist = unit.toUnitInfoDistance().getDistance(u);
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

    static Position cropPosition(Position pos) {
        MutablePair<Integer, Integer> cropped = new MutablePair<>(pos.getX(), pos.getY());
        int sizeX = getGs().getGame().getBWMap().mapWidth() * 32;
        int sizeY = getGs().getGame().getBWMap().mapHeight() * 32;
        if (cropped.first < 0.0) cropped.first = 0;
        else if (cropped.first >= sizeX) cropped.first = sizeX - 1;
        if (cropped.second < 0.0) cropped.second = 0;
        else if (cropped.second >= sizeY) cropped.second = sizeY - 1;
        return new Position(cropped.first, cropped.second);
    }

    public static Position choosePatrolPositionVulture(Vulture myUnit, UnitInfo attackUnit) {
        try {
            Position myUnitPos = myUnit.getPosition();
            Position attackUnitPos = attackUnit.lastPosition;
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
            System.err.println("choosePatrolPositionVulture Exception");
            e.printStackTrace();
            return null;
        }
    }

    private static MutablePair<Double, Double> normalize(MutablePair<Double, Double> pos) {
        double norm = Math.sqrt(pos.first * pos.first + pos.second * pos.second);
        return new MutablePair<>(pos.first / norm, pos.second / norm);
    }

    public static double getSpeed(UnitInfo unit) {
        if (unit.unitType.isBuilding() && !unit.flying) return 0.0;
        if (unit.burrowed) return 0.0;
        return unit.player.getUnitStatCalculator().topSpeed(unit.unitType);
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
        //double angle = Math.atan2(unitPos.getY(), unitPos.getX()) - Math.atan2(pos.getY(), pos.getX());
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
            if (getSquareTiles(b.getLocation(), UnitType.Terran_Command_Center).contains(pos.toTilePosition()))
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

    public static double getChokeWidth(ChokePoint choke) {
        List<WalkPosition> walks = choke.getGeometry();
        return walks.get(0).toPosition().getDistance(walks.get(walks.size() - 1).toPosition());
    }

    public static boolean isStaticDefense(UnitInfo u) {
        return isStaticDefense(u.unitType);
    }

    public static boolean isStaticDefense(UnitType u) {
        return u.isBuilding() && (u.canAttack() || u == UnitType.Terran_Bunker || u == UnitType.Zerg_Creep_Colony);
    }

    public static boolean isGroundStaticDefense(Unit u) {
        return u instanceof Bunker || u instanceof SunkenColony || u instanceof PhotonCannon;
    }

    public static boolean isAirStaticDefense(Unit u) {
        return u instanceof Bunker || u instanceof MissileTurret || u instanceof SporeColony || u instanceof PhotonCannon;
    }

    public static int countBuildingAll(UnitType type) {
        int count = 0;
        for (MutablePair<UnitType, TilePosition> w : getGs().workerBuild.values()) {
            if (w.first == type) count++;
        }
        count += countUnitTypeSelf(type);
        return count;
    }

    public static boolean hasFreePatches(Base base) {
        List<MineralPatch> minerals = base.getMinerals().stream().map(u -> (MineralPatch) u.getUnit()).collect(Collectors.toList());
        int count = 0;
        for (MineralPatch m : minerals) {
            if (getGs().mineralsAssigned.containsKey(m)) count += getGs().mineralsAssigned.get(m);
        }
        return count < 2 * minerals.size();
    }

    public static int getNumberCCs() {
        return getGs().CCs.size() + getGs().islandCCs.size();
    }

    public static boolean checkSiege() {
        boolean machineShop = false;
        if (getGs().Fs.isEmpty()) return false;
        int mS = (int) getGs().UBs.stream().filter(u -> u instanceof MachineShop).count();
        if (mS == 0) {
            for (Factory f : getGs().Fs) {
                if (f.getMachineShop() != null) {
                    machineShop = true;
                    break;
                }
            }
            return !machineShop;
        }
        return mS >= 1;
    }

    public static InteractionHandler getIH() {
        return getGs().getIH();
    }

    public static UnitInfo getTankTarget(UnitInfo t, Set<UnitInfo> tankTargets) {
        UnitInfo chosenTarget = null;
        int highPriority = 0;
        int closestDist = Integer.MAX_VALUE;
        for (UnitInfo target : tankTargets) {
            if (!target.visible) continue;
            int distance = t.toUnitInfoDistance().getDistance(target);
            int priority = getRangedAttackPriority(t, target);
            if (isStaticDefense(t)) priority *= 1.2;
            if (chosenTarget == null || (priority > highPriority) || (priority == highPriority && distance < closestDist)) {
                closestDist = distance;
                highPriority = priority;
                chosenTarget = target;
            }
        }
        return chosenTarget;
    }

    // Credits to SH
    public static UnitInfo getRangedTarget(UnitInfo rangedUnit, Set<UnitInfo> enemies, Position pos) {
        int bestScore = -999999;
        UnitInfo bestTarget = null;
        if (rangedUnit == null || enemies.isEmpty()) return null;
        if (pos == null) return getRangedTarget(rangedUnit, enemies);
        for (UnitInfo enemy : enemies) {
            if (enemy.unit == null || ((enemy.unit.isCloaked() || enemy.burrowed) && !enemy.unit.isDetected()))
                continue;
            if (enemy.flying && !(rangedUnit.unit instanceof AirAttacker)) continue;
            if (!enemy.flying && !(rangedUnit.unit instanceof GroundAttacker)) continue;
            int priority = getRangedAttackPriority(rangedUnit, enemy);
            int distance = rangedUnit.toUnitInfoDistance().getDistance(enemy);
            double closerToGoal = rangedUnit.toUnitInfoDistance().getDistance(pos) - enemy.toUnitInfoDistance().getDistance(pos);
            if (distance >= 13 * 32) continue;
            int score = 5 * 32 * priority - distance;
            if (closerToGoal > 0) score += 2 * 32;
            boolean isThreat = canAttack(enemy, rangedUnit);
            boolean canShootBack = isThreat && distance <= 32 + getAttackRange(enemy, rangedUnit);
            if (isThreat) {
                if (canShootBack) score += 7 * 32;
                else {
                    double weaponDist = enemy.player.getUnitStatCalculator().weaponMaxRange(getWeapon(enemy, rangedUnit));
                    if (distance < weaponDist) score += 6 * 32;
                    else score += 5 * 32;
                }
            } else if (enemy.unit instanceof MobileUnit && !((MobileUnit) enemy.unit).isMoving()) {
                if ((enemy.unit instanceof SiegeTank && ((SiegeTank) enemy.unit).isSieged()) || enemy.currentOrder == Order.Sieging ||
                        enemy.currentOrder == Order.Unsieging ||
                        (enemy.burrowed)) {
                    score += 48;
                } else score += 24;
            } else if (enemy.unit instanceof MobileUnit && ((MobileUnit) enemy.unit).isBraking()) score += 16;
            else if (enemy.speed >= rangedUnit.speed) {
                score -= 4 * 32;
            }
            if (enemy.unitType.getRace() == Race.Protoss && enemy.shields <= 5) score += 32;
            if (enemy.health < enemy.unitType.maxHitPoints()) score += 24;
            DamageType damage = getWeapon(rangedUnit, enemy).damageType();
            if (damage == DamageType.Explosive) {
                if (enemy.unitType.size() == UnitSizeType.Large) score += 32;

            } else if (damage == DamageType.Concussive) {
                if (enemy.unitType.size() == UnitSizeType.Small) score += 32;
                else if (enemy.unitType.size() == UnitSizeType.Large) score -= 32;
            }
            if (score > bestScore) {
                bestScore = score;
                bestTarget = enemy;
            }
        }
        return bestScore > 0 ? bestTarget : null;
    }

    // Credits to SH
    public static UnitInfo getRangedTarget(UnitInfo rangedUnit, Set<UnitInfo> enemies) {
        int bestScore = -999999;
        UnitInfo bestTarget = null;
        if (rangedUnit == null || enemies.isEmpty()) return null;
        for (UnitInfo enemy : enemies) {
            if (enemy.unit == null || ((enemy.unit.isCloaked() || enemy.burrowed) && !enemy.unit.isDetected()))
                continue;
            if (enemy.flying && !(rangedUnit.unit instanceof AirAttacker)) continue;
            if (!enemy.flying && !(rangedUnit.unit instanceof GroundAttacker)) continue;
            int priority = getRangedAttackPriority(rangedUnit, enemy);
            int distance = rangedUnit.toUnitInfoDistance().getDistance(enemy);
            if (distance >= 13 * 32) continue;
            int score = 5 * 32 * priority - distance;
            boolean isThreat = canAttack(enemy, rangedUnit);
            boolean canShootBack = isThreat && distance <= 32 + getAttackRange(enemy, rangedUnit);
            if (isThreat) {
                if (canShootBack) score += 7 * 32;
                else {
                    double weaponDist = enemy.player.getUnitStatCalculator().weaponMaxRange(getWeapon(enemy, rangedUnit));
                    if (distance < weaponDist) score += 6 * 32;
                    else score += 5 * 32;
                }
            } else if (enemy.unit instanceof MobileUnit && !((MobileUnit) enemy.unit).isMoving()) {
                if ((enemy.unit instanceof SiegeTank && ((SiegeTank) enemy.unit).isSieged()) || enemy.currentOrder == Order.Sieging ||
                        enemy.currentOrder == Order.Unsieging || enemy.burrowed) {
                    score += 48;
                } else score += 24;
            } else if (enemy.unit instanceof MobileUnit && ((MobileUnit) enemy.unit).isBraking()) score += 16;
            else if (enemy.speed >= rangedUnit.speed) {
                score -= 4 * 32;
            }
            if (enemy.unitType.getRace() == Race.Protoss && enemy.shields <= 5) score += 32;
            if (enemy.health < enemy.unitType.maxHitPoints()) score += 24;
            DamageType damage = getWeapon(rangedUnit, enemy).damageType();
            if (damage == DamageType.Explosive) {
                if (enemy.unitType.size() == UnitSizeType.Large) score += 32;

            } else if (damage == DamageType.Concussive) {
                if (enemy.unitType.size() == UnitSizeType.Small) score += 32;
                else if (enemy.unitType.size() == UnitSizeType.Large) score -= 32;
            }
            if (score > bestScore) {
                bestScore = score;
                bestTarget = enemy;
            }
        }
        return bestScore > 0 ? bestTarget : null;
    }

    // Credits to SH
    private static int getRangedAttackPriority(UnitInfo rangedUnit, UnitInfo target) {
        UnitType rangedType = rangedUnit.unitType;
        UnitType targetType = target.unitType;
        if (targetType == UnitType.Terran_Ghost && target.currentOrder == Order.NukePaint || target.currentOrder == Order.NukeTrack) {
            return 15;
        }
        if (rangedType.isFlyer()) {
            if (targetType == UnitType.Zerg_Scourge) return 12;
        } else if (targetType == UnitType.Terran_Vulture_Spider_Mine && !target.burrowed ||
                targetType == UnitType.Zerg_Infested_Terran) {
            return 12;
        }
        if (rangedType == UnitType.Terran_Wraith) {
            if (target.flying) return targetType.isFlyingBuilding() ? 5 : 11;
        } else if (rangedType == UnitType.Terran_Goliath && target.flying)
            return targetType.isFlyingBuilding() ? 8 : 10;
        if (rangedType.isFlyer() && target.unit instanceof SiegeTank) return 10;
        if (targetType == UnitType.Protoss_High_Templar || targetType == UnitType.Zerg_Defiler) return 12;
        if (targetType == UnitType.Protoss_Reaver || targetType == UnitType.Protoss_Arbiter) return 11;
        if (isStaticDefense(target)) {
            if (canAttack(target, rangedUnit)) {
                if (target.completed) return 9;
                return 10;
            } else if (target.completed) return 8;
            return 7;
        }
        if (canAttack(target, rangedUnit) && !targetType.isWorker()) {
            if (rangedUnit.toUnitInfoDistance().getDistance(target) > 48 + getAttackRange(target, rangedUnit)) return 8;
            return 10;
        }
        if (targetType == UnitType.Terran_Dropship || targetType == UnitType.Protoss_Shuttle) return 10;
        if (targetType == UnitType.Terran_Science_Vessel || targetType == UnitType.Zerg_Scourge || targetType == UnitType.Protoss_Observer) {
            return 10;
        }
        if (targetType.isWorker()) {
            if (rangedType == UnitType.Terran_Vulture) return 11;
            if (target.unit instanceof SCV) {
                if (((SCV) target.unit).isRepairing()) return 11;
                if (((SCV) target.unit).isConstructing()) {
                    if (getGs().getStrategyFromManager().proxy) {
                        Unit build = target.unit.getBuildUnit();
                        if ((build instanceof Bunker || build instanceof Factory)) return 15;
                        return 13;
                    }
                    return 10;
                }
            }
            return 9;
        }
        if (targetType == UnitType.Protoss_Carrier || targetType == UnitType.Terran_Siege_Tank_Tank_Mode ||
                targetType == UnitType.Terran_Siege_Tank_Siege_Mode) {
            return 8;
        }
        if (targetType.isSpellcaster() || targetType.groundWeapon() != WeaponType.None || targetType.airWeapon() != WeaponType.None) {
            return 7;
        }
        if (targetType == UnitType.Protoss_Templar_Archives) return 7;
        if (targetType == UnitType.Zerg_Spawning_Pool) return 7;
        if (targetType.isResourceDepot()) {
            if (getGs().getStrategyFromManager().proxy) return 3;
            else return 6;
        }
        if (targetType == UnitType.Protoss_Pylon) return 5;
        if (targetType == UnitType.Terran_Factory || targetType == UnitType.Terran_Armory) return 5;
        if (targetType.isBuilding() && (!target.completed || !target.unit.isPowered()) && !(targetType.isResourceDepot()
                || targetType.groundWeapon() != WeaponType.None || targetType.airWeapon() != WeaponType.None)) {
            return 2;
        }
        if (targetType.gasPrice() > 0) return 4;
        if (targetType.mineralPrice() > 0) return 3;
        return 1;
    }

    // Credits to SH
    public static int getAttackRange(UnitInfo attacker, UnitInfo target) {
        UnitType attackerType = attacker.unitType;
        if (attackerType == UnitType.Protoss_Reaver && !target.flying) return 8 * 32;
        if (attackerType == UnitType.Protoss_Carrier) return 8 * 32;
        if (attackerType == UnitType.Terran_Bunker) {
            return attacker.player.getUnitStatCalculator().weaponMaxRange(WeaponType.Gauss_Rifle) + 32;
        }
        WeaponType weapon = getWeapon(attacker, target);
        if (weapon == WeaponType.None) return 0;
        return attacker.player.getUnitStatCalculator().weaponMaxRange(weapon);
    }

    public static boolean isInOurBases(UnitInfo u) {
        if (u == null) return false;
        Area uArea = getGs().bwem.getMap().getArea(u.lastTileposition);
        if (uArea == null) return false;
        if (uArea.equals(getGs().enemyMainArea) || uArea.equals(getGs().enemyNaturalArea)) return false;
        for (Base b : getGs().CCs.keySet()) {
            if (b.getArea().equals(uArea)) return true;
        }
        return uArea.equals(getGs().naturalArea);
    }

    public static boolean isResearched(TechType tech) {
        return getGs().getPlayer().hasResearched(tech);
    }


    public static int getDistance(Unit unit, Position target) {
        if (!unit.exists() || target == null) return Integer.MAX_VALUE;
        int xDist = unit.getLeft() - target.getX();
        if (xDist < 0) {
            xDist = target.getX() - (unit.getRight() + 1);
            if (xDist < 0) xDist = 0;
        }
        int yDist = unit.getTop() - target.getY();
        if (yDist < 0) {
            yDist = target.getY() - (unit.getBottom() + 1);
            if (yDist < 0) yDist = 0;
        }
        return new Position(0, 0).getDistance(new Position(xDist, yDist));
    }
}
