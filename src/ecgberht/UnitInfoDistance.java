package ecgberht;

import ecgberht.Util.Util;
import ecgberht.Util.UtilMicro;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.WalkPosition;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.Unit;

public class UnitInfoDistance {

    private UnitInfo unitInfo;

    public UnitInfoDistance(UnitInfo unitInfo) {
        this.unitInfo = unitInfo;
    }

    public int getDistance(Position pos) {
        if (unitInfo.visible) {
            return Util.getDistance(unitInfo.unit, pos);
        } else {
            return unitInfo.lastPosition.getDistance(pos);
        }
    }

    public int getDistance(Unit u) {
        if (unitInfo.visible) {
            return unitInfo.unit.getDistance(u);
        } else {
            return unitInfo.lastPosition.getDistance(u.getPosition());
        }
    }

    public int getDistance(UnitInfo target) {
        if (unitInfo.visible)
            return target.visible ? unitInfo.unit.getDistance(target.unit) : this.getDistance(target.lastPosition);
        return target.visible ? unitInfo.lastPosition.getDistance(unitInfo.lastPosition) : target.lastPosition.getDistance(unitInfo.lastPosition);
    }

    public double getPredictedDistance(UnitInfo target) {
        Position nextPosition = UtilMicro.predictUnitPosition(target, 1);
        if (nextPosition == null) {
            return getDistance(target);
        } else {
            return getDistance(nextPosition);
        }
    }

    public double getPredictedDistance(UnitInfo target, int frames) {
        Position nextPosition = UtilMicro.predictUnitPosition(target, frames);
        if (nextPosition == null) {
            return getDistance(target);
        } else {
            return getDistance(nextPosition);
        }
    }
}
