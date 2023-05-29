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
import org.openbw.bwapi4j.unit.Unit;

import java.util.ArrayList;
import java.util.List;

/**
 * Neutral is the abstract base class for a small hierarchy of wrappers around some BWAPI::Units<br>
 * The units concerned are the Resources (Minerals and Geysers) and the static Buildings.<br>
 * Stacked Neutrals are supported, provided they share the same type at the same location.
 */
public abstract class Neutral {
    private final Unit bwapiUnit;
    private final Position pos;
    protected TilePosition topLeft;
    private final TilePosition tileSize;
    private final BWMap map;
    private Neutral nextStacked = null;
    private List<WalkPosition> blockedAreas = new ArrayList<>();

    Neutral(final Unit unit, final BWMap map) {
        this.bwapiUnit = unit;
        this.map = map;
        this.pos = unit.getInitialPosition();
        this.topLeft = unit.getInitialTilePosition();
        this.tileSize = unit.getType().tileSize();

        putOnTiles();
    }

    void simulateCPPObjectDestructor() {
        removeFromTiles();

        if (isBlocking()) {
            BWMapInitializer map = (BWMapInitializer) getMap();
            map.onBlockingNeutralDestroyed(this);
        }
    }

    /**
     * Returns the BWAPI::Unit this Neutral is wrapping around.
     */
    public Unit getUnit() {
        return this.bwapiUnit;
    }

    /**
     * Returns the center of this Neutral, in pixels (same as unit()->getInitialPosition()).
     */
    public Position getCenter() {
        return this.pos;
    }

    /**
     * Returns the top left Tile position of this Neutral (same as unit()->getInitialTilePosition()).
     */
    public TilePosition getTopLeft() {
        return this.topLeft;
    }

    /**
     * Returns the bottom right Tile position of this Neutral
     */
    public TilePosition getBottomRight() {
        return this.topLeft.add(this.tileSize).subtract(new TilePosition(1, 1));
    }

    /**
     * Returns the size of this Neutral, in Tiles (same as Type()->tileSize())
     */
    public TilePosition getSize() {
        return this.tileSize;
    }

    /**
     * Tells whether this Neutral is blocking some ChokePoint.<br>
     * - This applies to minerals and StaticBuildings only.<br>
     * - For each blocking Neutral, a pseudo ChokePoint (which is blocked()) is created on top of it,
     * with the exception of stacked blocking Neutrals for which only one pseudo ChokePoint is
     * created.<br>
     * - Cf. definition of pseudo getChokePoints in class ChokePoint comment.<br>
     * - Cf. ChokePoint::blockingNeutral and ChokePoint::blocked.
     */
    public boolean isBlocking() {
        return !this.blockedAreas.isEmpty();
    }

    void setBlocking(final List<WalkPosition> blockedAreas) {
        if (!(this.blockedAreas.isEmpty() && !blockedAreas.isEmpty())) {
            map.asserter.throwIllegalStateException("");
        }
        this.blockedAreas = blockedAreas;
    }

    /**
     * If blocking() == true, returns the set of areas blocked by this Neutral.
     */
    public List<Area> getBlockedAreas() {
        final List<Area> blockedAreas = new ArrayList<>();
        for (final WalkPosition w : this.blockedAreas) {
            if (getMap().getArea(w) != null) {
                blockedAreas.add(getMap().getArea(w));
            }
        }
        return blockedAreas;
    }

    /**
     * Returns the next Neutral stacked over this Neutral, if ever.<br>
     * - To iterate through the whole stack, one can use the following:<br>
     * <code>for (const Neutral * n = BWMap::GetTile(topLeft()).GetNeutral() ; n ; n = n->nextStacked())
     * </code>
     */
    public Neutral getNextStacked() {
        return this.nextStacked;
    }

    /**
     * Returns the last Neutral stacked over this Neutral, if ever.
     */
    public Neutral getLastStacked() {
        Neutral topNeutral = this;
        while (topNeutral.getNextStacked() != null) {
            topNeutral = topNeutral.getNextStacked();
        }
        return topNeutral;
    }

    private boolean isSameUnitTypeAs(Neutral neutral) {
        return this.getUnit().getType() == neutral.getUnit().getType();
    }

    private void putOnTiles() {
        if (getNextStacked() != null) {
            map.asserter.throwIllegalStateException("");
        }

        for (int dy = 0; dy < getSize().getY(); ++dy)
            for (int dx = 0; dx < getSize().getX(); ++dx) {
                final Tile deltaTile = getMap().getData()
                        .getTile(getTopLeft().add(new TilePosition(dx, dy)));
                if (deltaTile.getNeutral() == null) {
                    deltaTile.addNeutral(this);
                } else {
                    final Neutral topNeutral = deltaTile.getNeutral().getLastStacked();
                    // https://github.com/N00byEdge/BWEM-community/issues/30#issuecomment-400840140
                    if (!isTopNeutralValid(topNeutral)) continue;
                    if (handleStackedNeutral(dy, dx, deltaTile, topNeutral)) return;
                }
            }
    }

    private boolean handleStackedNeutral(int dy, int dx, Tile deltaTile, Neutral topNeutral) {
        if (this.equals(deltaTile.getNeutral())
                || this.equals(topNeutral)
                || topNeutral.getClass().getName().equals(Geyser.class.getName())) {
            map.asserter.throwIllegalStateException("");
        } else if (!topNeutral.isSameUnitTypeAs(this)) {
            //                    bwem_assert_plus(pTop->Type() == Type(), "stacked neutrals have
            map.asserter.throwIllegalStateException(
                    "Stacked Neutral objects have different types: top="
                            + topNeutral.getClass().getName()
                            + ", this="
                            + this.getClass().getName());
        } else if (!(topNeutral.getTopLeft().equals(getTopLeft()))) {
            //                    bwem_assert_plus(pTop->topLeft() == topLeft(), "stacked neutrals
            map.asserter.throwIllegalStateException(
                    "Stacked Neutral objects not aligned: top="
                            + topNeutral.getTopLeft().toString()
                            + ", this="
                            + getTopLeft().toString());
        } else if (!(dx == 0 && dy == 0)) {
            map.asserter.throwIllegalStateException("");
        } else {
            topNeutral.nextStacked = this;
            return true;
        }
        return false;
    }

    private boolean isTopNeutralValid(Neutral topNeutral) {
        return topNeutral.getTopLeft().equals(getTopLeft()) && topNeutral.getBottomRight().equals(getBottomRight());
    }

    /**
     * Warning: Do not use this function outside of BWEM's internals. This method is designed to be
     * called from the "~Neutral" destructor in C++.
     */
    private void removeFromTiles() {
        for (int dy = 0; dy < getSize().getY(); ++dy)
            for (int dx = 0; dx < getSize().getX(); ++dx) {
                final Tile tile = getMap().getData()
                        .getTile(getTopLeft().add(new TilePosition(dx, dy)));
                if (tile.getNeutral() == null) {
                    map.asserter.throwIllegalStateException("");
                }

                if (tile.getNeutral().equals(this)) {
                    tile.removeNeutral(this);
                    if (this.nextStacked != null) {
                        tile.addNeutral(this.nextStacked);
                    }
                } else {
                    Neutral prevStacked = tile.getNeutral();
                    while (prevStacked != null && !this.equals(prevStacked.getNextStacked())) {
                        prevStacked = prevStacked.getNextStacked();
                    }
                    if (!(dx == 0 && dy == 0)) {
                        map.asserter.throwIllegalStateException("");
                    }
                    if (prevStacked != null) {
                        if (!prevStacked.isSameUnitTypeAs(this) || !(prevStacked
                                .getTopLeft().equals(getTopLeft()))) {
                            map.asserter.throwIllegalStateException("");
                        }
                        prevStacked.nextStacked = nextStacked;
                    }
                    this.nextStacked = null;
                    return;
                }
            }

        this.nextStacked = null;
    }

    private BWMap getMap() {
        return this.map;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof Neutral)) {
            return false;
        } else {
            final Neutral that = (Neutral) object;
            return (getUnit().getId() == that.getUnit().getId());
        }
    }

    @Override
    public int hashCode() {
        return getUnit().hashCode();
    }
}
