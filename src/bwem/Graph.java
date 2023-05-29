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
import bwem.util.Pred;
import bwem.util.Utils;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.WalkPosition;

import java.util.*;

public final class Graph {
    private final BWMap map;
    private final List<Area> areas = new ArrayList<>();
    private final List<ChokePoint> chokePoints = new ArrayList<>();
    private final List<List<List<ChokePoint>>> chokePointsMatrix =
            new ArrayList<>(); // index == Area::id x Area::id
    private final List<List<Integer>> chokePointDistanceMatrix =
            new ArrayList<>(); // index == ChokePoint::index x ChokePoint::index
    private final List<List<CPPath>> pathsBetweenChokePoints =
            new ArrayList<>(); // index == ChokePoint::index x ChokePoint::index
    private final List<Base> bases = new ArrayList<>();

    Graph(BWMap map) {
        this.map = map;
    }

    public BWMap getMap() {
        return map;
    }

    public List<Area> getAreas() {
        return areas;
    }

    public int getAreaCount() {
        return areas.size();
    }

    public Area getArea(final AreaId id) {
        if (!isValid(id)) {
            map.asserter.throwIllegalStateException("");
        }
        return areas.get(id.intValue() - 1);
    }

    public Area getArea(final WalkPosition walkPosition) {
        final AreaId areaId = getMap().getData().getMiniTile(walkPosition).getAreaId();
        return (areaId.intValue() > 0) ? getArea(areaId) : null;
    }

    public Area getArea(final TilePosition tilePosition) {
        final AreaId areaId = getMap().getData().getTile(tilePosition).getAreaId();
        return (areaId.intValue() > 0) ? getArea(areaId) : null;
    }

    public Area getNearestArea(final WalkPosition walkPosition) {
        final Area area = getArea(walkPosition);
        if (area != null) {
            return area;
        }

        final WalkPosition w =
                getMap()
                        .breadthFirstSearch(
                                walkPosition,
                                // findCond
                                (MiniTile miniTile, WalkPosition unused) -> (miniTile.getAreaId().intValue() > 0),
                                // visitCond
                                Pred.accept());

        return getArea(w);
    }

    public Area getNearestArea(final TilePosition tilePosition) {
        final Area area = getArea(tilePosition);
        if (area != null) {
            return area;
        }

        final TilePosition t =
                getMap()
                        .breadthFirstSearch(
                                tilePosition,
                                // findCond
                                (Tile tile, TilePosition unused) -> tile.getAreaId().intValue() > 0,
                                // visitCond
                                (Tile tile, TilePosition unused) -> true);

        return getArea(t);
    }

    // Returns the list of all the getChokePoints in the BWMap.
    public List<ChokePoint> getChokePoints() {
        return chokePoints;
    }

    // Returns the getChokePoints between two areas.
    private List<ChokePoint> getChokePoints(final AreaId a, final AreaId b) {
        if (!isValid(a) || !isValid(b) || a.intValue() == b.intValue()) {
            map.asserter.throwIllegalStateException("");
        }

        int aVal = a.intValue();
        int bVal = b.intValue();
        if (aVal > bVal) {
            int aValTmp = aVal;
            aVal = bVal;
            bVal = aValTmp;
        }

        return chokePointsMatrix.get(bVal).get(aVal);
    }

    // Returns the getChokePoints between two areas.
    private List<ChokePoint> getChokePoints(final Area a, final Area b) {
        return getChokePoints(a.getId(), b.getId());
    }

    // Returns the ground distance in pixels between cpA->center() and cpB>center()
    public int distance(ChokePoint cpA, ChokePoint cpB) {
        return chokePointDistanceMatrix
                .get(cpA.getIndex())
                .get(cpB.getIndex());
    }

    // Returns a list of getChokePoints, which is intended to be the shortest walking path from cpA to
    // cpB.
    public CPPath getPath(ChokePoint cpA, ChokePoint cpB) {
        return pathsBetweenChokePoints
                .get(cpA.getIndex())
                .get(cpB.getIndex());
    }

    public Optional<PathingResult> getPathingResult(Position a, Position b) {
        return new Pathing(a, b).getPathWithLength();
    }

    public Optional<CPPath> getPath(final Position a, final Position b) {
        return new Pathing(a, b).getPath();
    }

    public List<Base> getBases() {
        return this.bases;
    }

    // Creates a new Area for each pair (top, miniTiles) in areasList (See Area::top() and
    // Area::miniTiles())
    void createAreas(final List<Pair<WalkPosition, Integer>> areasList) {
        for (int id = 1; id <= areasList.size(); ++id) {
            final WalkPosition top = areasList.get(id - 1).getLeft();
            final int miniTileCount = areasList.get(id - 1).getRight();
            this.areas.add(new AreaInitializer(getMap(), new AreaId(id), top, miniTileCount));
        }
    }

    ////////////////////////////////////////////////////////////////////////
    // Graph::createChokePoints
    ////////////////////////////////////////////////////////////////////////

    // ----------------------------------------------------------------------
    // 1) size the matrix
    // ----------------------------------------------------------------------
    private void initializeChokePointsMatrix() {
        int areasCount = getAreaCount();
        chokePointsMatrix.clear();
        // Unused due to ids starting at 1
        chokePointsMatrix.add(null);
        for (int id = 1; id <= areasCount; ++id) { // triangular matrix
            ArrayList<List<ChokePoint>> subList = new ArrayList<>();
            chokePointsMatrix.add(subList);
            for (int n = 0; n < id; ++n) {
                subList.add(new ArrayList<>());
            }
        }
    }
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    // 2) Dispatch the global raw frontier between all the relevant pairs of areas:
    // ----------------------------------------------------------------------
    private Map<Pair<AreaId, AreaId>, List<WalkPosition>>
    createRawFrontierByAreaPairMap(
            final List<Pair<Pair<AreaId, AreaId>, WalkPosition>> rawFrontier) {
        final Map<Pair<AreaId, AreaId>, List<WalkPosition>> rawFrontierByAreaPair =
                new HashMap<>();

        for (final Pair<Pair<AreaId, AreaId>, WalkPosition> raw : rawFrontier) {
            int a = raw.getLeft().getLeft().intValue();
            int b = raw.getLeft().getRight().intValue();
            if (a > b) {
                final int a_tmp = a;
                a = b;
                b = a_tmp;
            }
            if (!((a >= 1) && (b <= getAreaCount()))) {
                map.asserter.throwIllegalStateException("");
            }

            final Pair<AreaId, AreaId> key = new Pair<>(new AreaId(a), new AreaId(b));
            rawFrontierByAreaPair.computeIfAbsent(key, mp -> new ArrayList<>()).add(raw.getRight());
        }

        return rawFrontierByAreaPair;
    }
    // ----------------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////////

    // Creates a new Area for each pair (top, miniTiles) in AreasList (See Area::top() and
    // Area::miniTiles())
    public void createChokePoints(
            final List<StaticBuilding> staticBuildings,
            final List<Mineral> minerals,
            final List<Pair<Pair<AreaId, AreaId>, WalkPosition>> rawFrontier) {
        int newIndex = 0;
        final List<Neutral> blockingNeutrals = new ArrayList<>();
        for (final StaticBuilding s : staticBuildings) {
            if (s.isBlocking()) {
                blockingNeutrals.add(s);
            }
        }
        for (final Mineral m : minerals) {
            if (m.isBlocking()) {
                blockingNeutrals.add(m);
            }
        }

        // Note: pseudoChokePointsToCreate is only used for pre-allocating the GetChokePoints array
        // size.
        //      This number will highly likely be very small. There is no reason to set a minimum size.
        //        int pseudoChokePointsToCreate = 0;
        //        for (final Neutral blockingNeutral : blockingNeutrals) {
        //            if (blockingNeutral.getNextStacked() == null) {
        //                ++pseudoChokePointsToCreate;
        //            }
        //        }

        // 1) size the matrix
        initializeChokePointsMatrix();

        // 2) Dispatch the global raw frontier between all the relevant pairs of areas:
        final Map<Pair<AreaId, AreaId>, List<WalkPosition>> rawFrontierByAreaPair =
                createRawFrontierByAreaPairMap(rawFrontier);

        // 3) For each pair of areas (A, B):
        for (final Map.Entry<Pair<AreaId, AreaId>, List<WalkPosition>> entry :
                rawFrontierByAreaPair.entrySet()) {
            Pair<AreaId, AreaId> rawleft = entry.getKey();
            final List<WalkPosition> rawFrontierAB = entry.getValue();

            // Because our dispatching preserved order,
            // and because BWMap::m_RawFrontier was populated in descending order of the altitude (see
            // BWMap::computeAreas),
            // we know that rawFrontierAB is also ordered the same way, but let's check it:
            {
                final List<Altitude> altitudes = new ArrayList<>();
                for (final WalkPosition w : rawFrontierAB) {
                    altitudes.add(getMap().getData().getMiniTile(w).getAltitude());
                }

                // Check if the altitudes array is sorted in descending order.
                for (int i = 1; i < altitudes.size(); ++i) {
                    final int prev = altitudes.get(i - 1).intValue();
                    final int curr = altitudes.get(i).intValue();
                    if (prev < curr) {
                        map.asserter.throwIllegalStateException("");
                    }
                }
            }

            // 3.1) Use that information to efficiently cluster rawFrontierAB in one or several
            // chokepoints.
            //    Each cluster will be populated starting with the center of a chokepoint (max altitude)
            //    and finishing with the ends (min altitude).
            final int clusterMinDist = (int) Math.sqrt(BwemExt.LAKE_MAX_MINI_TILES);
            final List<List<WalkPosition>> clusters = new ArrayList<>();
            for (final WalkPosition w : rawFrontierAB) {
                boolean added = false;
                for (final List<WalkPosition> cluster : clusters) {
                    final int distToFront = BwemExt.queenWiseDist(cluster.get(0), w);
                    final int distToBack = BwemExt.queenWiseDist(cluster.get(cluster.size() - 1), w);
                    if (Math.min(distToFront, distToBack) <= clusterMinDist) {
                        if (distToFront < distToBack) {
                            cluster.add(0, w);
                        } else {
                            cluster.add(w);
                        }
                        added = true;
                        break;
                    }
                }

                if (!added) {
                    final List<WalkPosition> list = new ArrayList<>();
                    list.add(w);
                    clusters.add(list);
                }
            }

            // 3.2) Create one Chokepoint for each cluster:
            final AreaId a = rawleft.getLeft();
            final AreaId b = rawleft.getRight();
            for (final List<WalkPosition> cluster : clusters) {
                getChokePoints(a, b)
                        .add(new ChokePoint(this, newIndex, getArea(a), getArea(b), cluster));
                newIndex++;
            }
        }

        // 4) Create one Chokepoint for each pair of blocked areas, for each blocking Neutral:
        for (final Neutral blockingNeutral : blockingNeutrals) {
            if (blockingNeutral.getNextStacked()
                    == null) { // in the case where several neutrals are stacked, we only consider the top
                final List<Area> blockedAreas = blockingNeutral.getBlockedAreas();
                for (final Area blockedAreaA : blockedAreas)
                    for (final Area blockedAreaB : blockedAreas) {
                        if (blockedAreaB.equals(blockedAreaA)) {
                            break; // breaks symmetry
                        }

                        final WalkPosition center =
                                getMap().breadthFirstSearch(
                                        blockingNeutral.getCenter().toWalkPosition(),
                                        // findCond
                                        (MiniTile miniTile, WalkPosition unused) -> miniTile.isWalkable(),
                                        // visitCond
                                        Pred.accept());

                        final List<WalkPosition> list = new ArrayList<>();
                        list.add(center);
                        getChokePoints(blockedAreaA, blockedAreaB)
                                .add(new ChokePoint(this, newIndex, blockedAreaA, blockedAreaB, list, blockingNeutral));
                        newIndex++;
                    }
            }
        }

        // 5) Set the references to the freshly created Chokepoints:
        for (int loopA = 1; loopA <= getAreaCount(); ++loopA)
            for (int loopB = 1; loopB < loopA; ++loopB) {
                final AreaId a = new AreaId(loopA);
                final AreaId b = new AreaId(loopB);
                if (!getChokePoints(a, b).isEmpty()) {
                    ((AreaInitializer) getArea(a)).addChokePoints(getArea(b), getChokePoints(a, b));
                    ((AreaInitializer) getArea(b)).addChokePoints(getArea(a), getChokePoints(a, b));

                    this.chokePoints.addAll(getChokePoints(a, b));
                }
            }
    }

    // ----------------------------------------------------------------------

    // Computes the ground distances between any pair of ChokePoints in pContext
    // This is achieved by invoking several times pContext->ComputeDistances,
    // which effectively computes the distances from one starting ChokePoint, using Dijkstra's
    // algorithm.
    // If Context == Area, Dijkstra's algorithm works on the Tiles inside one Area.
    // If Context == Graph, Dijkstra's algorithm works on the GetChokePoints between the AreaS.
    public void computeChokePointDistanceMatrix() {
        // 1) size the matrix
        chokePointDistanceMatrix.clear();
        for (int i = 0; i < chokePoints.size(); ++i) {
            chokePointDistanceMatrix.add(new ArrayList<>());
        }
        for (List<Integer> chokePointDistanceMatrix1 : chokePointDistanceMatrix) {
            for (int n = 0; n < chokePoints.size(); ++n) {
                chokePointDistanceMatrix1.add(-1);
            }
        }

        pathsBetweenChokePoints.clear();
        for (int i = 0; i < chokePoints.size(); ++i) {
            pathsBetweenChokePoints.add(new ArrayList<>());
        }
        for (List<CPPath> pathsBetweenChokePoint : pathsBetweenChokePoints) {
            for (int n = 0; n < chokePoints.size(); ++n) {
                pathsBetweenChokePoint.add(new CPPath());
            }
        }

        // 2) Compute distances inside each Area
        for (final Area area : getAreas()) {
            computeChokePointDistances(area);
        }

        // 3) Compute distances through connected areas
        computeChokePointDistances(this);

        for (final ChokePoint cp : getChokePoints()) {
            setDistance(cp, cp, 0);
            CPPath cppath = new CPPath();
            cppath.add(cp);
            setPath(cp, cp, cppath);
        }

        // 4) Update Area::m_AccessibleNeighbors for each Area
        for (final Area area : getAreas()) ((AreaInitializer) area).updateAccessibleNeighbors();

        // 5)  Update Area::m_groupId for each Area
        updateGroupIds();
    }

    public void collectInformation() {
        // 1) Process the whole BWMap:

        for (final Mineral mineral : getMap().getNeutralData().getMinerals()) {
            final Area area = getMap().getMainArea(mineral.getTopLeft(), mineral.getSize());
            if (area != null) {
                ((AreaInitializer) area).addMineral(mineral);
            }
        }

        for (Geyser geyser : getMap().getNeutralData().getGeysers()) {
            final Area area = getMap().getMainArea(geyser.getTopLeft(), geyser.getSize());
            if (area != null) {
                ((AreaInitializer) area).addGeyser(geyser);
            }
        }

        for (int y = 0; y < getMap().getData().getMapData().getTileSize().getY(); ++y)
            for (int x = 0; x < getMap().getData().getMapData().getTileSize().getX(); ++x) {
                final Tile tile = getMap().getData().getTile(new TilePosition(x, y));
                if (tile.getAreaId().intValue() > 0) {
                    ((AreaInitializer) getArea(tile.getAreaId()))
                            .addTileInformation(new TilePosition(x, y), tile);
                }
            }
    }

    public void createBases(final TerrainData terrainData) {
        this.bases.clear();
        for (final Area area : this.areas) {
            ((AreaInitializer) area).createBases(terrainData);
            this.bases.addAll(area.getBases());
        }
    }

    ////////////////////////////////////////////////////////////////////////
    // Graph::ComputeChokePointDistances
    ////////////////////////////////////////////////////////////////////////

    // Computes the ground distances between any pair of getChokePoints in pContext
    // This is achieved by invoking several times pContext->computeDistances,
    // which effectively computes the distances from one starting ChokePoint, using Dijkstra's
    // algorithm.
    // If Context == Area, Dijkstra's algorithm works on the Tiles inside one Area.
    // If Context == Graph, Dijkstra's algorithm works on the getChokePoints between the AreaS.

    private void computeChokePointDistances(final Area pContext) {
        for (final ChokePoint pStart : pContext.getChokePoints()) {
            final List<ChokePoint> targets = new ArrayList<>();
            for (final ChokePoint cp : pContext.getChokePoints()) {
                if (cp.equals(pStart)) {
                    break; // breaks symmetry
                }
                targets.add(cp);
            }

            final int[] distanceToTargets =
                    ((AreaInitializer) pContext).computeDistances(pStart, targets);

            setPathForComputeChokePointDistances(distanceToTargets, pStart, targets, false);
        }
    }

    private void computeChokePointDistances(final Graph pContext) {
        for (final ChokePoint pStart : pContext.getChokePoints()) {
            final List<ChokePoint> targets = new ArrayList<>();
            for (final ChokePoint cp : pContext.getChokePoints()) {
                if (cp.equals(pStart)) {
                    break; // breaks symmetry
                }
                targets.add(cp);
            }

            final int[] distanceToTargets = pContext.computeDistances(pStart, targets);

            setPathForComputeChokePointDistances(distanceToTargets, pStart, targets, true);
        }
    }

    private void setPathForComputeChokePointDistances(
            final int[] distanceToTargets,
            ChokePoint pStart,
            List<ChokePoint> targets,
            boolean collectIntermediateChokePoints) {
        for (int i = 0; i < targets.size(); ++i) {
            final int newDist = distanceToTargets[i];
            final ChokePoint target = targets.get(i);
            final int existingDist = distance(pStart, target);

            if (newDist != 0 && ((existingDist == -1) || (newDist < existingDist))) {
                setDistance(pStart, target, newDist);

                // Build the path from pStart to targets[i]:

                final CPPath path = new CPPath();
                path.add(pStart);
                path.add(target);

                //                // if (Context == Graph), there may be intermediate getChokePoints. They
                // have been set by computeDistances,
                //                // so we just have to collect them (in the reverse order) and insert them
                // into Path:
                //                if ((void *)(pContext) == (void *)(this))	// tests (Context == Graph)
                // without warning about constant condition
                if (collectIntermediateChokePoints) {
                    for (ChokePoint pPrev = target.getPathBackTrace();
                         !pPrev.equals(pStart);
                         pPrev = pPrev.getPathBackTrace()) {
                        path.add(1, pPrev);
                    }
                }

                setPath(pStart, target, path);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////

    // Returns Distances such that Distances[i] == ground_distance(start, targets[i]) in pixels
    // Any Distances[i] may be 0 (meaning targets[i] is not reachable).
    // This may occur in the case where start and targets[i] leave in different continents or due to
    // Bloqued intermediate ChokePoint(s).
    // For each reached target, the shortest path can be derived using
    // the backward trace set in cp->getPathBackTrace() for each intermediate ChokePoint cp from the
    // target.
    // Note: same algo than Area::computeDistances (derived from Dijkstra)
    private int[] computeDistances(final ChokePoint start, final List<ChokePoint> targets) {
        final int[] distances = new int[targets.size()];

        Tile.getStaticMarkable().unmarkAll();

        final Queue<Pair<Integer, ChokePoint>> toVisit =
                new PriorityQueue<>(Comparator.comparingInt(Pair::getLeft));
        toVisit.offer(new Pair<>(0, start));

        int remainingTargets = targets.size();
        while (!toVisit.isEmpty()) {
            final Pair<Integer, ChokePoint> distanceAndChokePoint = toVisit.poll();
            final int currentDist = distanceAndChokePoint.getLeft();
            final ChokePoint current = distanceAndChokePoint.getRight();
            final Tile currentTile =
                    getMap().getData().getTile(current.getCenter().toTilePosition(), bwem.util.CheckMode.NO_CHECK);
            if (!(currentTile.getInternalData() == currentDist)) {
                map.asserter.throwIllegalStateException("");
            }
            currentTile.setInternalData(0); // resets Tile::m_internalData for future usage
            currentTile.getMarkable().setMarked();

            for (int i = 0; i < targets.size(); ++i) {
                if (current == targets.get(i)) {
                    distances[i] = currentDist;
                    --remainingTargets;
                }
            }
            if (remainingTargets == 0) {
                break;
            }

            if (current.isBlocked() && (!current.equals(start))) {
                continue;
            }

            for (final Area pArea :
                    new Area[]{current.getAreas().getLeft(), current.getAreas().getRight()}) {
                for (final ChokePoint next : pArea.getChokePoints()) {
                    if (!next.equals(current)) {
                        final int newNextDist = currentDist + distance(current, next);
                        final Tile nextTile =
                                getMap().getData().getTile(next.getCenter().toTilePosition(), bwem.util.CheckMode.NO_CHECK);
                        if (nextTile.getMarkable().isUnmarked()) {
                            if (nextTile.getInternalData() == 0 || newNextDist < nextTile.getInternalData()) {
                                if (nextTile.getInternalData() != 0) {
                                    final boolean removed = toVisit.remove(new Pair<>(nextTile.getInternalData(), next));
                                    if (!removed) {
                                        map.asserter.throwIllegalStateException("");
                                    }
                                }

                                nextTile.setInternalData(newNextDist);
                                next.setPathBackTrace(current);
                                toVisit.offer(new Pair<>(newNextDist, next));
                            }
                        }
                    }
                }
            }
        }

        //    //	bwem_assert(!remainingTargets);
        //        if (!(remainingTargets == 0)) {
        //            throw new IllegalStateException();
        //        }

        // reset Tile::m_internalData for future usage
        for (Pair<Integer, ChokePoint> distanceToChokePoint : toVisit) {
            getMap()
                    .getData()
                    .getTile(
                            distanceToChokePoint.getRight().getCenter().toTilePosition(),
                            bwem.util.CheckMode.NO_CHECK)
                    .setInternalData(0);
        }

        return distances;
    }

    private void updateGroupIds() {
        int nextGroupId = 1;

        AreaInitializer.getStaticMarkable().unmarkAll();

        for (final Area start : getAreas()) {
            if (((AreaInitializer) start).getMarkable().isUnmarked()) {
                final List<Area> toVisit = new ArrayList<>();
                toVisit.add(start);
                while (!toVisit.isEmpty()) {
                    final Area current = toVisit.remove(toVisit.size() - 1);
                    ((AreaInitializer) current).setGroupId(nextGroupId);

                    for (final Area next : current.getAccessibleNeighbors()) {
                        if (((AreaInitializer) next).getMarkable().isUnmarked()) {
                            ((AreaInitializer) next).getMarkable().setMarked();
                            toVisit.add(next);
                        }
                    }
                }
                ++nextGroupId;
            }
        }
    }

    private void setDistance(final ChokePoint cpA, final ChokePoint cpB, final int value) {
        final int indexA = cpA.getIndex();
        final int indexB = cpB.getIndex();
        this.chokePointDistanceMatrix.get(indexA).set(indexB, value);
        this.chokePointDistanceMatrix.get(indexB).set(indexA, value);
    }

    private void setPath(final ChokePoint cpA, final ChokePoint cpB, final CPPath pathAB) {
        final int indexA = cpA.getIndex();
        final int indexB = cpB.getIndex();

        this.pathsBetweenChokePoints.get(indexA).set(indexB, pathAB);

        if (cpA != cpB) {
            final CPPath reversePath = this.pathsBetweenChokePoints.get(indexB).get(indexA);
            reversePath.clear();
            for (int i = pathAB.size() - 1; i >= 0; --i) {
                final ChokePoint cp = pathAB.get(i);
                reversePath.add(cp);
            }
        }
    }

    private boolean isValid(AreaId id) {
        return (1 <= id.intValue() && id.intValue() <= getAreaCount());
    }

    private class Pathing {
        private final Position a;
        private final Position b;
        private final Area areaA;
        private final Area areaB;
        private final CPPath path;
        private ChokePoint pBestCpA;
        private ChokePoint pBestCpB;
        private int minDistAB;

        Pathing(Position a, Position b) {
            this.a = a;
            this.b = b;
            areaA = getNearestArea(a.toWalkPosition());
            areaB = getNearestArea(b.toWalkPosition());

            if (areaA.equals(areaB)) {
                path = CPPath.EMPTY_PATH;
                return;
            }

            if (!areaA.isAccessibleFrom(areaB)) {
                path = null;
                return;
            }

            minDistAB = Integer.MAX_VALUE;

            for (final ChokePoint cpA : areaA.getChokePoints()) {
                if (!cpA.isBlocked()) {
                    final int distACpA = a.getDistance(cpA.getCenter().toPosition());
                    for (final ChokePoint cpB : areaB.getChokePoints()) {
                        if (!cpB.isBlocked()) {
                            final int distBToCPB = b.getDistance(cpB.getCenter().toPosition());
                            final int distAToB = distACpA + distBToCPB + distance(cpA, cpB);
                            if (distAToB < minDistAB) {
                                minDistAB = distAToB;
                                pBestCpA = cpA;
                                pBestCpB = cpB;
                            }
                        }
                    }
                }
            }

            if (minDistAB == Integer.MAX_VALUE) {
                map.asserter.throwIllegalStateException("");
            }

            path = Graph.this.getPath(pBestCpA, pBestCpB);
        }

        Optional<PathingResult> getPathWithLength() {
            if (path == null) {
                return Optional.empty();
            }
            if (areaA.equals(areaB)) {
                return Optional.of(new PathingResult(path, a.getDistance(b)));
            }

            if (path.size() == 1) {
                if (!pBestCpA.equals(pBestCpB)) {
                    map.asserter.throwIllegalStateException("");
                }

                final Position cpEnd1 = BwemExt.center(pBestCpA.getNodePosition(ChokePoint.Node.END1));
                final Position cpEnd2 = BwemExt.center(pBestCpA.getNodePosition(ChokePoint.Node.END2));
                if (Utils.intersect(
                        a.getX(),
                        a.getY(),
                        b.getX(),
                        b.getY(),
                        cpEnd1.getX(),
                        cpEnd1.getY(),
                        cpEnd2.getX(),
                        cpEnd2.getY())) {
                    return Optional.of(new PathingResult(path, a.getDistance(b)));
                } else {
                    int pLength = minDistAB;
                    for (final ChokePoint.Node node :
                            new ChokePoint.Node[]{ChokePoint.Node.END1, ChokePoint.Node.END2}) {
                        Position c = BwemExt.center(pBestCpA.getNodePosition(node));
                        int distAToB = a.getDistance(c) + b.getDistance(c);
                        pLength = Math.min(pLength, distAToB);
                    }
                    return Optional.of(new PathingResult(path, pLength));
                }
            }
            return Optional.of(new PathingResult(path, minDistAB));
        }

        Optional<CPPath> getPath() {
            return Optional.ofNullable(path);
        }
    }
}
