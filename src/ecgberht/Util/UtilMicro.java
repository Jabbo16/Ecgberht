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
        if (pos == null || attacker == null || !attacker.exists() || attacker.isAttackFrame()) return;
        Position targetPos = attacker.getTargetPosition();
        if (pos.equals(targetPos)) return;
        if (!getGs().getGame().getBWMap().isValidPosition(pos)) return;
        if (!attacker.isFlying() && !getGs().getGame().getBWMap().isWalkable(pos.toWalkPosition())) return;
        attacker.attack(pos);
    }

    public static void attack(Attacker attacker, Unit target) {
        if (attacker == null || target == null || !attacker.exists() || !target.exists()  || attacker.isAttackFrame()) return;
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
            if (enemies.isEmpty()) {
                return null;
            }
            Position ownPosition = unit.getPosition();
            List<MutablePair<Double, Double>> vectors = new ArrayList<>();

            //double minDistance = Double.MAX_VALUE;
            for (final Unit enemy : enemies) {
                final Position enemyPosition = enemy.getPosition();
                Position sub = ownPosition.subtract(enemyPosition);
                MutablePair<Double, Double> unitV = new MutablePair<>((double) sub.getX(), (double) sub.getY());
                final double distance = enemy.getDistance(unit);
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
}
