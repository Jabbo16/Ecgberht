package ecgberht;

import bwapi.Unit;

import java.util.Comparator;

public class UnitComparator implements Comparator<Unit> {

    public int compare(Unit e1, Unit e2) {
        return e1.getID() - e2.getID();
    }
}