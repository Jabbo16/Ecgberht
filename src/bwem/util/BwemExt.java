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

package bwem.util;

import bwem.BWMap;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.WalkPosition;

import java.util.ArrayList;
import java.util.List;

public final class BwemExt {
    // These constants control how to decide between Seas and Lakes.
    public static final int LAKE_MAX_MINI_TILES = 300;
    public static final int LAKE_MAX_WIDTH_IN_MINI_TILES = 8 * 4;
    // At least AREA_MIN_MINI_TILES connected MiniTiles are necessary for an Area to be created.
    public static final int AREA_MIN_MINI_TILES = 64;
    public static final int MAX_TILES_BETWEEN_COMMAND_CENTER_AND_RESOURCES = 10;
    public static final int MIN_TILES_BETWEEN_BASES = 10;
    public static final int MAX_TILES_BETWEEN_STARTING_LOCATION_AND_ITS_ASSIGNED_BASE = 3;
    private static final int TILE_POSITION_CENTER_OFFSET_IN_PIXELS = TilePosition.SIZE_IN_PIXELS / 2;
    private static final Position TILE_POSITION_CENTER_IN_PIXELS =
            new Position(
                    BwemExt.TILE_POSITION_CENTER_OFFSET_IN_PIXELS,
                    BwemExt.TILE_POSITION_CENTER_OFFSET_IN_PIXELS);
    private static final int WALK_POSITION_CENTER_OFFSET_IN_PIXELS = WalkPosition.SIZE_IN_PIXELS / 2;
    private static final Position WALK_POSITION_CENTER_IN_PIXELS =
            new Position(
                    BwemExt.WALK_POSITION_CENTER_OFFSET_IN_PIXELS,
                    BwemExt.WALK_POSITION_CENTER_OFFSET_IN_PIXELS);

    private BwemExt() {
    }

    public static Position center(final TilePosition tilePosition) {
        return tilePosition.toPosition().add(BwemExt.TILE_POSITION_CENTER_IN_PIXELS);
    }

    public static Position center(final WalkPosition walkPosition) {
        return walkPosition.toPosition().add(BwemExt.WALK_POSITION_CENTER_IN_PIXELS);
    }

    public static Position centerOfBuilding(
            final TilePosition tilePosition, final TilePosition buildingSize) {
        final Position pixelSize = buildingSize.toPosition();
        final Position pixelOffset = pixelSize.divide(new Position(2, 2));
        return tilePosition.toPosition().add(pixelOffset);
    }

    // Enlarges the bounding box [topLeft, bottomRight] so that it includes A.
    public static Pair<TilePosition, TilePosition> makeBoundingBoxIncludePoint(
            final TilePosition topLeft, final TilePosition bottomRight, final TilePosition point) {
        int topLeftX = topLeft.getX();
        int topLeftY = topLeft.getY();

        int bottomRightX = bottomRight.getX();
        int bottomRightY = bottomRight.getY();

        if (point.getX() < topLeftX) topLeftX = point.getX();
        if (point.getX() > bottomRightX) bottomRightX = point.getX();

        if (point.getY() < topLeftY) topLeftY = point.getY();
        if (point.getY() > bottomRightY) bottomRightY = point.getY();

        return new Pair<>(
                new TilePosition(topLeftX, topLeftY), new TilePosition(bottomRightX, bottomRightY));
    }

    // Makes the smallest change to A so that it is included in the bounding box [topLeft,
    // bottomRight].
    public static TilePosition makePointFitToBoundingBox(
            final TilePosition point, final TilePosition topLeft, final TilePosition bottomRight) {
        int pointX = point.getX();
        int pointY = point.getY();

        if (pointX < topLeft.getX()) pointX = topLeft.getX();
        else if (pointX > bottomRight.getX()) pointX = bottomRight.getX();

        if (pointY < topLeft.getY()) pointY = topLeft.getY();
        else if (pointY > bottomRight.getY()) pointY = bottomRight.getY();

        return new TilePosition(pointX, pointY);
    }

    // bwapiExt.h:71:inBoundingBox
    public static boolean isPointInBoundingBox(
            final TilePosition point, final TilePosition topLeft, final TilePosition bottomRight) {
        return (point.getX() >= topLeft.getX())
                && (point.getX() <= bottomRight.getX())
                && (point.getY() >= topLeft.getY())
                && (point.getY() <= bottomRight.getY());
    }

    public static int queenWiseDist(final TilePosition a, final TilePosition b) {
        final TilePosition ret = a.subtract(b);
        return Utils.queenWiseNorm(ret.getX(), ret.getY());
    }

    public static int queenWiseDist(final WalkPosition a, final WalkPosition b) {
        final WalkPosition ret = a.subtract(b);
        return Utils.queenWiseNorm(ret.getX(), ret.getY());
    }

    public static int queenWiseDist(final Position a, final Position b) {
        final Position ret = a.subtract(b);
        return Utils.queenWiseNorm(ret.getX(), ret.getY());
    }

    public static int squaredDist(final TilePosition a, final TilePosition b) {
        final TilePosition ret = a.subtract(b);
        return Utils.squaredNorm(ret.getX(), ret.getY());
    }

    public static int squaredDist(final WalkPosition a, final WalkPosition b) {
        final WalkPosition ret = a.subtract(b);
        return Utils.squaredNorm(ret.getX(), ret.getY());
    }

    public static int squaredDist(final Position a, final Position b) {
        final Position ret = a.subtract(b);
        return Utils.squaredNorm(ret.getX(), ret.getY());
    }

    public static double dist(final TilePosition a, final TilePosition b) {
        final TilePosition ret = a.subtract(b);
        return Utils.norm(ret.getX(), ret.getY());
    }

    private static double dist(final WalkPosition a, final WalkPosition b) {
        final WalkPosition ret = a.subtract(b);
        return Utils.norm(ret.getX(), ret.getY());
    }

    private static double dist(final Position a, final Position b) {
        final Position ret = a.subtract(b);
        return Utils.norm(ret.getX(), ret.getY());
    }

    public static int roundedDist(final TilePosition a, final TilePosition b) {
        return (int) Math.round(dist(a, b));
    }

    public static int roundedDist(final WalkPosition a, final WalkPosition b) {
        return (int) Math.round(dist(a, b));
    }

    private static int roundedDist(final Position a, final Position b) {
        return (int) Math.round(dist(a, b));
    }

    public static int distToRectangle(final Position a, final Position topLeft, final Position size) {
        final Position bottomRight = topLeft.add(size).subtract(new Position(1, 1));

        if (a.getX() >= topLeft.getX())
            if (a.getX() <= bottomRight.getX())
                if (a.getY() > bottomRight.getY()) return a.getY() - bottomRight.getY(); // S
                else if (a.getY() < topLeft.getY()) return topLeft.getY() - a.getY(); // N
                else return 0; // inside
            else if (a.getY() > bottomRight.getY()) return roundedDist(a, bottomRight); // SE
            else if (a.getY() < topLeft.getY())
                return roundedDist(a, new Position(bottomRight.getX(), topLeft.getY())); // NE
            else return a.getX() - bottomRight.getX(); // E
        else if (a.getY() > bottomRight.getY())
            return roundedDist(a, new Position(topLeft.getX(), bottomRight.getY())); // SW
        else if (a.getY() < topLeft.getY()) return roundedDist(a, topLeft); // NW
        else return topLeft.getX() - a.getX(); // W
    }

    private static List<Pair<Integer, Integer>> innerBorderDeltas(
            final int sizeX, final int sizeY, final boolean noCorner) {
        final List<Pair<Integer, Integer>> border = new ArrayList<>();

        for (int dy = 0; dy < sizeY; ++dy)
            for (int dx = 0; dx < sizeX; ++dx) {
                if ((dy == 0) || (dy == sizeY - 1) || (dx == 0) || (dx == sizeX - 1)) {
                    if (!noCorner
                            || !(((dx == 0) && (dy == 0))
                            || ((dx == sizeX - 1) && (dy == sizeY - 1))
                            || ((dx == 0) && (dy == sizeY - 1))
                            || ((dx == sizeX - 1) && (dy == 0)))) {
                        border.add(new Pair<>(dx, dy));
                    }
                }
            }

        return border;
    }

    private static List<TilePosition> innerBorder(
            final TilePosition topLeft, final TilePosition size, final boolean noCorner) {
        final List<TilePosition> border = new ArrayList<>();
        final List<Pair<Integer, Integer>> deltas =
                innerBorderDeltas(size.getX(), size.getY(), noCorner);
        for (final Pair<Integer, Integer> delta : deltas) {
            border.add(topLeft.add(new TilePosition(delta.getLeft(), delta.getRight())));
        }
        return border;
    }

    public static List<TilePosition> innerBorder(
            final TilePosition topLeft, final TilePosition size) {
        return innerBorder(topLeft, size, false);
    }

    private static List<WalkPosition> innerBorder(
            final WalkPosition topLeft, final WalkPosition size, boolean noCorner) {
        final List<WalkPosition> border = new ArrayList<>();
        final List<Pair<Integer, Integer>> deltas =
                innerBorderDeltas(size.getX(), size.getY(), noCorner);
        for (final Pair<Integer, Integer> delta : deltas) {
            border.add(topLeft.add(new WalkPosition(delta.getLeft(), delta.getRight())));
        }
        return border;
    }

    public static List<WalkPosition> innerBorder(
            final WalkPosition topLeft, final WalkPosition size) {
        return innerBorder(topLeft, size, false);
    }

    private static List<TilePosition> outerBorder(
            final TilePosition topLeft, final TilePosition size, final boolean noCorner) {
        return innerBorder(
                topLeft.subtract(new TilePosition(1, 1)), size.add(new TilePosition(2, 2)), noCorner);
    }

    public static List<TilePosition> outerBorder(
            final TilePosition topLeft, final TilePosition size) {
        return outerBorder(topLeft, size, false);
    }

    private static List<WalkPosition> outerBorder(
            final WalkPosition topLeft, final WalkPosition size, final boolean noCorner) {
        return innerBorder(
                topLeft.subtract(new WalkPosition(1, 1)), size.add(new WalkPosition(2, 2)), noCorner);
    }

    public static List<WalkPosition> outerBorder(
            final WalkPosition topLeft, final WalkPosition size) {
        return outerBorder(topLeft, size, false);
    }

    private static List<WalkPosition> outerMiniTileBorder(
            final TilePosition topLeft, final TilePosition size, final boolean noCorner) {
        return outerBorder(topLeft.toWalkPosition(), size.toWalkPosition(), noCorner);
    }

    public static List<WalkPosition> outerMiniTileBorder(
            final TilePosition topLeft, final TilePosition size) {
        return outerMiniTileBorder(topLeft, size, false);
    }

    private static List<WalkPosition> innerMiniTileBorder(
            final TilePosition topLeft, final TilePosition size, final boolean noCorner) {
        return innerBorder(topLeft.toWalkPosition(), size.toWalkPosition(), noCorner);
    }

    public static List<WalkPosition> innerMiniTileBorder(
            final TilePosition topLeft, TilePosition size) {
        return innerMiniTileBorder(topLeft, size, false);
    }

    public static boolean adjoins8SomeLakeOrNeutral(final WalkPosition p, final BWMap pMap) {
        final WalkPosition[] deltas = {
                new WalkPosition(-1, -1),
                new WalkPosition(0, -1),
                new WalkPosition(+1, -1),
                new WalkPosition(-1, 0),
                new WalkPosition(+1, 0),
                new WalkPosition(-1, +1),
                new WalkPosition(0, +1),
                new WalkPosition(+1, +1)
        };
        for (final WalkPosition delta : deltas) {
            final WalkPosition next = p.add(delta);
            if (pMap.getData().getMapData().isValid(next)) {
                if (pMap.getData().getTile(next.toTilePosition(), CheckMode.NO_CHECK).getNeutral()
                        != null) {
                    return true;
                }
                if (pMap.getData().getMiniTile(next, CheckMode.NO_CHECK).isLake()) {
                    return true;
                }
            }
        }

        return false;
    }
}
