package ecgberht.Util;

import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.unit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static ecgberht.Ecgberht.getGs;

public class UtilMicro {

    public static void attack(MobileUnit attacker, Position pos) {
        if (pos == null || attacker == null || !attacker.exists() || attacker.isAttackFrame() || attacker.isStartingAttack())
            return;
        if (getGs().frameCount == attacker.getLastCommandFrame()) return;
        Position targetPos = attacker.getTargetPosition();
        if (pos.equals(targetPos)) return;
        if (!getGs().getGame().getBWMap().isValidPosition(pos)) return;
        if (!attacker.isFlying() && !getGs().getGame().getBWMap().isWalkable(pos.toWalkPosition())) return;
        attacker.attack(pos);
    }

    public static void attack(Attacker attacker, Unit target) {
        if (attacker == null || target == null || !attacker.exists() || !target.exists() || attacker.isAttackFrame())
            return;
        if (getGs().frameCount == attacker.getLastCommandFrame()) return;
        Unit targetUnit = attacker.getTargetUnit();
        if (target.equals(targetUnit)) return;
        attacker.attack(target);
    }

    public static void irradiate(ScienceVessel vessel, PlayerUnit target) {
        if (vessel == null || target == null || !vessel.exists() || !target.exists()) return;
        if (vessel.getOrder() == Order.CastIrradiate) {
            Unit targetUnit = vessel.getTargetUnit();
            if (target.equals(targetUnit)) return;
        }
        vessel.irradiate(target);
    }

    public static void defenseMatrix(ScienceVessel vessel, MobileUnit target) {
        if (vessel == null || target == null || !vessel.exists() || !target.exists()) return;
        if (vessel.getOrder() == Order.CastDefensiveMatrix) {
            Unit targetUnit = vessel.getTargetUnit();
            if (target.equals(targetUnit)) return;
        }
        vessel.defensiveMatrix(target);
    }

    public static void emp(ScienceVessel vessel, Position pos) {
        if (pos == null || vessel == null || !vessel.exists()) return;
        if (vessel.getOrder() == Order.CastEMPShockwave) {
            Position targetPos = vessel.getTargetPosition();
            if (pos.equals(targetPos)) return;
        }
        vessel.empShockWave(pos);
    }

    public static void move(MobileUnit u, Position pos) {
        if (pos == null || u == null || !u.exists()) return;
        Position targetPos = u.getTargetPosition();
        if (pos.equals(targetPos)) return;
        if (!getGs().getGame().getBWMap().isValidPosition(pos)) return;
        if (!u.isFlying() && !getGs().getGame().getBWMap().isWalkable(pos.toWalkPosition())) return;
        u.move(pos);
    }

    // Credits to @Yegers for a better kite method
    public static Position kiteAway(final Unit unit, final Set<Unit> enemies) {
        try {
            if (enemies.isEmpty()) return null;
            Position ownPosition = unit.getPosition();
            List<MutablePair<Double, Double>> vectors = new ArrayList<>();

            //double minDistance = Double.MAX_VALUE;
            for (final Unit enemy : enemies) {
                final Position enemyPosition = enemy.getPosition();
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

    public static void heal(Medic u, PlayerUnit heal) {
        if (u == null || heal == null || u.getLastCommandFrame() == getGs().frameCount) return;
        Unit targetUnit = u.getTargetUnit();
        if (heal.equals(targetUnit)) return;
        u.heal(heal);
    }

    public static void heal(Medic u, Position heal) {
        if (u == null || heal == null || !getGs().bw.getBWMap().isValidPosition(heal)) return;
        if (u.getLastCommandFrame() == getGs().frameCount) return;
        Position targetPos = u.getTargetPosition();
        if (heal.equals(targetPos)) return;
        u.heal(heal);
    }

    public static void stop(MobileUnit u) {
        if (getGs().frameCount == u.getLastCommandFrame()) return;
        if (u.getOrder() == Order.Stop) return;
        u.stop(false);
    }

    public static Position predictUnitPosition(Unit unit, int frames) {
        if (unit == null || !unit.exists() || !unit.isVisible()) return null;
        if (!(unit instanceof MobileUnit)) return unit.getPosition();
        return unit.getPosition().add(new Position((int) (frames * ((MobileUnit) unit).getVelocityX()), (int) (frames * ((MobileUnit) unit).getVelocityY())));
    }

    private static boolean verifyPosition(Position position) {
        if (!getGs().getGame().getBWMap().isValidPosition(position)) return false;
        if (getGs().map.getMap()[position.getY() / 32][position.getX() / 32].equals("0")) return false;
        return getGs().getGame().getBWMap().isWalkable(position.toWalkPosition());
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
        if (bestPosition != null && getGs().getGame().getBWMap().isValidPosition(bestPosition)) {
            return bestPosition;
        }
        return null;
    }

}
