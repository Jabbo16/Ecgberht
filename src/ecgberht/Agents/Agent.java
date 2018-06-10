package ecgberht.Agents;

import ecgberht.Util;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.Unit;

import java.util.Set;
import java.util.TreeSet;

import static ecgberht.Ecgberht.getGs;

public abstract class Agent {

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
        if (getGs().enemyBase != null) return getGs().enemyBase.getLocation().toPosition();
        else return getGs().EnemyBLs.get(1).getLocation().toPosition();
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

    public abstract boolean runAgent();

    enum Status {ATTACK, KITE, COMBAT, IDLE, RETREAT}
}
