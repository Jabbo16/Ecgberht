package ecgberht;

import ecgberht.Util.Util;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.Unit;

import java.util.Objects;

public class EnemyBuilding {
    public TilePosition pos;
    public Building unit;
    public UnitType type;

    public EnemyBuilding(Unit unit) {
        this.unit = (Building) unit;
        this.pos = unit.getTilePosition();
        this.type = Util.getType((PlayerUnit) unit);
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
