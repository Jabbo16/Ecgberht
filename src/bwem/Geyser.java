// Original work Copyright (c) 2015, 2017, Igor Dimitrijevic
// Modified work Copyright (c) 2017-2018 OpenBW Team

//////////////////////////////////////////////////////////////////////////
//
// This file is part of the BWEM Library.
// BWEM is free software, licensed under the MIT/X11 License.
// A copy of the license is provided with the library in the LICENSE file.
// Copyright (c) 2015, 2017, Igor Dimitrijevic
//
//////////////////////////////////////////////////////////////////////////

package bwem;

import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Unit;

/**
 * Geysers Correspond to the units in BWAPI::getStaticNeutralUnits() for which getType() ==
 * Resource_Vespene_Geyser.
 */
public final class Geyser extends Resource {
    Geyser(final Unit unit, final BWMap map) {
        super(unit, map);

        if (!unit.getType().equals(UnitType.Resource_Vespene_Geyser)) {
            map.asserter.throwIllegalStateException(
                    "Unit is not a VespeneGeyser: " + unit.getType());
        }
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof Geyser)) {
            return false;
        } else {
            final Geyser that = (Geyser) object;
            return (this.getUnit().getId() == that.getUnit().getId());
        }
    }

    @Override
    public int hashCode() {
        return getUnit().hashCode();
    }
}
