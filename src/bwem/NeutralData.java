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

import org.openbw.bwapi4j.unit.Unit;

import java.util.ArrayList;
import java.util.List;

import static org.openbw.bwapi4j.type.UnitType.*;

public final class NeutralData {
    private final List<Mineral> minerals;
    private final List<Geyser> geysers;
    private final List<StaticBuilding> staticBuildings;

    NeutralData(
        final BWMap map,
        final List<Unit> mineralPatches,
        final List<Unit> vespeneGeysers,
        final List<Unit> neutralUnits) {
        ////////////////////////////////////////////////////////////////////////
        // BWMap::InitializeNeutrals
        ////////////////////////////////////////////////////////////////////////

        this.minerals = new ArrayList<>();
        for (final Unit mineralPatch : mineralPatches) {
            this.minerals.add(new Mineral(mineralPatch, map));
        }

        this.geysers = new ArrayList<>();
        for (final Unit vespeneGeyser : vespeneGeysers) {
            this.geysers.add(new Geyser(vespeneGeyser, map));
        }

        this.staticBuildings = new ArrayList<>();
        for (final Unit neutralUnit : neutralUnits) {
            if (neutralUnit.getType().isBuilding()) {
                this.staticBuildings.add(new StaticBuilding(neutralUnit, map));
            }
            if ( neutralUnit.getType() == Special_Pit_Door || neutralUnit.getType() == Special_Right_Pit_Door) {
                this.staticBuildings.add(new StaticBuilding(neutralUnit, map));
            }
        }
    }

    public List<Mineral> getMinerals() {
        return this.minerals;
    }

    public Mineral getMineral(final Unit unit) {
        for (final Mineral mineral : getMinerals()) {
            if (mineral.getUnit().equals(unit)) {
                return mineral;
            }
        }
        return null;
    }

    public List<Geyser> getGeysers() {
        return this.geysers;
    }

    public Geyser getGeyser(final Unit unit) {
        for (final Geyser geyser : getGeysers()) {
            if (geyser.getUnit().equals(unit)) {
                return geyser;
            }
        }
        return null;
    }

    public List<StaticBuilding> getStaticBuildings() {
        return this.staticBuildings;
    }
}
