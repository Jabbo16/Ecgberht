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

import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.WalkPosition;

import java.util.ArrayList;
import java.util.List;

public final class MapData {
    private final TilePosition tileSize;
    private final WalkPosition walkSize;
    private final Position pixelSize;
    private final Position center;
    private final List<TilePosition> startingLocations;

    MapData(
            final int tileWidth, final int tileHeight, final List<TilePosition> startingLocations) {

        this.tileSize = new TilePosition(tileWidth, tileHeight);
        this.walkSize = this.tileSize.toWalkPosition();
        this.pixelSize = this.tileSize.toPosition();

        this.center = new Position(this.pixelSize.getX() / 2, this.pixelSize.getY() / 2);

        this.startingLocations = new ArrayList<>(startingLocations);
    }

    public TilePosition getTileSize() {
        return this.tileSize;
    }

    public WalkPosition getWalkSize() {
        return this.walkSize;
    }

    public Position getPixelSize() {
        return this.pixelSize;
    }

    public Position getCenter() {
        return this.center;
    }

    public List<TilePosition> getStartingLocations() {
        return this.startingLocations;
    }

    public boolean isValid(final TilePosition tilePosition) {
        return isValid(
                tilePosition.getX(), tilePosition.getY(), getTileSize().getX(), getTileSize().getY());
    }

    public boolean isValid(final WalkPosition walkPosition) {
        return isValid(
                walkPosition.getX(), walkPosition.getY(), getWalkSize().getX(), getWalkSize().getY());
    }

    public boolean isValid(final Position position) {
        return isValid(position.getX(), position.getY(), getPixelSize().getX(), getPixelSize().getY());
    }

    private boolean isValid(final int x, final int y, final int maxX, final int maxY) {
        return (x >= 0 && x < maxX && y >= 0 && y < maxY);
    }
}
