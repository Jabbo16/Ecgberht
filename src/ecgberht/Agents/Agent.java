package ecgberht.Agents;

import ecgberht.UnitInfo;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.Unit;

import static ecgberht.Ecgberht.getGs;

public abstract class Agent {

    public Unit myUnit;
    public UnitInfo unitInfo;
    Status status = Status.IDLE;
    Position attackPos = null;
    UnitInfo attackUnit = null;
    int frameLastOrder = 0;
    int actualFrame = 0;

    Agent(Unit unit) {
        this.unitInfo = getGs().unitStorage.getAllyUnits().get(unit);
        this.myUnit = unit;
    }

    public String statusToString() {
        if (status == Status.ATTACK) return "Attack";
        if (status == Status.KITE) return "Kite";
        if (status == Status.COMBAT) return "Combat";
        if (status == Status.RETREAT) return "Retreat";
        if (status == Status.IDLE) return "Idle";
        if (status == Status.PATROL) return "Patrol";
        return "None";
    }

    public abstract boolean runAgent();

    enum Status {ATTACK, KITE, COMBAT, IDLE, RETREAT, PATROL}
}
