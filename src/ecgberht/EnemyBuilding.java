package ecgberht;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

import java.util.Objects;

public class EnemyBuilding {
    public TilePosition pos = null;
    public Unit unit = null;
    public UnitType type = null;

    public EnemyBuilding(Unit unit) {
        this.unit = unit;
        this.pos = unit.getTilePosition();
        this.type = unit.getType();
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof EnemyBuilding)) {
            return false;
        }
        EnemyBuilding eB2 = (EnemyBuilding) o;
        return unit.equals(eB2.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit);
    }
}
