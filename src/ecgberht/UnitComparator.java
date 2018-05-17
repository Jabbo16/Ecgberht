package ecgberht;

import org.openbw.bwapi4j.unit.Unit;

import java.util.Comparator;

public class UnitComparator implements Comparator<Object> {

    public int compare(Object e1, Object e2) {
        return ((Unit) e1).getId() - ((Unit) e2).getId();
    }
}