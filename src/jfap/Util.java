package jfap;

import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

public class Util {

    private static UnitType getZergType(PlayerUnit unit) {
        if (unit instanceof Zergling) {
            return UnitType.Zerg_Zergling;
        }
        if (unit instanceof Hydralisk) {
            return UnitType.Zerg_Hydralisk;
        }
        if (unit instanceof Mutalisk) {
            return UnitType.Zerg_Mutalisk;
        }
        if (unit instanceof Lurker) {
            return UnitType.Zerg_Lurker;
        }
        if (unit instanceof Queen) {
            return UnitType.Zerg_Queen;
        }
        if (unit instanceof Ultralisk) {
            return UnitType.Zerg_Ultralisk;
        }
        if (unit instanceof Guardian) {
            return UnitType.Zerg_Guardian;
        }
        if (unit instanceof Defiler) {
            return UnitType.Zerg_Defiler;
        }
        return unit.getInitialType();
    }

    private static UnitType getTerranType(PlayerUnit unit) {
        if (unit instanceof SiegeTank) {
            SiegeTank t = (SiegeTank) unit;
            return t.isSieged() ? UnitType.Terran_Siege_Tank_Siege_Mode : UnitType.Terran_Siege_Tank_Tank_Mode;
        }
        return unit.getInitialType();
    }

    private static UnitType getProtossType(PlayerUnit unit) {
        if (unit instanceof Archon) {
            return UnitType.Protoss_Archon;
        }
        return unit.getInitialType();
    }

    protected static UnitType getType(PlayerUnit unit) { // TODO TEST
        Race race = unit.getPlayer().getRace();
        UnitType type = UnitType.Unknown;
        if (race == Race.Terran) {
            type = getTerranType(unit);
        }
        if (type != UnitType.Unknown) return type;

        if (race == Race.Zerg) {
            type = getZergType(unit);
        }

        if (type != UnitType.Unknown) return type;

        if (race == Race.Protoss) {
            type = getProtossType(unit);
            if (type.getRace() != race) {
                return type.getRace() == Race.Zerg ? getZergType(unit) : getTerranType(unit);
            }
        }
        return unit.getInitialType();
    }
}
