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

final class TileData {

    private final Tile[] tiles;
    private final MiniTile[] miniTiles;

    final Asserter asserter;

    TileData(final int tileCount, final int miniTileCount, final Asserter asserter) {
        tiles = new Tile[tileCount];
        this.asserter = asserter;
        for (int i = 0; i < tileCount; ++i) {
            tiles[i] = new Tile(asserter);
        }

        miniTiles = new MiniTile[miniTileCount];
        for (int i = 0; i < miniTileCount; ++i) {
            miniTiles[i] = new MiniTile(asserter);
        }
    }

    Tile getTile(int index) {
        return tiles[index];
    }

    MiniTile getMiniTile(int index) {
        return miniTiles[index];
    }
}
