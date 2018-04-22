package ecgberht;

import java.util.Comparator;

import org.openbw.bwapi4j.unit.Unit;

public class UnitComparator implements Comparator<Unit>{

	public int compare(Unit e1, Unit e2) {
		return e1.getId() - e2.getId();
	}
}