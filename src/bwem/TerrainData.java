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

import bwem.util.CheckMode;
import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.WalkPosition;

import java.util.ArrayList;
import java.util.List;

public final class TerrainData {
    private final MapData mapData;
    private final TileData tileData;

    TerrainData(final MapData mapData, final TileData tileData) {
        this.mapData = mapData;
        this.tileData = tileData;
    }

    public MapData getMapData() {
        return this.mapData;
    }

    public TileData getTileData() {
        return this.tileData;
    }

    public Tile getTile(final TilePosition tilePosition, final CheckMode checkMode) {
        if (!((checkMode == CheckMode.NO_CHECK) || getMapData().isValid(tilePosition))) {
            tileData.asserter.throwIllegalStateException("");
        }
        return getTileData()
                .getTile(getMapData().getTileSize().getX() * tilePosition.getY() + tilePosition.getX());
    }

    public Tile getTile(final TilePosition tilePosition) {
        return getTile(tilePosition, CheckMode.CHECK);
    }

    public MiniTile getMiniTile(final WalkPosition walkPosition, final CheckMode checkMode) {
        if (!((checkMode == CheckMode.NO_CHECK) || getMapData().isValid(walkPosition))) {
            tileData.asserter.throwIllegalStateException("");
        }
        return getTileData()
                .getMiniTile(getMapData().getWalkSize().getX() * walkPosition.getY() + walkPosition.getX());
    }

    public MiniTile getMiniTile(final WalkPosition walkPosition) {
        return getMiniTile(walkPosition, CheckMode.CHECK);
    }

    boolean isSeaWithNonSeaNeighbors(final WalkPosition walkPosition) {
        if (!getMiniTile(walkPosition).isSea()) {
            return false;
        }

        final WalkPosition[] deltas = {
                new WalkPosition(0, -1),
                new WalkPosition(-1, 0),
                new WalkPosition(1, 0),
                new WalkPosition(0, 1)
        };
        for (final WalkPosition delta : deltas) {
            final WalkPosition walkPositionDelta = walkPosition.add(delta);
            if (getMapData().isValid(walkPositionDelta) && !getMiniTile(walkPositionDelta,
                CheckMode.NO_CHECK).isSea()) {
                return true;
            }
        }

        return false;
    }

    void markUnwalkableMiniTiles(final BW game) {
        // Mark unwalkable minitiles (minitiles are walkable by default).
        for (int y = 0; y < getMapData().getWalkSize().getY(); ++y)
            for (int x = 0; x < getMapData().getWalkSize().getX(); ++x) {
                if (!game.getBWMap().isWalkable(x, y)) {
                    // For each unwalkable minitile, we also mark its 8 neighbors as not walkable.
                    // According to some tests, this prevents from wrongly pretending one marine can go by
                    // some thin path.
                    for (int dy = -1; dy <= 1; ++dy)
                        for (int dx = -1; dx <= 1; ++dx) {
                            final WalkPosition walkPosition = new WalkPosition(x + dx, y + dy);
                            if (getMapData().isValid(walkPosition)) {
                                getMiniTile(walkPosition, CheckMode.NO_CHECK).setWalkable(false);
                            }
                        }
                }
            }
    }

    void markBuildableTilesAndGroundHeight(final BW game) {
        // Mark buildable tiles (tiles are unbuildable by default).
        for (int y = 0; y < getMapData().getTileSize().getY(); ++y)
            for (int x = 0; x < getMapData().getTileSize().getX(); ++x) {
                final TilePosition tilePosition = new TilePosition(x, y);
                final WalkPosition walkPosition = tilePosition.toWalkPosition();
                final Tile tile = getTile(tilePosition);

                if (game.getBWMap().isBuildable(tilePosition, false)) {
                    tile.setBuildable();

                    // Ensures buildable ==> walkable.
                    for (int dy = 0; dy < 4; ++dy)
                        for (int dx = 0; dx < 4; ++dx) {
                            getMiniTile(walkPosition.add(new WalkPosition(dx, dy)), CheckMode.NO_CHECK)
                                    .setWalkable(true);
                        }
                }

                // Add ground height and doodad information.
                final int bwapiGroundHeight = game.getBWMap().getGroundHeight(tilePosition);
                tile.setGroundHeight(bwapiGroundHeight / 2);
                if (bwapiGroundHeight % 2 != 0) {
                    tile.setDoodad();
                }
            }
    }

    void decideSeasOrLakes() {
        for (int y = 0; y < getMapData().getWalkSize().getY(); ++y)
            for (int x = 0; x < getMapData().getWalkSize().getX(); ++x) {
                final WalkPosition originWalkPosition = new WalkPosition(x, y);
                final MiniTile originMiniTile = getMiniTile(originWalkPosition, CheckMode.NO_CHECK);

                if (originMiniTile.isSeaOrLake()) {
                    final List<WalkPosition> toSearch = new ArrayList<>();
                    toSearch.add(originWalkPosition);

                    final List<MiniTile> seaExtent = new ArrayList<>();
                    originMiniTile.setSea();
                    seaExtent.add(originMiniTile);

                    int topLeftX = originWalkPosition.getX();
                    int topLeftY = originWalkPosition.getY();
                    int bottomRightX = originWalkPosition.getX();
                    int bottomRightY = originWalkPosition.getY();

                    while (!toSearch.isEmpty()) {
                        final WalkPosition current = toSearch.remove(toSearch.size() - 1);
                        if (current.getX() < topLeftX) topLeftX = current.getX();
                        if (current.getY() < topLeftY) topLeftY = current.getY();
                        if (current.getX() > bottomRightX) bottomRightX = current.getX();
                        if (current.getY() > bottomRightY) bottomRightY = current.getY();

                        final WalkPosition[] deltas = {
                                new WalkPosition(0, -1),
                                new WalkPosition(-1, 0),
                                new WalkPosition(1, 0),
                                new WalkPosition(0, 1)
                        };
                        for (final WalkPosition delta : deltas) {
                            final WalkPosition nextWalkPosition = current.add(delta);
                            if (getMapData().isValid(nextWalkPosition)) {
                                final MiniTile nextMiniTile = getMiniTile(nextWalkPosition, CheckMode.NO_CHECK);
                                if (nextMiniTile.isSeaOrLake()) {
                                    toSearch.add(nextWalkPosition);
                                    if (seaExtent.size() <= bwem.util.BwemExt.LAKE_MAX_MINI_TILES) {
                                        seaExtent.add(nextMiniTile);
                                    }
                                    nextMiniTile.setSea();
                                }
                            }
                        }
                    }

                    if ((seaExtent.size() <= bwem.util.BwemExt.LAKE_MAX_MINI_TILES)
                            && (bottomRightX - topLeftX <= bwem.util.BwemExt.LAKE_MAX_WIDTH_IN_MINI_TILES)
                            && (bottomRightY - topLeftY <= bwem.util.BwemExt.LAKE_MAX_WIDTH_IN_MINI_TILES)
                            && (topLeftX >= 2)
                            && (topLeftY >= 2)
                            && (bottomRightX < getMapData().getWalkSize().getX() - 2)
                            && (bottomRightY < getMapData().getWalkSize().getY() - 2)) {
                        for (final MiniTile miniTile : seaExtent) {
                            miniTile.setLake();
                        }
                    }
                }
            }
    }
}
