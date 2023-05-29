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
import bwem.util.Pair;
import bwem.util.Utils;
import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.WalkPosition;
import org.openbw.bwapi4j.unit.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class BWMapInitializer extends BWMap {
    BWMapInitializer(final BW game, final Asserter asserter) {
        super(game, asserter);
    }

    void initialize() {
        initializeTerrainData(
                game.getBWMap().mapWidth(), game.getBWMap().mapHeight(), game.getBWMap().getStartPositions());


        // Computes walkability, buildability and groundHeight and doodad information, using BWAPI
        // corresponding functions
        TerrainData initializer = getData();
        initializer.markUnwalkableMiniTiles(game);
        initializer.markBuildableTilesAndGroundHeight(game);

        //
        initializer.decideSeasOrLakes();


        initializeNeutralData(
                super.mineralPatches,
                super.vespeneGeysers,
                filterNeutralPlayerUnits(super.units, super.players));


        computeAltitude(getData());


        processBlockingNeutrals(
                getCandidates(getNeutralData().getStaticBuildings(), getNeutralData().getMinerals()));


        computeAreas(
                computeTempAreas(getSortedMiniTilesByDescendingAltitude()));


        getGraph()
                .createChokePoints(
                        getNeutralData().getStaticBuildings(),
                        getNeutralData().getMinerals(),
                        getRawFrontier());
        //


        getGraph().computeChokePointDistanceMatrix();

        getGraph().collectInformation();

        getGraph().createBases(getData());
    }

    private void initializeTerrainData(
            final int mapTileWidth, final int mapTileHeight, final List<TilePosition> startingLocations) {
        final MapData mapData = new MapData(mapTileWidth, mapTileHeight, startingLocations);
        final TileData tileData =
                new TileData(
                        mapData.getTileSize().getX() * mapData.getTileSize().getY(),
                        mapData.getWalkSize().getX() * mapData.getWalkSize().getY(),
                        asserter);
        super.terrainData = new TerrainData(mapData, tileData);
    }

    ////////////////////////////////////////////////////////////////////////
    // BWMap::InitializeNeutrals
    ////////////////////////////////////////////////////////////////////////

    private void initializeNeutralData(
            final List<Unit> mineralPatches,
            final List<Unit> vespeneGeysers,
            final List<Unit> neutralUnits) {
        super.neutralData = new NeutralData(this, mineralPatches, vespeneGeysers, neutralUnits);
    }

    ////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////
    // BWMap::ComputeAltitude
    ////////////////////////////////////////////////////////////////////////

    // Assigns MiniTile::m_altitude foar each miniTile having AltitudeMissing()
    // Cf. MiniTile::Altitude() for meaning of altitude_t.
    // Altitudes are computed using the straightforward Dijkstra's algorithm : the lower ones are
    // computed first, starting from the seaside-miniTiles neighbors.
    // The point here is to precompute all possible altitudes for all possible tiles, and sort them.
    private void computeAltitude(final TerrainData terrainData) {
        final int altitudeScale =
                8; // 8 provides a pixel definition for altitude_t, since altitudes are computed from
        // miniTiles which are 8x8 pixels

        final List<Pair<WalkPosition, Altitude>> deltasByAscendingAltitude =
                getSortedDeltasByAscendingAltitude(
                        terrainData.getMapData().getWalkSize().getX(),
                        terrainData.getMapData().getWalkSize().getY(),
                        altitudeScale);

        final List<Pair<WalkPosition, Altitude>> activeSeaSides =
                getActiveSeaSideList(terrainData);

        super.highestAltitude = setAltitudesAndGetUpdatedHighestAltitude(
                getHighestAltitude(),
                terrainData,
                deltasByAscendingAltitude,
                activeSeaSides,
                altitudeScale);
    }

    /**
     * 1) Fill in and sort DeltasByAscendingAltitude
     */
    private List<Pair<WalkPosition, Altitude>> getSortedDeltasByAscendingAltitude(
            final int mapWalkTileWidth, final int mapWalkTileHeight, int altitudeScale) {
        final int range =
                Math.max(mapWalkTileWidth, mapWalkTileHeight) / 2
                        + 3; // should suffice for maps with no Sea.

        final List<Pair<WalkPosition, Altitude>> deltasByAscendingAltitude = new ArrayList<>();

        for (int dy = 0; dy <= range; ++dy) {
            for (int dx = dy;
                 dx <= range;
                 ++dx) { // Only consider 1/8 of possible deltas. Other ones obtained by symmetry.
                if (dx != 0 || dy != 0) {
                    deltasByAscendingAltitude.add(
                            new Pair<>(
                                    new WalkPosition(dx, dy),
                                    new Altitude((int) Math.round(Utils.norm(dx, dy) * altitudeScale))));
                }
            }
        }

        deltasByAscendingAltitude.sort(Altitude.BY_ALTITUDE_ORDER);

        return deltasByAscendingAltitude;
    }

    /**
     * 2) Fill in ActiveSeaSideList, which basically contains all the seaside miniTiles (from which
     * altitudes are to be computed) It also includes extra border-miniTiles which are considered as
     * seaside miniTiles too.
     */
    private List<Pair<WalkPosition, Altitude>> getActiveSeaSideList(
            final TerrainData terrainData) {
        final List<Pair<WalkPosition, Altitude>> activeSeaSideList = new ArrayList<>();

        for (int y = -1; y <= terrainData.getMapData().getWalkSize().getY(); ++y) {
            for (int x = -1; x <= terrainData.getMapData().getWalkSize().getX(); ++x) {
                final WalkPosition walkPosition = new WalkPosition(x, y);
                if (!terrainData.getMapData().isValid(walkPosition)
                        || terrainData.isSeaWithNonSeaNeighbors(walkPosition)) {
                    activeSeaSideList.add(new Pair<>(walkPosition, Altitude.ZERO));
                }
            }
        }

        return activeSeaSideList;
    }

    // ----------------------------------------------------------------------
    // 3) Dijkstra's algorithm to set altitude for mini tiles.
    // ----------------------------------------------------------------------

    private Altitude setAltitudesAndGetUpdatedHighestAltitude(
            final Altitude currentHighestAltitude,
            final TerrainData terrainData,
            final List<Pair<WalkPosition, Altitude>> deltasByAscendingAltitude,
            final List<Pair<WalkPosition, Altitude>> activeSeaSideList,
            final int altitudeScale) {
        Altitude updatedHighestAltitude = currentHighestAltitude;

        for (final Pair<WalkPosition, Altitude> deltaAltitude : deltasByAscendingAltitude) {
            final WalkPosition d = deltaAltitude.getLeft();
            final Altitude altitude = deltaAltitude.getRight();

            for (int i = 0; i < activeSeaSideList.size(); ++i) {
                final Pair<WalkPosition, Altitude> current = activeSeaSideList.get(i);
                if (altitude.intValue() - current.getRight().intValue() >= 2 * altitudeScale) {
                    // optimization : once a seaside miniTile verifies this condition,
                    // we can throw it away as it will not generate min altitudes anymore
                    Utils.fastErase(activeSeaSideList, i--);
                } else {
                    final WalkPosition[] deltas = {
                            new WalkPosition(d.getX(), d.getY()), new WalkPosition(-d.getX(), d.getY()),
                            new WalkPosition(d.getX(), -d.getY()), new WalkPosition(-d.getX(), -d.getY()),
                            new WalkPosition(d.getY(), d.getX()), new WalkPosition(-d.getY(), d.getX()),
                            new WalkPosition(d.getY(), -d.getX()), new WalkPosition(-d.getY(), -d.getX())
                    };
                    for (final WalkPosition delta : deltas) {
                        final WalkPosition w = current.getLeft().add(delta);
                        if (terrainData.getMapData().isValid(w)) {
                            final MiniTile miniTile = terrainData.getMiniTile(w, bwem.util.CheckMode.NO_CHECK);
                            if (miniTile.isAltitudeMissing()) {
                                assertIf(updatedHighestAltitude != null
                                        && updatedHighestAltitude.intValue() > altitude.intValue());
                                updatedHighestAltitude = altitude;
                                current.setRight(altitude);
                                miniTile.setAltitude(altitude);
                            }
                        }
                    }
                }
            }
        }

        return updatedHighestAltitude;
    }

    // ----------------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////
    // BWMap::processBlockingNeutrals
    ////////////////////////////////////////////////////////////////////////

    private void processBlockingNeutrals(final List<Neutral> candidates) {
        for (final Neutral pCandidate : candidates) {
            if (pCandidate.getNextStacked()
                    == null) { // in the case where several neutrals are stacked, we only consider the top one
                final List<WalkPosition> border =
                        trimOuterMiniTileBorder(getOuterMiniTileBorderOfNeutral(pCandidate));

                final List<WalkPosition> doors = getDoors(border);

                final List<WalkPosition> trueDoors = getTrueDoors(doors, pCandidate);

                markBlockingStackedNeutrals(pCandidate, trueDoors);
            }
        }
    }

    private List<Neutral> getCandidates(
            final List<StaticBuilding> staticBuildings, final List<Mineral> minerals) {
        final List<Neutral> candidates = new ArrayList<>();
        candidates.addAll(staticBuildings);
        candidates.addAll(minerals);
        return candidates;
    }

    // ----------------------------------------------------------------------
    // 1)  Retrieve the Border: the outer border of pCandidate
    // ----------------------------------------------------------------------

    private List<WalkPosition> getOuterMiniTileBorderOfNeutral(final Neutral pCandidate) {
        return BwemExt.outerMiniTileBorder(pCandidate.getTopLeft(), pCandidate.getSize());
    }

    private List<WalkPosition> trimOuterMiniTileBorder(final List<WalkPosition> border) {
        border.removeIf(w -> {
            MapData mapData = getData().getMapData();
            MiniTile miniTile = getData().getMiniTile(w, bwem.util.CheckMode.NO_CHECK);
            Tile tile = getData().getTile(w.toTilePosition(), bwem.util.CheckMode.NO_CHECK);
            return !mapData.isValid(w) || !miniTile.isWalkable() || tile.getNeutral() != null;
        });
        return border;
    }

    // ----------------------------------------------------------------------

    private boolean isTraversablePosition(List<WalkPosition> visited, WalkPosition next) {
        return getData().getMapData().isValid(next) && !visited.contains(next)
                && getData().getMiniTile(next, bwem.util.CheckMode.NO_CHECK).isWalkable();
    }
    /**
     * 2) Find the doors in border: one door for each connected set of walkable, neighboring
     * miniTiles. The searched connected miniTiles all have to be next to some lake or some static
     * building, though they can't be part of one.
     */
    private List<WalkPosition> getDoors(final List<WalkPosition> border) {
        final List<WalkPosition> doors = new ArrayList<>();

        while (!border.isEmpty()) {
            final WalkPosition door = border.remove(border.size() - 1);
            doors.add(door);

            final List<WalkPosition> toVisit = new ArrayList<>();
            toVisit.add(door);

            final List<WalkPosition> visited = new ArrayList<>();
            visited.add(door);

            while (!toVisit.isEmpty()) {
                final WalkPosition current = toVisit.remove(toVisit.size() - 1);

                final WalkPosition[] deltas = {
                        new WalkPosition(0, -1),
                        new WalkPosition(-1, 0),
                        new WalkPosition(+1, 0),
                        new WalkPosition(0, +1)
                };
                for (final WalkPosition delta : deltas) {
                    final WalkPosition next = current.add(delta);
                    if (isTraversablePosition(visited, next)
                        && getData()
                            .getTile((next.toPosition()).toTilePosition(), bwem.util.CheckMode.NO_CHECK)
                            .getNeutral() == null
                        && BwemExt.adjoins8SomeLakeOrNeutral(next, this)) {
                            toVisit.add(next);
                            visited.add(next);
                    }
                }
            }

            border.removeIf(visited::contains);
        }

        return doors;
    }

    /**
     * 3) If at least 2 doors, find the true doors in Border: a true door is a door that gives onto an
     * area big enough
     */
    private List<WalkPosition> getTrueDoors(final List<WalkPosition> doors,
                                            final Neutral pCandidate) {
        final List<WalkPosition> trueDoors = new ArrayList<>();

        if (doors.size() >= 2) {
            for (final WalkPosition door : doors) {
                final List<WalkPosition> toVisit = new ArrayList<>();
                toVisit.add(door);

                final List<WalkPosition> visited = new ArrayList<>();
                visited.add(door);

                final int limit =
                        (pCandidate instanceof StaticBuilding) ? 10 : 400; // TODO: Description for 10 and 400?

                while (!toVisit.isEmpty() && (visited.size() < limit)) {
                    final WalkPosition current = toVisit.remove(toVisit.size() - 1);
                    final WalkPosition[] deltas = {
                            new WalkPosition(0, -1),
                            new WalkPosition(-1, 0),
                            new WalkPosition(+1, 0),
                            new WalkPosition(0, +1)
                    };
                    for (final WalkPosition delta : deltas) {
                        final WalkPosition next = current.add(delta);
                        if (isTraversablePosition(visited, next)
                            && getData().getTile(next.toTilePosition(), bwem.util.CheckMode.NO_CHECK).getNeutral()
                                    == null) {
                                toVisit.add(next);
                                visited.add(next);
                        }
                    }
                }
                if (visited.size() >= limit) {
                    trueDoors.add(door);
                }
            }
        }

        return trueDoors;
    }

    /**
     * 4) If at least 2 true doors, pCandidate is a blocking static building
     */
    private void markBlockingStackedNeutrals(
            final Neutral pCandidate, final List<WalkPosition> trueDoors) {
        if (trueDoors.size() >= 2) {
            // Marks pCandidate (and any Neutral stacked with it) as blocking.
            for (Neutral pNeutral = getData().getTile(pCandidate.getTopLeft()).getNeutral();
                 pNeutral != null;
                 pNeutral = pNeutral.getNextStacked()) {
                pNeutral.setBlocking(trueDoors);
            }

            // Marks all the miniTiles of pCandidate as blocked.
            // This way, areas at trueDoors won't merge together.
            final WalkPosition pCandidateW = pCandidate.getSize().toWalkPosition();
            for (int dy = 0; dy < pCandidateW.getY(); ++dy) {
                for (int dx = 0; dx < pCandidateW.getX(); ++dx) {
                    final MiniTile miniTile =
                            getData()
                                    .getMiniTile(
                                            ((pCandidate.getTopLeft().toPosition()).toWalkPosition())
                                                    .add(new WalkPosition(dx, dy)));
                    if (miniTile.isWalkable()) {
                        miniTile.setBlocked();
                    }
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////
    // BWMap::ComputeAreas
    ////////////////////////////////////////////////////////////////////////

    // Assigns MiniTile::m_areaId for each miniTile having AreaIdMissing()
    // Areas are computed using MiniTile::Altitude() information only.
    // The miniTiles are considered successively in descending order of their Altitude().
    // Each of them either:
    //   - involves the creation of a new area.
    //   - is added to some existing neighboring area.
    //   - makes two neighboring areas merge together.
    private void computeAreas(final List<TempAreaInfo> tempAreaList) {
        createAreas(tempAreaList, BwemExt.AREA_MIN_MINI_TILES);
        setAreaIdAndLowestAltitudeInTiles();
    }

    private List<Pair<WalkPosition, MiniTile>> getSortedMiniTilesByDescendingAltitude() {
        final List<Pair<WalkPosition, MiniTile>> miniTilesByDescendingAltitude =
                new ArrayList<>();

        for (int y = 0; y < getData().getMapData().getWalkSize().getY(); ++y) {
            for (int x = 0; x < getData().getMapData().getWalkSize().getX(); ++x) {
                final WalkPosition w = new WalkPosition(x, y);
                final MiniTile miniTile = getData().getMiniTile(w, bwem.util.CheckMode.NO_CHECK);
                if (miniTile.isAreaIdMissing()) {
                    miniTilesByDescendingAltitude.add(new Pair<>(w, miniTile));
                }
            }
        }

        miniTilesByDescendingAltitude.sort(MiniTile.BY_ALTITUDE_ORDER);
        Collections.reverse(miniTilesByDescendingAltitude);

        return miniTilesByDescendingAltitude;
    }

    private List<TempAreaInfo> computeTempAreas(
            final List<Pair<WalkPosition, MiniTile>> miniTilesByDescendingAltitude) {
        final List<TempAreaInfo> tempAreaList = new ArrayList<>();
        tempAreaList.add(new TempAreaInfo(asserter)); // tempAreaList[0] left unused, as AreaIds are > 0

        for (final Pair<WalkPosition, MiniTile> current : miniTilesByDescendingAltitude) {
            final WalkPosition pos = new WalkPosition(current.getLeft().getX(), current.getLeft().getY());
            final MiniTile cur = current.getRight();

            final Pair<AreaId, AreaId> neighboringAreas = findNeighboringAreas(pos);
            if (neighboringAreas.getLeft() == null) { // no neighboring area : creates of a new area
                tempAreaList.add(new TempAreaInfo(new AreaId(tempAreaList.size()), cur, pos, asserter));
            } else if (neighboringAreas.getRight()
                    == null) { // one neighboring area : adds cur to the existing area
                tempAreaList.get(neighboringAreas.getLeft().intValue()).add(cur);
            } else { // two neighboring areas : adds cur to one of them  &  possible merging
                AreaId smaller = neighboringAreas.getLeft();
                AreaId bigger = neighboringAreas.getRight();
                if (tempAreaList.get(smaller.intValue()).getSize()
                        > tempAreaList.get(bigger.intValue()).getSize()) {
                    AreaId smallerTmp = smaller;
                    smaller = bigger;
                    bigger = smallerTmp;
                }

                // Condition for the neighboring areas to merge:
                //                any_of(StartingLocations().begin(), StartingLocations().end(),
                // [&pos](const TilePosition & startingLoc)
                //                    { return dist(TilePosition(pos), startingLoc + TilePosition(2, 1)) <=
                // 3;})
                boolean cppAlgorithmStdAnyOf =
                        getData()
                                .getMapData()
                                .getStartingLocations()
                                .stream()
                                .anyMatch(
                                        startingLoc ->
                                                BwemExt.dist(pos.toTilePosition(), startingLoc.add(new TilePosition(2, 1)))
                                                        <= 3.0);
                final int curAltitude = cur.getAltitude().intValue();
                final int biggerHighestAltitude =
                        tempAreaList.get(bigger.intValue()).getHighestAltitude().intValue();
                final int smallerHighestAltitude =
                        tempAreaList.get(smaller.intValue()).getHighestAltitude().intValue();
                if ((tempAreaList.get(smaller.intValue()).getSize() < 80)
                        || (smallerHighestAltitude < 80)
                        || ((double) curAltitude / (double) biggerHighestAltitude >= 0.90)
                        || ((double) curAltitude / (double) smallerHighestAltitude >= 0.90)
                        || cppAlgorithmStdAnyOf) {
                    // adds cur to the absorbing area:
                    tempAreaList.get(bigger.intValue()).add(cur);

                    // merges the two neighboring areas:
                    replaceAreaIds(
                            tempAreaList.get(smaller.intValue()).getWalkPositionWithHighestAltitude(), bigger);
                    tempAreaList.get(bigger.intValue()).merge(tempAreaList.get(smaller.intValue()));
                } else { // no merge : cur starts or continues the frontier between the two neighboring
                    // areas
                    // adds cur to the chosen Area:
                    tempAreaList.get(chooseNeighboringArea(smaller, bigger).intValue()).add(cur);
                    super.rawFrontier.add(new Pair<>(neighboringAreas, pos));
                }
            }
        }

        // Remove from the frontier obsolete positions
        rawFrontier.removeIf(f -> f.getLeft().getLeft().equals(f.getLeft().getRight()));

        return tempAreaList;
    }

    private void replaceAreaIds(final WalkPosition p, final AreaId newAreaId) {
        final MiniTile origin = getData().getMiniTile(p, bwem.util.CheckMode.NO_CHECK);
        final AreaId oldAreaId = origin.getAreaId();
        origin.replaceAreaId(newAreaId);

        List<WalkPosition> toSearch = new ArrayList<>();
        toSearch.add(p);
        while (!toSearch.isEmpty()) {
            final WalkPosition current = toSearch.remove(toSearch.size() - 1);

            final WalkPosition[] deltas = {
                    new WalkPosition(0, -1),
                    new WalkPosition(-1, 0),
                    new WalkPosition(+1, 0),
                    new WalkPosition(0, +1)
            };
            for (final WalkPosition delta : deltas) {
                final WalkPosition next = current.add(delta);
                if (getData().getMapData().isValid(next)) {
                    final MiniTile miniTile = getData().getMiniTile(next, bwem.util.CheckMode.NO_CHECK);
                    if (miniTile.getAreaId().equals(oldAreaId)) {
                        toSearch.add(next);
                        miniTile.replaceAreaId(newAreaId);
                    }
                }
            }
        }

        // also replaces references of oldAreaId by newAreaId in getRawFrontier:
        if (newAreaId.intValue() > 0) {
            for (final Pair<Pair<AreaId, AreaId>, WalkPosition> f : super.rawFrontier) {
                if (f.getLeft().getLeft().equals(oldAreaId)) {
                    f.getLeft().setLeft(newAreaId);
                }
                if (f.getLeft().getRight().equals(oldAreaId)) {
                    f.getLeft().setRight(newAreaId);
                }
            }
        }
    }

    // Initializes Graph with the valid and big enough areas in tempAreaList.
    private void createAreas(final List<TempAreaInfo> tempAreaList, final int areaMinMiniTiles) {
        final List<Pair<WalkPosition, Integer>> areasList = new ArrayList<>();

        int newAreaId = 1;
        int newTinyAreaId = -2;

        for (final TempAreaInfo tempArea : tempAreaList) {
            if (tempArea.isValid()) {
                int tempAreaSize = tempArea.getSize();
                if (tempAreaSize >= areaMinMiniTiles) {
                    AreaId tempAreaId = tempArea.getId();
                    assertIf((newAreaId > tempAreaId.intValue()));
                    if (newAreaId != tempAreaId.intValue()) {
                        replaceAreaIds(tempArea.getWalkPositionWithHighestAltitude(), new AreaId(newAreaId));
                    }

                    areasList.add(
                            new Pair<>(tempArea.getWalkPositionWithHighestAltitude(), tempAreaSize));
                    ++newAreaId;
                } else {
                    replaceAreaIds(tempArea.getWalkPositionWithHighestAltitude(), new AreaId(newTinyAreaId));
                    --newTinyAreaId;
                }
            }
        }

        getGraph().createAreas(areasList);
    }

    private void assertIf(boolean condition) {
        if (condition) {
            asserter.throwIllegalStateException("");
        }
    }

    // Renamed from "BWMap::SetAltitudeInTile"
    private void setLowestAltitudeInTile(final TilePosition t) {
        Altitude lowestAltitude = new Altitude(Integer.MAX_VALUE);

        for (int dy = 0; dy < 4; ++dy) {
            for (int dx = 0; dx < 4; ++dx) {
                final Altitude altitude =
                        getData()
                                .getMiniTile(
                                        ((t.toPosition()).toWalkPosition()).add(new WalkPosition(dx, dy)),
                                        bwem.util.CheckMode.NO_CHECK)
                                .getAltitude();
                if (altitude.intValue() < lowestAltitude.intValue()) {
                    lowestAltitude = altitude;
                }
            }
        }

        getData().getTile(t).setLowestAltitude(lowestAltitude);
    }

    // Renamed from "BWMap::SetAreaIdInTiles"
    private void setAreaIdAndLowestAltitudeInTiles() {
        for (int y = 0; y < getData().getMapData().getTileSize().getY(); ++y)
            for (int x = 0; x < getData().getMapData().getTileSize().getX(); ++x) {
                final TilePosition t = new TilePosition(x, y);
                setAreaIdInTile(t);
                setLowestAltitudeInTile(t);
            }
    }

    void onBlockingNeutralDestroyed(Neutral pBlocking) {
        if (pBlocking == null) {
            throw new IllegalStateException();
        }
        assertIf(!pBlocking.isBlocking());

        for (Area pArea : pBlocking.getBlockedAreas()) {
            for (ChokePoint cp : pArea.getChokePoints()) {
                cp.onBlockingNeutralDestroyed(pBlocking);
            }
        }

        // there remains some blocking Neutrals at the same location
        if (getData().getTile(pBlocking.getTopLeft()).getNeutral() != null) {
            return;
        }

        // Unblock the miniTiles of pBlocking:
        AreaId newId = pBlocking.getBlockedAreas().iterator().next().getId();
        WalkPosition pBlockingW = pBlocking.getSize().toWalkPosition();
        for (int dy = 0; dy < pBlockingW.getY(); ++dy) {
            for (int dx = 0; dx < pBlockingW.getX(); ++dx) {
                MiniTile miniTile = getData().getMiniTile(
                        pBlocking.getTopLeft().toWalkPosition().add(new WalkPosition(dx, dy)));
                if (miniTile.isWalkable()) {
                    miniTile.replaceBlockedAreaId(newId);
                }
            }
        }

        // Unblock the Tiles of pBlocking:
        for (int dy = 0; dy < pBlocking.getSize().getY(); ++dy) {
            for (int dx = 0; dx < pBlocking.getSize().getX(); ++dx) {
                getData().getTile(pBlocking.getTopLeft().add(new TilePosition(dx, dy)))
                        .resetAreaId();
                setAreaIdInTile(pBlocking.getTopLeft().add(new TilePosition(dx, dy)));
            }
        }

        if (automaticPathUpdate()) {
            getGraph().computeChokePointDistanceMatrix();
        }
    }
}
