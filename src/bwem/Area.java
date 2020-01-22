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
import org.openbw.bwapi4j.WalkPosition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class Area {
    final java.util.Map<Area, List<ChokePoint>> chokePointsByArea = new HashMap<>();
    final List<Area> accessibleNeighbors = new ArrayList<>();
    final List<ChokePoint> chokePoints = new ArrayList<>();
    final List<Mineral> minerals = new ArrayList<>();
    final List<Geyser> geysers = new ArrayList<>();
    final List<Base> bases = new ArrayList<>();
    private final AreaId id;
    private final WalkPosition walkPositionWithHighestAltitude;
    private final int miniTileCount;
    int groupId = 0;
    Altitude highestAltitude;
    TilePosition topLeft = new TilePosition(Integer.MAX_VALUE, Integer.MAX_VALUE);
    TilePosition bottomRight = new TilePosition(Integer.MIN_VALUE, Integer.MIN_VALUE);
    int tileCount = 0;
    int buildableTileCount =
            0; /* Set and later incremented but not used in original C++ BWEM 1.4.1. Remains for portability consistency. */
    int highGroundTileCount = 0;
    int veryHighGroundTileCount = 0;

    protected final BWMap map;


    Area(final AreaId areaId, final WalkPosition top, final int miniTileCount, final BWMap map) {
        this.id = areaId;
        this.walkPositionWithHighestAltitude = top;
        this.miniTileCount = miniTileCount;
        this.map = map;
    }

    public AreaId getId() {
        return this.id;
    }

    public int getGroupId() {
        return this.groupId;
    }

    public TilePosition getTopLeft() {
        return this.topLeft;
    }

    public TilePosition getBottomRight() {
        return this.bottomRight;
    }

    public TilePosition getBoundingBoxSize() {
        return this.bottomRight.subtract(this.topLeft).add(new TilePosition(1, 1));
    }

    public WalkPosition getWalkPositionWithHighestAltitude() {
        return this.walkPositionWithHighestAltitude;
    }

    public WalkPosition getTop() {
        return getWalkPositionWithHighestAltitude();
    }

    public Altitude getHighestAltitude() {
        return this.highestAltitude;
    }

    public int getSize() {
        return this.miniTileCount;
    }

    public int getLowGroundPercentage() {
        final int lowGroundTileCount =
                this.tileCount - this.highGroundTileCount - this.veryHighGroundTileCount;
        return ((lowGroundTileCount * 100) / this.tileCount);
    }

    public int getHighGroundPercentage() {
        return ((this.highGroundTileCount * 100) / this.tileCount);
    }

    public int getVeryHighGroundPercentage() {
        return ((this.veryHighGroundTileCount * 100) / tileCount);
    }

    public List<ChokePoint> getChokePoints() {
        return this.chokePoints;
    }

    public List<ChokePoint> getChokePoints(final Area area) {
        final List<ChokePoint> ret = this.chokePointsByArea.get(area);
        if (ret == null) {
            map.asserter.throwIllegalStateException("");
        }
        return ret;
    }

    public java.util.Map<Area, List<ChokePoint>> getChokePointsByArea() {
        return this.chokePointsByArea;
    }

    public List<Area> getAccessibleNeighbors() {
        return this.accessibleNeighbors;
    }

    public boolean isAccessibleFrom(final Area area) {
        return groupId == area.getGroupId();
    }

    public List<Mineral> getMinerals() {
        return this.minerals;
    }

    public List<Geyser> getGeysers() {
        return this.geysers;
    }

    public List<Base> getBases() {
        return this.bases;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof Area)) {
            return false;
        } else {
            final Area that = (Area) object;
            return (getId().equals(that.getId()));
        }
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
