package ecgberht;

import bwapi.Order;
import bwapi.Unit;

public class SmartUnit{
	Unit ogUnit = null;
	int lastFrameOrder = 0;
	Unit target = null;
	Order lastOrder = null;
	
	public SmartUnit(Unit unit) {
		ogUnit = unit;
	}
	
}
