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

import bwem.util.BwemExt;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * After Areas and ChokePoints, Bases are the third kind of object BWEM automatically computes from
 * Brood War's maps. A Base is essentially a suggested location (intended to be optimal) to put a
 * resource depot. It also provides information on the resources available, and some statistics. A
 * Base always belongs to some Area. An Area may contain zero, one or several Bases. Like Areas and
 * ChokePoints, the number and the addresses of Base instances remain unchanged.
 */
public final class Base {
    private final Area area;
    private final List<Mineral> minerals = new ArrayList<>();
    private final List<Geyser> geysers = new ArrayList<>();
    private final List<Mineral> blockingMinerals;
    private TilePosition location;
    private Position center;
    private boolean isStartingLocation = false;

    private final Asserter asserter;

    Base(
            final Area area,
            final TilePosition location,
            final List<Resource> assignedResources,
            final List<Mineral> blockingMinerals,
            final Asserter asserter) {
        this.area = area;
        this.location = location;
        this.center = BwemExt.centerOfBuilding(location, UnitType.Terran_Command_Center.tileSize());
        this.blockingMinerals = blockingMinerals;
        this.asserter = asserter;

        //        bwem_assert(!AssignedResources.empty());
        if (assignedResources.isEmpty()) {
            asserter.throwIllegalStateException("");
        }

        for (final Resource assignedResource : assignedResources) {
            if (assignedResource instanceof Mineral) {
                final Mineral assignedMineral = (Mineral) assignedResource;
                this.minerals.add(assignedMineral);
            } else if (assignedResource instanceof Geyser) {
                final Geyser assignedGeyser = (Geyser) assignedResource;
                this.geysers.add(assignedGeyser);
            }
        }
    }

    /**
     * Tests whether this base is a start location.<br>
     * - Note: all players start at locations taken from {@link MapData#getStartingLocations()},<br>
     * which doesn't mean all the locations in {@link MapData#getStartingLocations()} are actually
     * used.
     */
    public boolean isStartingLocation() {
        return this.isStartingLocation;
    }

    /**
     * Returns the area in which this base is located.
     */
    public Area getArea() {
        return this.area;
    }

    /**
     * Returns the position (top-left TilePosition) of the location for a resource depot.<br>
     * - Note: If {@link #isStartingLocation()} == true, it is guaranteed that the location
     * corresponds exactly to one of {@link MapData#getStartingLocations()}.
     */
    public TilePosition getLocation() {
        return this.location;
    }

    /**
     * Returns the center position of {@link #getLocation()}.
     */
    public Position getCenter() {
        return this.center;
    }

    /**
     * Returns the available minerals.<br>
     * - These minerals are assigned to this base (it is guaranteed that no other base provides them).
     * <br>
     * - Note: The size of the returned list may decrease, as some of the minerals may get destroyed.
     */
    public List<Mineral> getMinerals() {
        return this.minerals;
    }

    /**
     * Returns the available geysers.<br>
     * - These geysers are assigned to this Base (it is guaranteed that no other Base provides them).
     * <br>
     * - Note: The size of the returned list will NOT decrease, as geysers never get destroyed.
     */
    public List<Geyser> getGeysers() {
        return this.geysers;
    }

    /**
     * Returns the blocking minerals.<br>
     * - These are special minerals. They are placed at or near the resource depot location,<br>
     * thus blocking the building of a resource depot from being close to the resources.<br>
     * - So, before trying to build a resource depot, these minerals must be gathered first.<br>
     * - Fortunately, these are guaranteed to have their initialAmount() <= 8.<br>
     * - As an example of blocking minerals, see the two islands in Andromeda.scx.<br>
     * - Note: if {@link #isStartingLocation()} == true, an empty list is returned.<br>
     * - Note: should not be confused with {@link ChokePoint#getBlockingNeutral()}
     * and {@link Neutral#isBlocking()}:<br>
     * The last two refer to a Neutral blocking a ChokePoint, not a Base.
     */
    public List<Mineral> getBlockingMinerals() {
        return this.blockingMinerals;
    }

    void assignStartingLocation(final TilePosition actualLocation) {
        this.isStartingLocation = true;
        this.location = actualLocation;
        this.center = BwemExt.centerOfBuilding(actualLocation, UnitType.Terran_Command_Center.tileSize());
    }

    public void onMineralDestroyed(final Mineral mineral) {
        //    	bwem_assert(pMineral);
        if (mineral == null) {
            asserter.throwIllegalStateException("");
        }

        this.minerals.remove(mineral);
        this.blockingMinerals.remove(mineral);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof Base)) {
            return false;
        } else {
            final Base that = (Base) object;
            return (getArea().equals(that.getArea())
                    && getLocation().equals(that.getLocation())
                    && getCenter().equals(that.getCenter()));
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.area, this.location, this.center);
    }
}
