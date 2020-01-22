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
import bwem.util.Pair;
import org.openbw.bwapi4j.WalkPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ChokePoints are frontiers that BWEM automatically computes from Brood War's maps.<br>
 * A ChokePoint represents (part of) the frontier between exactly 2 Areas. It has a form of line.
 * <br>
 * A ChokePoint doesn't contain any MiniTile: All the MiniTiles whose positions are returned by its
 * Geometry() are just guaranteed to be part of one of the 2 Areas.<br>
 * Among the MiniTiles of its Geometry, 3 particular ones called nodes can also be accessed using
 * Pos(middle), Pos(end1) and Pos(end2).<br>
 * ChokePoints play an important role in BWEM:<br>
 * - they define accessibility between Areas.<br>
 * - the Paths provided by BWMap::GetPath are made of ChokePoints.<br>
 * Like Areas and Bases, the number and the addresses of ChokePoint instances remain unchanged.<br>
 * <br>
 * Pseudo ChokePoints:<br>
 * Some Neutrals can be detected as blocking Neutrals (Cf. Neutral::Blocking).<br>
 * Because only ChokePoints can serve as frontiers between Areas, BWEM automatically creates a
 * ChokePoint for each blocking Neutral (only one in the case of stacked blocking Neutral).<br>
 * Such ChokePoints are called pseudo ChokePoints and they behave differently in several ways.
 */
public final class ChokePoint {
    private final Graph graph;
    private final boolean isPseudo;
    private final int index;
    private final Pair<Area, Area> areas;
    private final WalkPosition[] nodes;
    private final List<Pair<WalkPosition, WalkPosition>> nodesInArea;
    private final List<WalkPosition> geometry;
    private boolean isBlocked;
    private Neutral blockingNeutral;
    private ChokePoint pathBackTrace = null;

    ChokePoint(
        final Graph graph,
        final int index,
        final Area area1,
        final Area area2,
        final List<WalkPosition> geometry,
        final Neutral blockingNeutral) {
        if (geometry.isEmpty()) {
            graph.getMap().asserter.throwIllegalStateException("");
        }

        this.graph = graph;
        this.index = index;
        this.areas = new Pair<>(area1, area2);
        this.geometry = geometry;

        // Ensures that in the case where several neutrals are stacked, blockingNeutral points to the
        // bottom one:
        this.blockingNeutral =
                blockingNeutral != null
                        ? getMap().getData().getTile(blockingNeutral.getTopLeft()).getNeutral()
                        : blockingNeutral;

        this.isBlocked = blockingNeutral != null;
        this.isPseudo = this.isBlocked;

        this.nodes = new WalkPosition[Node.NODE_COUNT.ordinal()];
        this.nodes[Node.END1.ordinal()] = geometry.get(0);
        this.nodes[Node.END2.ordinal()] = geometry.get(geometry.size() - 1);

        this.nodesInArea = new ArrayList<>(Node.NODE_COUNT.ordinal());
        for (int i = 0; i < Node.NODE_COUNT.ordinal(); ++i) {
            this.nodesInArea.add(new Pair<>(new WalkPosition(0, 0), new WalkPosition(0, 0)));
        }

        int i = geometry.size() / 2;
        while ((i > 0)
                && (getMap().getData().getMiniTile(geometry.get(i - 1)).getAltitude().intValue()
                > getMap().getData().getMiniTile(geometry.get(i)).getAltitude().intValue())) {
            --i;
        }
        while ((i < geometry.size() - 1)
                && (getMap().getData().getMiniTile(geometry.get(i + 1)).getAltitude().intValue()
                > getMap().getData().getMiniTile(geometry.get(i)).getAltitude().intValue())) {
            ++i;
        }
        this.nodes[Node.MIDDLE.ordinal()] = geometry.get(i);

        BWMap map = getMap();
        for (int n = 0; n < Node.NODE_COUNT.ordinal(); ++n) {
            for (final Area area : new Area[]{area1, area2}) {
                final WalkPosition nodeInArea =
                    getGraph()
                        .getMap()
                        .breadthFirstSearch(
                            this.nodes[n],
                            // findCond
                            (MiniTile miniTile, WalkPosition w) -> (
                                miniTile.getAreaId().equals(area.getId())
                                    && map.getData()
                                    .getTile(w.toTilePosition(), CheckMode.NO_CHECK)
                                    .getNeutral() == null),
                            // visitCond
                            (MiniTile miniTile, WalkPosition w) -> (
                                miniTile.getAreaId().equals(area.getId())
                                    || (isBlocked()
                                    && (miniTile.isBlocked()
                                    || map.getData()
                                    .getTile(w.toTilePosition(), CheckMode.NO_CHECK)
                                    .getNeutral() != null))));

                /*
                 * Note: In the original C++ code, "nodeInArea" is a reference to a "WalkPosition" in
                 * "nodesInArea" which changes! Change that object here (after the call to
                 * "breadthFirstSearch")...
                 */
                final WalkPosition left = nodesInArea.get(n).getLeft();
                final WalkPosition right = nodesInArea.get(n).getRight();
                final Pair<WalkPosition, WalkPosition> replacementPair =
                        new Pair<>(left, right);
                if (area.equals(this.areas.getLeft())) {
                    replacementPair.setLeft(nodeInArea);
                } else {
                    replacementPair.setRight(nodeInArea);
                }
                this.nodesInArea.set(n, replacementPair);
            }
        }
    }

    ChokePoint(
        final Graph graph,
        final int index,
        final Area area1,
        final Area area2,
        final List<WalkPosition> geometry) {
        this(graph, index, area1, area2, geometry, null);
    }

    private BWMap getMap() {
        return this.graph.getMap();
    }

    private Graph getGraph() {
        return this.graph;
    }

    /**
     * Tells whether this ChokePoint is a pseudo ChokePoint, i.e., it was created on top of a blocking
     * Neutral.
     */
    public boolean isPseudo() {
        return this.isPseudo;
    }

    /**
     * Returns the two areas of this ChokePoint.
     */
    public Pair<Area, Area> getAreas() {
        return this.areas;
    }

    /**
     * Returns the center of this ChokePoint.
     */
    public WalkPosition getCenter() {
        return getNodePosition(Node.MIDDLE);
    }

    /**
     * Returns the position of one of the 3 nodes of this ChokePoint (Cf. node definition).<br>
     * - Note: the returned value is contained in geometry()
     */
    public WalkPosition getNodePosition(final Node node) {
        if (!(node.ordinal() < Node.NODE_COUNT.ordinal())) {
            graph.getMap().asserter.throwIllegalStateException("");
        }
        return this.nodes[node.ordinal()];
    }

    /**
     * Pretty much the same as pos(n), except that the returned MiniTile position is guaranteed to be
     * part of pArea. That is: BWMap::getArea(positionOfNodeInArea(n, pArea)) == pArea.
     */
    public WalkPosition getNodePositionInArea(final Node node, final Area area) {
        if (!(area.equals(this.areas.getLeft()) || area.equals(this.areas.getRight()))) {
            graph.getMap().asserter.throwIllegalStateException("");
        }
        return area.equals(areas.getLeft())
                ? this.nodesInArea.get(node.ordinal()).getLeft()
                : this.nodesInArea.get(node.ordinal()).getRight();
    }

    /**
     * Returns the set of positions that defines the shape of this ChokePoint.<br>
     * - Note: none of these miniTiles actually belongs to this ChokePoint (a ChokePoint doesn't
     * contain any MiniTile). They are however guaranteed to be part of one of the 2 areas.<br>
     * - Note: the returned set contains pos(middle), pos(END_1) and pos(END_2). If isPseudo(),
     * returns {p} where p is the position of a walkable MiniTile near from blockingNeutral()->pos().
     */
    public List<WalkPosition> getGeometry() {
        return this.geometry;
    }

    /**
     * If !isPseudo(), returns false. Otherwise, returns whether this ChokePoint is considered
     * blocked. Normally, a pseudo ChokePoint either remains blocked, or switches to not isBlocked
     * when blockingNeutral() is destroyed and there is no remaining Neutral stacked with it. However,
     * in the case where BWMap::automaticPathUpdate() == false, blocked() will always return true
     * whatever blockingNeutral() returns. Cf. Area::AccessibleNeighbors().
     */
    public boolean isBlocked() {
        return this.isBlocked;
    }

    /**
     * If !isPseudo(), returns nullptr. Otherwise, returns a pointer to the blocking Neutral on top of
     * which this pseudo ChokePoint was created, unless this blocking Neutral has been destroyed. In
     * this case, returns a pointer to the next blocking Neutral that was stacked at the same
     * location, or nullptr if no such Neutral exists.
     */
    public Neutral getBlockingNeutral() {
        return this.blockingNeutral;
    }

    /**
     * If accessibleFrom(cp) == false, returns -1. Otherwise, returns the ground distance in pixels
     * between center() and cp->center(). - Note: if this == cp, returns 0.<br>
     * - Time complexity: O(1)<br>
     * - Note: Corresponds to the length in pixels of getPathTo(cp). So it suffers from the same lack
     * of accuracy. In particular, the value returned tends to be slightly higher than expected when
     * getPathTo(cp).size() is high.
     */
    public int distanceFrom(final ChokePoint chokePoint) {
        return getGraph().distance(this, chokePoint);
    }

    /**
     * Returns whether this ChokePoint is accessible from cp (through a walkable path).<br>
     * - Note: the relation is symmetric: this->accessibleFrom(cp) == cp->accessibleFrom(this)<br>
     * - Note: if this == cp, returns true.<br>
     * - Time complexity: O(1)<br>
     */
    public boolean accessibleFrom(final ChokePoint chokePoint) {
        return (distanceFrom(chokePoint) >= 0);
    }

    /**
     * Returns a list of getChokePoints, which is intended to be the shortest walking path from this
     * ChokePoint to cp. The path always starts with this ChokePoint and ends with cp, unless
     * accessibleFrom(cp) == false. In this case, an empty list is returned.<br>
     * - Note: if this == cp, returns [cp].<br>
     * Time complexity: O(1)<br>
     * To get the length of the path returned in pixels, use distanceFrom(cp).<br>
     * - Note: all the possible Paths are precomputed during BWMap::initialize().<br>
     * The best one is then stored for each pair of getChokePoints. However, only the center of the
     * getChokePoints is considered. As a consequence, the returned path may not be the shortest one.
     */
    public CPPath getPathTo(final ChokePoint cp) {
        return getGraph().getPath(this, cp);
    }

    void onBlockingNeutralDestroyed(final Neutral pBlocking) {
        if (pBlocking == null) {
            throw new IllegalStateException();
        }
        if (!pBlocking.isBlocking()) {
            graph.getMap().asserter.throwIllegalStateException("");
        }

        if (pBlocking.equals(this.blockingNeutral)) {
            // Ensures that in the case where several neutrals are stacked, blockingNeutral points to the
            // bottom one:
            this.blockingNeutral =
                    getMap().getData().getTile(this.blockingNeutral.getTopLeft()).getNeutral();

            if (this.blockingNeutral == null && getGraph().getMap().automaticPathUpdate()) {
                this.isBlocked = false;
            }
        }
    }

    int getIndex() {
        return this.index;
    }

    ChokePoint getPathBackTrace() {
        return this.pathBackTrace;
    }

    void setPathBackTrace(final ChokePoint pathBackTrace) {
        this.pathBackTrace = pathBackTrace;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof ChokePoint)) {
            return false;
        } else {
            final ChokePoint that = (ChokePoint) object;
            final boolean lel = this.areas.getLeft().equals(that.areas.getLeft());
            final boolean ler = this.areas.getLeft().equals(that.areas.getRight());
            final boolean rer = this.areas.getRight().equals(that.areas.getRight());
            final boolean rel = this.areas.getRight().equals(that.areas.getLeft());
            return lel && rer
                    || ler && rel; /* true if area pairs are an exact match or if one pair is reversed. */
        }
    }

    @Override
    public int hashCode() {
        int idLeft = areas.getLeft().getId().intValue();
        int idRight = areas.getRight().getId().intValue();
        if (idLeft > idRight) {
            final int idLeftTmp = idLeft;
            idLeft = idRight;
            idRight = idLeftTmp;
        }
        return Objects.hash(idLeft, idRight);
    }

    /**
     * ChokePoint::middle denotes the "middle" MiniTile of Geometry(), while ChokePoint::END_1 and
     * ChokePoint::END_2 denote its "ends". It is guaranteed that, among all the MiniTiles of
     * Geometry(), ChokePoint::middle has the highest altitude value (Cf. MiniTile::Altitude()).
     */
    public enum Node {
        END1,
        MIDDLE,
        END2,
        NODE_COUNT
    }
}
