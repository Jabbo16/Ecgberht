package ecgberht;

import java.util.Comparator;

import bwapi.Unit;

public class UnitComparator implements Comparator<Unit>{

	public int compare(Unit e1, Unit e2) {
		return e1.getID() - e2.getID();
	}
}