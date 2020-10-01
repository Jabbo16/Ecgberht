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

import bwem.util.Pair;
import org.openbw.bwapi4j.WalkPosition;

import java.util.Comparator;

import static bwem.AreaId.UNINITIALIZED;

/**
 * Corresponds to BWAPI/Starcraft's concept of walk tile (8x8 pixels).<br>
 * - MiniTiles are accessed using WalkPositions {@link TerrainData#getMiniTile(WalkPosition)}<br>
 * - A BWMap holds BWMap::WalkSize().x * BWMap::WalkSize().y MiniTiles as its "MiniTile map".<br>
 * - A MiniTile contains essentialy 3 pieces of information:<br>
 * i) its Walkability<br>
 * ii) its altitude (distance from the nearest non walkable MiniTile, except those which are part of
 * small enough zones (lakes))<br>
 * iii) the id of the Area it is part of, if ever.<br>
 * - The whole process of analysis of a BWMap relies on the walkability information<br>
 * from which are derived successively: altitudes, Areas, ChokePoints.
 */
public final class MiniTile {
    private static final AreaId blockingCP = new AreaId(Integer.MIN_VALUE);
    public static final Comparator<Pair<?, MiniTile>> BY_ALTITUDE_ORDER = Comparator.comparing(p -> p.getRight().getAltitude().intValue());

    private Altitude
            altitude; // 0 for seas  ;  != 0 for terrain and lakes (-1 = not computed yet)  ;  1 =
    // SeaOrLake intermediate value
    private AreaId
            areaId; // 0 -> unwalkable  ;  > 0 -> index of some Area  ;  < 0 -> some walkable terrain, but
    // too small to be part of an Area

    private final Asserter asserter;

    MiniTile(final Asserter asserter) {
        this.altitude = Altitude.UNINITIALIZED;
        this.areaId = UNINITIALIZED;
        this.asserter = asserter;
    }

    /**
     * Corresponds approximatively to BWAPI::isWalkable<br>
     * The differences are:<br>
     * - For each BWAPI's unwalkable MiniTile, we also mark its 8 neighbors as not walkable.<br>
     * According to some tests, this prevents from wrongly pretending one small unit can go by some
     * thin path.<br>
     * - The relation buildable ==> walkable is enforced, by marking as walkable any MiniTile part of
     * a buildable Tile (Cf. {@link Tile#isBuildable()})<br>
     * Among the miniTiles having Altitude() > 0, the walkable ones are considered Terrain-miniTiles,
     * and the other ones Lake-miniTiles.
     */
    public boolean isWalkable() {
        return (this.areaId.intValue() != 0);
    }

    void setWalkable(boolean walkable) {
        this.areaId = new AreaId(walkable ? -1 : 0);
        this.altitude = new Altitude(walkable ? -1 : 1);
    }

    /**
     * Distance in pixels between the center of this MiniTile and the center of the nearest
     * Sea-MiniTile<br>
     * - Sea-miniTiles all have their Altitude() equal to 0.<br>
     * - miniTiles having Altitude() > 0 are not Sea-miniTiles. They can be either Terrain-miniTiles
     * or Lake-miniTiles.
     */
    public Altitude getAltitude() {
        return this.altitude;
    }

    void setAltitude(final Altitude altitude) {
        //        { bwem_assert_debug_only(AltitudeMissing() && (a > 0)); this.altitude = a; }
        if (!(isAltitudeMissing() && altitude.intValue() > 0)) {
            asserter.throwIllegalStateException("");
        }
        this.altitude = altitude;
    }

    /**
     * Sea-miniTiles are unwalkable miniTiles that have their altitude equal to 0.
     */
    public boolean isSea() {
        return (this.altitude.intValue() == 0);
    }

    /**
     * Lake-miniTiles are unwalkable miniTiles that have their Altitude() > 0.<br>
     * - They form small zones (inside Terrain-zones) that can be eaysily walked around (e.g.
     * Starcraft's doodads)<br>
     * - The intent is to preserve the continuity of altitudes inside areas.
     */
    public boolean isLake() {
        return (this.altitude.intValue() != 0 && !isWalkable());
    }

    /**
     * Terrain miniTiles are just walkable miniTiles
     */
    public boolean isTerrain() {
        return isWalkable();
    }

    /**
     * For Sea and Lake miniTiles, returns 0<br>
     * For Terrain miniTiles, returns a non zero id:<br>
     * - if (id > 0), id uniquely identifies the Area A that contains this MiniTile.<br>
     * Moreover we have: A.id() == id and BWMap::getArea(id) == A<br>
     * - For more information about positive Area::ids, see Area::id()<br>
     * - if (id < 0), then this MiniTile is part of a Terrain-zone that was considered too small to
     * create an Area for it.<br>
     * - Note: negative Area::ids start from -2<br>
     * - Note: because of the lakes, BWMap::getNearestArea should be prefered over BWMap::getArea.
     */
    public AreaId getAreaId() {
        return this.areaId;
    }

    void setAreaId(final AreaId areaId) {
        //        { bwem_assert(AreaIdMissing() && (id >= 1)); this.areaId = id; }
        if (!(isAreaIdMissing() && areaId.intValue() >= 1)) {
            asserter.throwIllegalStateException("");
        }
        this.areaId = areaId;
    }

    boolean isSeaOrLake() {
        return (this.altitude.intValue() == 1);
    }

    void setSea() {
        //        { bwem_assert(!Walkable() && SeaOrLake()); this.altitude = 0; }
        if (!(!isWalkable() && isSeaOrLake())) {
            asserter.throwIllegalStateException("");
        }
        this.altitude = Altitude.ZERO;
    }

    void setLake() {
        //        { bwem_assert(!Walkable() && Sea()); this.altitude = -1; }
        if (!(!isWalkable() && isSea())) {
            asserter.throwIllegalStateException("");
        }
        this.altitude = Altitude.UNINITIALIZED;
    }

    boolean isAltitudeMissing() {
        return (altitude.intValue() == -1);
    }

    boolean isAreaIdMissing() {
        return this.areaId.equals(UNINITIALIZED);
    }

    void replaceAreaId(final AreaId areaId) {
        //        { bwem_assert( (areaId > 0) && ((id >= 1) || (id <= -2)) && (id != this.areaId));
        // this.areaId = id; }
        //        if (!( (areaId.intValue() > 0) && ((id.intValue() >= 1) || (id.intValue() <= -2)) &&
        // (!id.equals(areaId)))) {
        if (!(this.areaId.intValue() > 0)) {
            asserter.throwIllegalStateException(
                    "Failed assert: this.areaId.intValue() > 0: " + this.areaId.intValue());
        } else if (!((areaId.intValue() >= 1) || (areaId.intValue() <= -2))) {
            asserter.throwIllegalStateException(
                    "Failed assert: (id.intValue() >= 1) || (id.intValue() <= -2): " + areaId.intValue());
        } else if (areaId.equals(this.areaId)) {
            asserter.throwIllegalStateException(
                    "Failed assert: !id.equals (areaId): not expected: "
                            + this.areaId.intValue()
                            + ", actual: "
                            + areaId.intValue());
        } else {
            this.areaId = areaId;
        }
    }

    void setBlocked() {
        //        { bwem_assert(AreaIdMissing()); this.areaId = blockingCP; }
        if (!isAreaIdMissing()) {
            asserter.throwIllegalStateException("");
        }
        this.areaId = MiniTile.blockingCP;
    }

    boolean isBlocked() {
        return this.areaId.equals(MiniTile.blockingCP);
    }

    void replaceBlockedAreaId(final AreaId areaId) {
        //        { bwem_assert( (areaId == blockingCP) && (id >= 1)); this.areaId = id; }
        if (!(this.areaId.equals(MiniTile.blockingCP) && areaId.intValue() >= 1)) {
            asserter.throwIllegalStateException("");
        }
        this.areaId = areaId;
    }
}
