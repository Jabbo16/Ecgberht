package jfap;

import bwapi.Unit;

public abstract class AEnemyData {
	
	abstract void initFromUnit();
	abstract void updateFromUnit();
	abstract void updateFromUnit(final Unit unit);
	abstract int expectedHealth();
	abstract int expectedShields();
	abstract boolean isFriendly();
	abstract boolean isEnemy();
}
