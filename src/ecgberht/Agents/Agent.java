package ecgberht.Agents;

import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.Unit;

import java.util.Set;
import java.util.TreeSet;

import static ecgberht.Ecgberht.getGs;

public abstract class Agent {

    public Unit myUnit;
    Status status = Status.IDLE;
    UnitType type = UnitType.Terran_Vulture;
    Position attackPos = null;
    Unit attackUnit = null;
    int frameLastOrder = 0;
    int actualFrame = 0;
    Set<Unit> closeEnemies = new TreeSet<>();
    Set<Unit> closeWorkers = new TreeSet<>();

    public String statusToString() {
        if (status == Status.ATTACK) return "Attack";
        if (status == Status.KITE) return "Kite";
        if (status == Status.COMBAT) return "Combat";
        if (status == Status.RETREAT) return "Retreat";
        if (status == Status.IDLE) return "Idle";
        return "None";
    }

    Position selectNewAttack() {
        MutablePair<Integer, Integer> p = getGs().inMap.getPosition(myUnit.getTilePosition(), true);
        if (p.first != -1 && p.second != -1) {
            Position attackPos = new TilePosition(p.second, p.first).toPosition();
            if (getGs().getGame().getBWMap().isValidPosition(attackPos)) return attackPos;
        }
        if (getGs().enemyMainBase != null) return getGs().enemyMainBase.getLocation().toPosition();
        return null;
    }

    Unit getUnitToAttack(Unit myUnit, Set<Unit> enemies) {
        Unit chosen = null;
        double distB = Double.MAX_VALUE;
        for (Unit u : enemies) {
            if (Util.getType((PlayerUnit) u).isFlyer() || ((PlayerUnit) u).isCloaked()) continue;
            double distA = getGs().broodWarDistance(myUnit.getPosition(), u.getPosition());
            if (chosen == null || distA < distB) {
                chosen = u;
                distB = distA;
            }
        }
        if (chosen != null) return chosen;
        return null;
    }

    protected void retreat() {
        Position CC = getGs().getNearestCC(myUnit.getPosition());
        if (CC != null) ((MobileUnit) myUnit).move(CC);
        else ((MobileUnit) myUnit).move(getGs().getPlayer().getStartLocation().toPosition());
        attackPos = null;
        attackUnit = null;
    }

    public abstract boolean runAgent();

    enum Status {ATTACK, KITE, COMBAT, IDLE, RETREAT}
}
