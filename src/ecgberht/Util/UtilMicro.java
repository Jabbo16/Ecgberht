package ecgberht.Util;

import bwapi.*;
import ecgberht.UnitInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static ecgberht.Ecgberht.getGs;

public class UtilMicro {

    public static void attack(Unit attacker, Position pos) {
        if (pos == null || attacker == null || !attacker.exists() || attacker.isAttackFrame() || attacker.isStartingAttack())
            return;
        if (getGs().frameCount == attacker.getLastCommandFrame()) return;
        Position targetPos = attacker.getTargetPosition();
        if (pos.equals(targetPos)) return;
        if (!pos.isValid(getGs().bw)) return;
        if (!attacker.isFlying() && !getGs().bw.isWalkable(pos.toWalkPosition())) return;
        attacker.attack(pos);
    }

    public static void attack(UnitInfo attacker, UnitInfo target) {
        try {
            Unit attackerUnit = attacker.unit;
            if (attackerUnit == null || target == null || !attackerUnit.exists() || attackerUnit.isStartingAttack() || attackerUnit.isAttackFrame())
                return;
            if (getGs().frameCount == attackerUnit.getLastCommandFrame()) return;
            Unit targetUnit = attackerUnit.getTarget();
            if (target.unit.equals(targetUnit)) return;
            if (target.visible) {
                WeaponType w = Util.getWeapon(attacker, target);
                double range = Util.getAttackRange(attacker, target);
                if (attacker.getDistance(target) <= range) {
                    attackerUnit.attack(target.unit);
                    return;
                }
                Position predicted = predictUnitPosition(target, 3);
                if (predicted != null) move(attackerUnit, predicted);
            } else move(attackerUnit, target.lastPosition);
        } catch (Exception e) {
            System.err.println("UtilMicro Attack Exception");
            e.printStackTrace();
        }
    }

    public static void attack(Unit attacker, UnitInfo target) {
        try {
            if (attacker == null || target == null || !attacker.exists() || attacker.isStartingAttack() || attacker.isAttackFrame())
                return;
            if (getGs().frameCount == attacker.getLastCommandFrame()) return;
            Unit targetUnit = attacker.getTarget();
            if (target.unit.equals(targetUnit)) return;
            if (target.visible) attacker.attack(target.unit);
            else attacker.move(target.lastPosition);
        } catch (Exception e) {
            System.err.println("UtilMicro Attack Exception");
            e.printStackTrace();
        }
    }

    public static void irradiate(Unit vessel, Unit target) {
        if (vessel == null || target == null || !vessel.exists() || !target.exists()) return;
        if (vessel.getOrder() == Order.CastIrradiate) {
            Unit targetUnit = vessel.getTarget();
            if (target.equals(targetUnit)) return;
        }
        vessel.useTech(TechType.Irradiate, target);
    }

    public static void defenseMatrix(Unit vessel, Unit target) {
        if (vessel == null || target == null || !vessel.exists() || !target.exists()) return;
        if (vessel.getOrder() == Order.CastDefensiveMatrix) {
            Unit targetUnit = vessel.getTarget();
            if (target.equals(targetUnit)) return;
        }
        vessel.useTech(TechType.Defensive_Matrix, target);
    }

    public static void emp(Unit vessel, Position pos) {
        if (pos == null || vessel == null || !vessel.exists()) return;
        if (vessel.getOrder() == Order.CastEMPShockwave) {
            Position targetPos = vessel.getTargetPosition();
            if (pos.equals(targetPos)) return;
        }
        vessel.useTech(TechType.EMP_Shockwave, pos);
    }

    public static void move(Unit u, Position pos) {
        if (pos == null || u == null || !u.exists()) return;
        if (getGs().frameCount == u.getLastCommandFrame()) return;
        Position targetPos = u.getTargetPosition();
        if (pos.equals(targetPos) || (targetPos != null && pos.toTilePosition().equals(targetPos.toTilePosition())))
            return;
        if (!pos.isValid(getGs().bw)) return;
        if (!u.isFlying() && !getGs().bw.isWalkable(pos.toWalkPosition())) return;
        u.move(pos);
    }

    // Credits to @Yegers for a better kite method
    public static Position kiteAway(final Unit unit, final Set<UnitInfo> enemies) {
        try {
            if (enemies.isEmpty()) return null;
            Position ownPosition = unit.getPosition();
            List<MutablePair<Double, Double>> vectors = new ArrayList<>();

            //double minDistance = Double.MAX_VALUE;
            for (final UnitInfo enemy : enemies) {
                final Position enemyPosition = enemy.position;
                Position sub = ownPosition.subtract(enemyPosition);
                MutablePair<Double, Double> unitV = new MutablePair<>((double) sub.getX(), (double) sub.getY());
                //final double distance = enemy.getDistance(unit);
                /*if (distance < minDistance) {
                    minDistance = distance;
                }*/
                /*unitV.first = unitV.first / distance;
                unitV.second = unitV.second / distance;*/
                vectors.add(unitV);
            }
            //minDistance *= 2;
            /*for (MutablePair<Double, Double> vector : vectors){
                vector.first *= minDistance;
                vector.second *= minDistance;
            }*/
            //return GenericMath.add(ownPosition, GenericMath.multiply(1. / vectors.size(), GenericMath.sumAll(vectors)));
            MutablePair<Double, Double> sumAll = Util.sumPosition(vectors);
            return Util.cropPosition(Util.sumPosition(ownPosition, new Position((int) (sumAll.first / vectors.size()), (int) (sumAll.second / vectors.size()))));
        } catch (Exception e) {
            System.err.println("KiteAway Exception");
            e.printStackTrace();
            return null;
        }
    }

    public static void heal(Unit u, Unit heal) {
        if (u == null || heal == null || u.getLastCommandFrame() == getGs().frameCount) return;
        Unit targetUnit = u.getTarget();
        if (heal.equals(targetUnit)) return;
        u.useTech(TechType.Healing, heal);
    }

    public static void heal(Unit u, Position heal) {
        if (u == null || heal == null || !heal.isValid(getGs().bw)) return;
        if (u.getLastCommandFrame() == getGs().frameCount) return;
        Position targetPos = u.getTargetPosition();
        if (heal.equals(targetPos) || (targetPos != null && heal.toTilePosition().equals(targetPos.toTilePosition())))
            return;
        u.useTech(TechType.Healing, heal);
    }

    public static void stop(Unit u) {
        if (getGs().frameCount == u.getLastCommandFrame()) return;
        if (u.getOrder() == Order.Stop) return;
        u.stop(false);
    }

    public static Position predictUnitPosition(UnitInfo unit, int frames) {
        if (unit == null) return null;
        if (unit.speed == 0.0) return unit.lastPosition;
        return unit.lastPosition.add(new Position((int) (frames * unit.unit.getVelocityX()), (int) (frames * unit.unit.getVelocityY())));
    }

    private static boolean verifyPosition(Position position) {
        if (!position.isValid(getGs().bw)) return false;
        if (getGs().map.getMap()[position.getY() / 32][position.getX() / 32].equals("0")) return false;
        return getGs().bw.isWalkable(position.toWalkPosition());
    }

    // Based on @Locutus micro logic, credits to him.
    public static Position kiteAwayAlt(Position unitPos, Position fleePos) {
        Position delta = fleePos.subtract(unitPos);
        double angleToTarget = Math.atan2(delta.getY(), delta.getX());
        Position bestPosition = null;
        boolean shouldBreak = false;
        for (int i = 0; i <= 3; i++) {
            if (shouldBreak) break;
            for (int sign = -1; i == 0 ? sign == -1 : sign <= 1; sign += 2) {
                double a = angleToTarget + (i * sign * Math.PI / 6);
                Position position = new Position(unitPos.getX() - (int) Math.round(64.0 * Math.cos(a)),
                        unitPos.getY() - (int) Math.round(64.0 * Math.sin(a)));
                if (!verifyPosition(position) || !verifyPosition(position.add(new Position(-16, -16))) ||
                        !verifyPosition(position.add(new Position(16, -16))) ||
                        !verifyPosition(position.add(new Position(16, 16))) ||
                        !verifyPosition(position.add(new Position(-16, 16)))) {
                    continue;
                }
                bestPosition = position;
                shouldBreak = true;
            }
        }
        if (bestPosition != null && bestPosition.isValid(getGs().bw)) {
            return bestPosition;
        }
        return null;
    }

}
