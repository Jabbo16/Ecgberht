package ecgberht;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;

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
        if (unit.getType().isBuilding()) return;
        UnitType type = unit.getType();
        if (type.supplyRequired() > 0) supplyUsed += type.supplyRequired();
    }

    void onComplete(Unit unit) {
        if (unit.getType().supplyProvided() > 0) supplyTotal += unit.getType().supplyProvided();

    }

    void onDestroy(Unit unit) {
        UnitType type = unit.getType();
        if (type.supplyProvided() > 0){
            supplyTotal -= type.supplyProvided();
            return;
        }
        if (type.supplyRequired() > 0) supplyUsed -= type.supplyRequired();
    }
}
