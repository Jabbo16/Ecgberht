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

import bwem.util.Markable;
import bwem.util.StaticMarkable;
import org.openbw.bwapi4j.TilePosition;

/**
 * Corresponds to BWAPI/Starcraft's concept of tile (32x32 pixels).<br>
 * - Tiles are accessed using TilePositions (Cf. {@link TerrainData#getTile(TilePosition)}).<br>
 * - A BWMap holds BWMap::Size().x * BWMap::Size().y Tiles as its "Tile map".<br>
 * <br>
 * - It should be noted that a Tile exactly overlaps 4 x 4 MiniTiles.<br>
 * - As there are 16 times as many MiniTiles as Tiles, we allow a Tiles to contain more data than
 * MiniTiles.<br>
 * - As a consequence, Tiles should be preferred over MiniTiles, for efficiency.<br>
 * - The use of Tiles is further facilitated by some functions like Tile::AreaId or
 * Tile::LowestAltitude<br>
 * which somewhat aggregate the MiniTile's corresponding information
 */
public final class Tile {
    private static final StaticMarkable staticMarkable = new StaticMarkable();
    private final Markable markable;

    private Neutral neutral;
    private Altitude lowestAltitude;
    private AreaId areaId;
    private int internalData;
    private GroundHeight groundHeight;
    private boolean isBuildable;
    private boolean isDoodad;

    private final Asserter asserter;

    Tile(final Asserter asserter) {
        this.markable = new Markable(Tile.staticMarkable);
        this.neutral = null;
        this.lowestAltitude = Altitude.ZERO;
        this.areaId = AreaId.ZERO;
        this.internalData = 0;
        this.groundHeight = GroundHeight.LOW_GROUND;
        this.isBuildable = false;
        this.isDoodad = false;
        this.asserter = asserter;
    }

    static StaticMarkable getStaticMarkable() {
        return Tile.staticMarkable;
    }

    Markable getMarkable() {
        return this.markable;
    }

    /**
     * BWEM enforces the relation buildable ==> walkable (Cf. {@link MiniTile#isWalkable()})<br>
     */
    public boolean isBuildable() {
        return this.isBuildable;
    }

    /**
     * This function somewhat aggregates the MiniTile::getAreaId() values of the 4 x 4 sub-miniTiles.
     * <br>
     * - Let S be the set of MiniTile::AreaId() values for each walkable MiniTile in this Tile.<br>
     * - If empty(S), returns 0. Note: in this case, no contained MiniTile is walkable, so all of them
     * have their AreaId() == 0.<br>
     * - If S = {a}, returns a (whether positive or negative).<br>
     * - If size(S) > 1 returns -1 (note that -1 is never returned by MiniTile::AreaId()).
     */
    public AreaId getAreaId() {
        return this.areaId;
    }

    void setAreaId(final AreaId areaId) {
        if (!(areaId.intValue() == -1 || getAreaId().intValue() == 0 && areaId.intValue() != 0)) {
            asserter.throwIllegalStateException("");
        }
        this.areaId = areaId;
    }

    /**
     * Tile::LowestAltitude() somewhat aggregates the MiniTile::Altitude() values of the 4 x 4
     * sub-miniTiles.<br>
     * - Returns the minimum value.
     */
    public Altitude getLowestAltitude() {
        return this.lowestAltitude;
    }

    void setLowestAltitude(final Altitude lowestAltitude) {
        if (!(lowestAltitude.intValue() >= 0)) {
            asserter.throwIllegalStateException("");
        }
        this.lowestAltitude = lowestAltitude;
    }

    /**
     * Tells if at least one of the sub-miniTiles is Walkable.
     */
    public boolean isWalkable() {
        return (getAreaId().intValue() != 0);
    }

    /**
     * Tells if at least one of the sub-miniTiles is a Terrain-MiniTile.
     */
    public boolean isTerrain() {
        return isWalkable();
    }

    /**
     * Corresponds to BWAPI::getGroundHeight / 2
     */
    public GroundHeight getGroundHeight() {
        return this.groundHeight;
    }

    void setGroundHeight(final int groundHeight) {
        //        { bwem_assert((0 <= h) && (h <= 2)); bits.groundHeight = h; }
        //        if (!((0 <= h) && (h <= 2))) {
        //            throw new IllegalArgumentException();
        //        }
        this.groundHeight = GroundHeight.parseGroundHeight(groundHeight);
    }

    /**
     * Tells if this Tile is part of a doodad. Corresponds to BWAPI::getGroundHeight % 2
     */
    public boolean isDoodad() {
        return this.isDoodad;
    }

    /**
     * If any Neutral occupies this Tile, returns it (note that all the Tiles it occupies will then
     * return it).<br>
     * Otherwise, returns nullptr.<br>
     * - Neutrals are minerals, geysers and StaticBuildings (Cf. Neutral).<br>
     * - In some maps (e.g. Benzene.scx), several Neutrals are stacked at the same location.<br>
     * In this case, only the "bottom" one is returned, while the other ones can be accessed using
     * Neutral::nextStacked().<br>
     * - Because Neutrals never move on the BWMap, the returned value is guaranteed to remain the same,
     * unless some Neutral<br>
     * is destroyed and BWEM is informed of that by a call of BWMap::onMineralDestroyed(BWAPI::unit u)
     * for exemple. In such a case,<br>
     * - BWEM automatically updates the data by deleting the Neutral instance and clearing any
     * reference to it such as the one<br>
     * returned by Tile::GetNeutral(). In case of stacked Neutrals, the next one is then returned.
     */
    public Neutral getNeutral() {
        return this.neutral;
    }

    /**
     * Returns the number of Neutrals that occupy this Tile (Cf. {@link #getNeutral()}).
     */
    public int getStackedNeutralCount() {
        int stackSize = 0;
        for (Neutral stackedNeutral = getNeutral();
             stackedNeutral != null;
             stackedNeutral = stackedNeutral.getNextStacked()) {
            ++stackSize;
        }
        return stackSize;
    }

    void setBuildable() {
        this.isBuildable = true;
    }

    void setDoodad() {
        this.isDoodad = true;
    }

    void addNeutral(final Neutral neutral) {
        if (!(getNeutral() == null && neutral != null)) {
            asserter.throwIllegalStateException("");
        }
        this.neutral = neutral;
    }

    void resetAreaId() {
        this.areaId = AreaId.ZERO;
    }

    void removeNeutral(final Neutral neutral) {
        if (!getNeutral().equals(neutral)) {
            asserter.throwIllegalStateException("");
        }
        this.neutral = null;
    }

    int getInternalData() {
        return this.internalData;
    }

    void setInternalData(int internalData) {
        this.internalData = internalData;
    }

    /**
     * Corresponds to BWAPI::getGroundHeight divided by 2.
     */
    public enum GroundHeight {
        LOW_GROUND, // Height 0
        HIGH_GROUND, // Height 1
        VERY_HIGH_GROUND; // Height 2

        static GroundHeight parseGroundHeight(int height) {
            return values()[height];
        }
    }
}
