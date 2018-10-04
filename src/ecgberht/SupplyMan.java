package ecgberht;

import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

public class SupplyMan {

    private int supplyUsed = 8;
    private int supplyTotal = 18;

    SupplyMan(Race race) {
        if (race == Race.Terran) supplyTotal = 20;
    }

    public int getSupplyLeft() {
        return supplyTotal - supplyUsed;
    }

    public int getSupplyUsed() {
        return supplyUsed;
    }

    int getSupplyTotal() {
        return supplyTotal;
    }

    void onCreate(Unit unit) {
        if (unit instanceof Building) return;
        UnitType type = unit.getType();
        if (type.supplyRequired() > 0) supplyUsed += type.supplyRequired();
    }

    void onComplete(Unit unit) {
        if (unit instanceof SupplyDepot || unit instanceof Pylon || unit instanceof Overlord || unit instanceof ResourceDepot) {
            UnitType type = unit.getType();
            if (type.supplyProvided() > 0) supplyTotal += type.supplyProvided();
        }
    }

    void onDestroy(Unit unit) {
        UnitType type = unit.getType();
        if (unit instanceof SupplyDepot || unit instanceof Pylon || unit instanceof Overlord || unit instanceof ResourceDepot) {
            if (type.supplyProvided() > 0) supplyTotal -= type.supplyProvided();
            return;
        }
        if (type.supplyRequired() > 0) supplyUsed -= type.supplyRequired();
    }
}
