package ecgberht;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

public class EnemyBuilding {
	public Unit unit = null;
	public TilePosition pos = null;
	public UnitType type = null;
	
	public EnemyBuilding(Unit unit) {
		this.unit = unit;
		this.pos = unit.getTilePosition();
		this.type = unit.getType();
	}
	
	public boolean equals(EnemyBuilding eB2) {
		return unit.equals(eB2.unit);
	}
}
