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


import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.unit.Unit;

import static org.openbw.bwapi4j.type.UnitType.Special_Right_Pit_Door;

/**
 * StaticBuildings Correspond to the units in BWAPI::getStaticNeutralUnits() for which
 * getType().isSpecialBuilding. StaticBuilding also wrappers some special units like
 * Special_Pit_Door.
 */
public class StaticBuilding extends Neutral {
    StaticBuilding(final Unit unit, final BWMap map) {
        super(unit, map);
        // https://github.com/N00byEdge/BWEM-community/blob/cf377c14a6fbad91d6bb4ce6c232a77cb22b500c/BWEM/src/neutral.cpp#L36
        if (unit.getType() == Special_Right_Pit_Door) {
            topLeft = topLeft.add(new TilePosition(1, 0));
        }
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof StaticBuilding)) {
            return false;
        } else {
            final StaticBuilding that = (StaticBuilding) object;
            return (this.getUnit().getId() == that.getUnit().getId());
        }
    }


    @Override
    public int hashCode() {
        return getUnit().hashCode();
    }
}
