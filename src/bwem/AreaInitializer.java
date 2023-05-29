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

import bwem.util.*;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.WalkPosition;
import org.openbw.bwapi4j.type.UnitType;

import java.util.*;

import static bwem.AreaId.UNINITIALIZED;

final class AreaInitializer extends Area {

    private static final StaticMarkable staticMarkable = new StaticMarkable();
    private final Markable markable;

    AreaInitializer(
            final BWMap map, final AreaId areaId, final WalkPosition top, final int miniTileCount) {
        super(areaId, top, miniTileCount, map);

        this.markable = new Markable(staticMarkable);

        if (!(areaId.intValue() > 0)) {
            map.asserter.throwIllegalStateException("");
        }

        final MiniTile topMiniTile = this.map.getData().getMiniTile(top);
        if (!(topMiniTile.getAreaId().equals(areaId))) {
            map.asserter.throwIllegalStateException(
                    "assert failed: topMiniTile.AreaId().equals(areaId): expected: "
                            + topMiniTile.getAreaId().intValue()
                            + ", actual: "
                            + areaId.intValue());
        }

        super.highestAltitude = topMiniTile.getAltitude();
    }

    static StaticMarkable getStaticMarkable() {
        return staticMarkable;
    }

    Markable getMarkable() {
        return this.markable;
    }

    void addChokePoints(final Area area, final List<ChokePoint> chokePoints) {
        if (!(super.chokePointsByArea.get(area) == null && chokePoints != null)) {
            map.asserter.throwIllegalStateException("");
        }

        super.chokePointsByArea.put(area, chokePoints);

        super.chokePoints.addAll(chokePoints);
    }

    void addMineral(final Mineral mineral) {
        if (!(mineral != null && !super.minerals.contains(mineral))) {
            map.asserter.throwIllegalStateException("");
        }
        super.minerals.add(mineral);
    }

    void addGeyser(final Geyser geyser) {
        if (!(geyser != null && !super.geysers.contains(geyser))) {
            map.asserter.throwIllegalStateException("");
        }
        super.geysers.add(geyser);
    }

    void addTileInformation(final TilePosition tilePosition, final Tile tile) {
        ++super.tileCount;

        if (tile.isBuildable()) {
            ++super.buildableTileCount;
        }

        if (tile.getGroundHeight() == Tile.GroundHeight.HIGH_GROUND) {
            ++super.highGroundTileCount;
        } else if (tile.getGroundHeight() == Tile.GroundHeight.VERY_HIGH_GROUND) {
            ++super.veryHighGroundTileCount;
        }

        if (tilePosition.getX() < super.topLeft.getX()) {
            super.topLeft = new TilePosition(tilePosition.getX(), super.topLeft.getY());
        }
        if (tilePosition.getY() < super.topLeft.getY()) {
            super.topLeft = new TilePosition(super.topLeft.getX(), tilePosition.getY());
        }
        if (tilePosition.getX() > super.bottomRight.getX()) {
            super.bottomRight = new TilePosition(tilePosition.getX(), super.bottomRight.getY());
        }
        if (tilePosition.getY() > super.bottomRight.getY()) {
            super.bottomRight = new TilePosition(super.bottomRight.getX(), tilePosition.getY());
        }
    }

    void setGroupId(int gid) {
        super.groupId = gid;
    }

    int[] computeDistances(final ChokePoint startCP, final List<ChokePoint> targetCPs) {
        if (targetCPs.contains(startCP)) {
            map.asserter.throwIllegalStateException("");
        }

        final TilePosition start =
                this.map
                        .breadthFirstSearch(
                                startCP.getNodePositionInArea(ChokePoint.Node.MIDDLE, this).toTilePosition(),
                                // findCond
                                (Tile tile, TilePosition unused) -> tile.getAreaId().equals(getId()),
                                // visitCond
                                (Tile tile, TilePosition unused) -> true);

        final List<TilePosition> targets = new ArrayList<>();
        for (final ChokePoint cp : targetCPs) {
            final TilePosition t =
                    this.map
                            .breadthFirstSearch(
                                    cp.getNodePositionInArea(ChokePoint.Node.MIDDLE, this).toTilePosition(),
                                    // findCond
                                    (Tile tile, TilePosition position) -> (tile.getAreaId().equals(getId())),
                                    // visitCond
                                    (Tile tile, TilePosition unused) -> true);
            targets.add(t);
        }

        return computeDistances(start, targets);
    }

    private int[] computeDistances(final TilePosition start, final List<TilePosition> targets) {
        final int[] distances = new int[targets.size()];

        Tile.getStaticMarkable().unmarkAll();

        final Queue<Pair<Integer, TilePosition>> toVisit =
                new PriorityQueue<>(
                        Comparator.comparingInt(
                                Pair::getLeft)); // a priority queue holding the tiles to visit ordered by their
        // distance to start.
        toVisit.offer(new Pair<>(0, start));

        int remainingTargets = targets.size();
        while (!toVisit.isEmpty()) {
            final Pair<Integer, TilePosition> distanceAndTilePosition = toVisit.poll();
            final int currentDist = distanceAndTilePosition.getLeft();
            final TilePosition current = distanceAndTilePosition.getRight();
            final Tile currentTile = this.map.getData().getTile(current, CheckMode.NO_CHECK);
            if (!(currentTile.getInternalData() == currentDist)) {
                map.asserter.throwIllegalStateException(
                        "currentTile.InternalData().intValue()="
                                + currentTile.getInternalData()
                                + ", currentDist="
                                + currentDist);
            }
            currentTile
                    .setInternalData(0); // resets Tile::m_internalData for future usage
            currentTile.getMarkable().setMarked();

            for (int i = 0; i < targets.size(); ++i) {
                if (current.equals(targets.get(i))) {
                    distances[i] = (int) Math.round(currentDist * 32.0 / 10000.0);
                    --remainingTargets;
                }
            }
            if (remainingTargets == 0) {
                break;
            }

            final TilePosition[] deltas = {
                    new TilePosition(-1, -1),
                    new TilePosition(0, -1),
                    new TilePosition(+1, -1),
                    new TilePosition(-1, 0),
                    new TilePosition(+1, 0),
                    new TilePosition(-1, +1),
                    new TilePosition(0, +1),
                    new TilePosition(+1, +1)
            };
            for (final TilePosition delta : deltas) {
                final boolean diagonalMove = (delta.getX() != 0) && (delta.getY() != 0);
                final int newNextDist = currentDist + (diagonalMove ? 14142 : 10000);

                final TilePosition next = current.add(delta);
                if (this.map.getData().getMapData().isValid(next)) {
                    final Tile nextTile = this.map.getData().getTile(next, CheckMode.NO_CHECK);
                    if (nextTile.getMarkable().isUnmarked()) {
                        if (nextTile.getInternalData()
                                != 0) { // next already in toVisit
                            if (newNextDist
                                    < nextTile
                                    .getInternalData()) { // nextNewDist < nextOldDist
                                // To update next's distance, we need to remove-insert it from toVisit:
                                final boolean removed =
                                        toVisit.remove(
                                                new Pair<>(nextTile.getInternalData(), next));
                                if (!removed) {
                                    map.asserter.throwIllegalStateException("");
                                }
                                nextTile.setInternalData(newNextDist);
                                toVisit.offer(new Pair<>(newNextDist, next));
                            }
                        } else if ((nextTile.getAreaId().equals(getId()))
                                || (nextTile.getAreaId().equals(UNINITIALIZED))) {
                            nextTile.setInternalData(newNextDist);
                            toVisit.offer(new Pair<>(newNextDist, next));
                        }
                    }
                }
            }
        }

        if (!(remainingTargets == 0)) {
            map.asserter.throwIllegalStateException("");
        }

        for (final Pair<Integer, TilePosition> distanceAndTilePosition : toVisit) {
            final Tile tileToUpdate =
                    this.map.getData()
                            .getTile(distanceAndTilePosition.getRight(), CheckMode.NO_CHECK);
            tileToUpdate.setInternalData(0);
        }

        return distances;
    }

    void updateAccessibleNeighbors() {
        super.accessibleNeighbors.clear();
        for (final Area area : getChokePointsByArea().keySet()) {
            for (final ChokePoint cp : getChokePointsByArea().get(area)) {
                if (!cp.isBlocked()) {
                    super.accessibleNeighbors.add(area);
                    break;
                }
            }
        }
    }

    void createBases(final TerrainData terrainData) {
        final TilePosition resourceDepotDimensions = UnitType.Terran_Command_Center.tileSize();

        final List<Resource> remainingResources = new ArrayList<>();

        for (final Mineral mineral : getMinerals()) {
            if ((mineral.getInitialAmount() >= 40) && !mineral.isBlocking()) {
                remainingResources.add(mineral);
            }
        }

        for (final Geyser geyser : getGeysers()) {
            if ((geyser.getInitialAmount() >= 300) && !geyser.isBlocking()) {
                remainingResources.add(geyser);
            }
        }
        while (!remainingResources.isEmpty()) {
            // 1) Calculate the SearchBoundingBox (needless to search too far from the
            // remainingResources):
            TilePosition topLeftResources = new TilePosition(Integer.MAX_VALUE, Integer.MAX_VALUE);
            TilePosition bottomRightResources = new TilePosition(Integer.MIN_VALUE,
                    Integer.MIN_VALUE);
            for (final Resource r : remainingResources) {
                final Pair<TilePosition, TilePosition> pair1 =
                        BwemExt.makeBoundingBoxIncludePoint(
                                topLeftResources, bottomRightResources, r.getTopLeft());
                topLeftResources = pair1.getLeft();
                bottomRightResources = pair1.getRight();

                final Pair<TilePosition, TilePosition> pair2 =
                        BwemExt.makeBoundingBoxIncludePoint(
                                topLeftResources, bottomRightResources, r.getBottomRight());
                topLeftResources = pair2.getLeft();
                bottomRightResources = pair2.getRight();
            }

            final TilePosition dimensionsBetweenResourceDepotAndResources =
                    new TilePosition(
                            BwemExt.MAX_TILES_BETWEEN_COMMAND_CENTER_AND_RESOURCES,
                            BwemExt.MAX_TILES_BETWEEN_COMMAND_CENTER_AND_RESOURCES);
            TilePosition topLeftSearchBoundingBox =
                    topLeftResources
                            .subtract(resourceDepotDimensions)
                            .subtract(dimensionsBetweenResourceDepotAndResources);
            TilePosition bottomRightSearchBoundingBox =
                    bottomRightResources
                            .add(new TilePosition(1, 1))
                            .add(dimensionsBetweenResourceDepotAndResources);
            topLeftSearchBoundingBox =
                    BwemExt.makePointFitToBoundingBox(
                            topLeftSearchBoundingBox,
                            getTopLeft(),
                            getBottomRight().subtract(resourceDepotDimensions).add(new TilePosition(1, 1)));
            bottomRightSearchBoundingBox =
                    BwemExt.makePointFitToBoundingBox(
                            bottomRightSearchBoundingBox,
                            getTopLeft(),
                            getBottomRight().subtract(resourceDepotDimensions).add(new TilePosition(1, 1)));

            // 2) Mark the Tiles with their distances from each remaining Resource (Potential Fields >= 0)
            for (final Resource r : remainingResources) {
                for (int dy =
                     -resourceDepotDimensions.getY()
                             - BwemExt.MAX_TILES_BETWEEN_COMMAND_CENTER_AND_RESOURCES;
                     dy
                             < r.getSize().getY()
                             + resourceDepotDimensions.getY()
                             + BwemExt.MAX_TILES_BETWEEN_COMMAND_CENTER_AND_RESOURCES;
                     ++dy) {
                    for (int dx =
                         -resourceDepotDimensions.getX()
                                 - BwemExt.MAX_TILES_BETWEEN_COMMAND_CENTER_AND_RESOURCES;
                         dx
                                 < r.getSize().getX()
                                 + resourceDepotDimensions.getX()
                                 + BwemExt.MAX_TILES_BETWEEN_COMMAND_CENTER_AND_RESOURCES;
                         ++dx) {
                        final TilePosition deltaTilePosition = r.getTopLeft()
                                .add(new TilePosition(dx, dy));
                        if (terrainData.getMapData().isValid(deltaTilePosition)) {
                            final Tile tile = terrainData
                                    .getTile(deltaTilePosition, CheckMode.NO_CHECK);
                            int dist =
                                    (BwemExt.distToRectangle(
                                            BwemExt.center(deltaTilePosition),
                                            r.getTopLeft().toPosition(),
                                            r.getSize().toPosition())
                                            + (TilePosition.SIZE_IN_PIXELS / 2))
                                            / TilePosition.SIZE_IN_PIXELS;
                            int score =
                                    Math.max(BwemExt.MAX_TILES_BETWEEN_COMMAND_CENTER_AND_RESOURCES + 3
                                            - dist, 0);
                            if (r instanceof Geyser) {
                                // somewhat compensates for Geyser alone vs the several minerals
                                score *= 3;
                            }
                            if (tile.getAreaId().equals(getId())) {
                                // note the additive effect (assume tile.InternalData() is 0 at the beginning)
                                tile
                                        .setInternalData(tile.getInternalData() + score);
                            }
                        }
                    }
                }
            }

            // 3) Invalidate the 7 x 7 Tiles around each remaining Resource (Starcraft rule)
            for (final Resource r : remainingResources) {
                for (int dy = -3; dy < r.getSize().getY() + 3; ++dy) {
                    for (int dx = -3; dx < r.getSize().getX() + 3; ++dx) {
                        final TilePosition deltaTilePosition = r.getTopLeft()
                                .add(new TilePosition(dx, dy));
                        if (terrainData.getMapData().isValid(deltaTilePosition)) {
                            final Tile tileToUpdate = terrainData
                                    .getTile(deltaTilePosition, CheckMode.NO_CHECK);
                            tileToUpdate.setInternalData(-1);
                        }
                    }
                }
            }

            // 4) Search the best location inside the SearchBoundingBox:
            TilePosition bestLocation = null;
            int bestScore = 0;
            final List<Mineral> blockingMinerals = new ArrayList<>();

            for (int y = topLeftSearchBoundingBox.getY(); y <= bottomRightSearchBoundingBox.getY();
                 ++y) {
                for (int x = topLeftSearchBoundingBox.getX();
                     x <= bottomRightSearchBoundingBox.getX();
                     ++x) {
                    final int score = computeBaseLocationScore(terrainData, new TilePosition(x, y));
                    if (score > bestScore && validateBaseLocation(terrainData,
                            new TilePosition(x, y),
                            blockingMinerals)) {
                        bestScore = score;
                        bestLocation = new TilePosition(x, y);
                    }
                }
            }

            // 5) Clear Tile::m_internalData (required due to our use of Potential Fields: see comments in
            // 2))
            for (Resource r : remainingResources) {
                for (int dy =
                     -resourceDepotDimensions.getY()
                             - BwemExt.MAX_TILES_BETWEEN_COMMAND_CENTER_AND_RESOURCES;
                     dy
                             < r.getSize().getY()
                             + resourceDepotDimensions.getY()
                             + BwemExt.MAX_TILES_BETWEEN_COMMAND_CENTER_AND_RESOURCES;
                     ++dy) {
                    for (int dx =
                         -resourceDepotDimensions.getX()
                                 - BwemExt.MAX_TILES_BETWEEN_COMMAND_CENTER_AND_RESOURCES;
                         dx
                                 < r.getSize().getX()
                                 + resourceDepotDimensions.getX()
                                 + BwemExt.MAX_TILES_BETWEEN_COMMAND_CENTER_AND_RESOURCES;
                         ++dx) {
                        final TilePosition deltaTilePosition = r.getTopLeft()
                                .add(new TilePosition(dx, dy));
                        if (terrainData.getMapData().isValid(deltaTilePosition)) {
                            final Tile tileToUpdate = terrainData
                                    .getTile(deltaTilePosition, CheckMode.NO_CHECK);
                            tileToUpdate.setInternalData(0);
                        }
                    }
                }
            }

            if (bestScore == 0) {
                break;
            }

            // 6) Create a new Base at bestLocation, assign to it the relevant resources and remove them
            // from RemainingResources:
            final List<Resource> assignedResources = new ArrayList<>();
            for (final Resource r : remainingResources) {
                if (BwemExt.distToRectangle(
                        r.getCenter(), bestLocation.toPosition(), resourceDepotDimensions.toPosition())
                        + 2
                        <= BwemExt.MAX_TILES_BETWEEN_COMMAND_CENTER_AND_RESOURCES
                        * TilePosition.SIZE_IN_PIXELS) {
                    assignedResources.add(r);
                }
            }

            remainingResources.removeIf(assignedResources::contains);

            if (assignedResources.isEmpty()) {
                break;
            }

            super.bases.add(new Base(this, bestLocation, assignedResources, blockingMinerals, map.asserter));
        }
    }

    private boolean isInvalidTile(Tile tile) {
        return !tile.isBuildable()
                || tile.getInternalData() == -1
                || !tile.getAreaId().equals(getId())
                || tile.getNeutral() instanceof StaticBuilding;
    }

    private int computeBaseLocationScore(final TerrainData terrainData,
                                         final TilePosition location) {
        final TilePosition dimCC = UnitType.Terran_Command_Center.tileSize();

        int sumScore = 0;
        for (int dy = 0; dy < dimCC.getY(); ++dy) {
            for (int dx = 0; dx < dimCC.getX(); ++dx) {
                final Tile tile =
                        terrainData.getTile(location.add(new TilePosition(dx, dy)), CheckMode.NO_CHECK);
                if (isInvalidTile(tile)) {
                    return -1;
                }

                sumScore += tile.getInternalData();
            }
        }

        return sumScore;
    }

    private boolean validateBaseLocation(
            final TerrainData terrainData,
            final TilePosition location,
            final List<Mineral> blockingMinerals) {
        final TilePosition dimCC = UnitType.Terran_Command_Center.tileSize();

        blockingMinerals.clear();

        for (int dy = -3; dy < dimCC.getY() + 3; ++dy) {
            for (int dx = -3; dx < dimCC.getX() + 3; ++dx) {
                final TilePosition deltaLocation = location.add(new TilePosition(dx, dy));
                if (terrainData.getMapData().isValid(deltaLocation)) {
                    final Tile deltaTile = terrainData.getTile(deltaLocation, CheckMode.NO_CHECK);
                    if (isTileInvalidForBase(blockingMinerals, deltaTile)) return false;
                }
            }
        }

        if (isBaseTooClose(location)) return false;

        return true;
    }

    private boolean isBaseTooClose(TilePosition location) {
        for (final Base base : getBases()) {
            if (BwemExt.roundedDist(base.getLocation(), location)
                    < BwemExt.MIN_TILES_BETWEEN_BASES) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTileInvalidForBase(List<Mineral> blockingMinerals, Tile deltaTile) {
        final Neutral deltaTileNeutral = deltaTile.getNeutral();
        if (deltaTileNeutral != null) {
            if (deltaTileNeutral instanceof Geyser) {
                return true;
            } else if (deltaTileNeutral instanceof Mineral) {
                final Mineral deltaTileMineral = (Mineral) deltaTileNeutral;
                if (deltaTileMineral.getInitialAmount() <= 8) {
                    blockingMinerals.add(deltaTileMineral);
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public void onMineralDestroyed(final Mineral mineral) {
        if (mineral == null) {
            map.asserter.throwIllegalStateException("");
        }

        this.minerals.remove(mineral);

        // let's examine the bases even if mineral was not found in this Area,
        // which could arise if minerals were allowed to be assigned to neighboring areas.
        for (final Base base : getBases()) {
            base.onMineralDestroyed(mineral);
        }
    }
}
