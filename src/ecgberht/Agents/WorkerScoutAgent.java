package ecgberht.Agents;

import bwem.Base;
import bwem.area.Area;
import ecgberht.BuildingMap;
import ecgberht.IntelligenceAgency;
import ecgberht.Simulation.SimInfo;
import ecgberht.Util.Util;
import ecgberht.Util.UtilMicro;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.Unit;

import java.util.ArrayList;
import java.util.List;

import static ecgberht.Ecgberht.getGs;

// Based on SteamHammer worker scout management, props to @JayScott
public class WorkerScoutAgent extends Agent {
    private SCV unit;
    private int currentVertex;
    private List<Position> enemyBaseBorders = new ArrayList<>();
    private Base enemyBase;
    private Status status = Status.IDLE;
    private int enemyNaturalIndex = -1;
    private Building disrupter = null;
    private boolean stoppedDisrupting = false;

    public WorkerScoutAgent(Unit unit, Base enemyBase) {
        this.unit = (SCV) unit;
        this.enemyBase = enemyBase;
        this.myUnit = unit;
    }

    public boolean runAgent() {
        if (unit == null || !unit.exists()){
            if(disrupter != null) getGs().disrupterBuilding = disrupter;
            return true;
        }
        if (enemyBaseBorders.isEmpty()) updateBorders();
        if (enemyNaturalIndex != -1 && (IntelligenceAgency.getEnemyStrat() == IntelligenceAgency.EnemyStrats.EarlyPool || getGs().EI.naughty)) {
            enemyBaseBorders.remove(enemyNaturalIndex);
            enemyNaturalIndex = -1;
        }
        status = chooseNewStatus();
        cancelDisrupter();
        switch (status) {
            case EXPLORE:
                followPerimeter();
                break;
            case DISRUPTING:
                disrupt();
                break;
            case IDLE:
                break;
        }
        return false;
    }

    private void cancelDisrupter() {
        if(stoppedDisrupting && disrupter != null && disrupter.getHitPoints() <= 20){
            disrupter.cancelConstruction();
            disrupter = null;
        }
    }

    private void disrupt() {
        if(disrupter == null){
            if(unit.getBuildUnit() != null){
                disrupter = (Building) unit.getBuildUnit();
                return;
            }
            if(unit.getOrder() != Order.PlaceBuilding){
                unit.build(getGs().enemyNaturalBase.getLocation(), UnitType.Terran_Engineering_Bay);
            }
        }
        else{
            if (disrupter.getRemainingBuildTime() <= 25 || enemiesAreClose()) {
                unit.haltConstruction();
                stoppedDisrupting = true;
            }
        }
    }

    private boolean enemiesAreClose() {
        if(unit.isUnderAttack()) return true;
        for(Unit u : getGs().sim.getSimulation(unit, SimInfo.SimType.GROUND).enemies){
            if(u.getDistance(unit) < 4 * 32) return true;
        }
        return false;
    }

    private Status chooseNewStatus() {
        if(getGs().enemyRace != Race.Zerg || stoppedDisrupting) return Status.EXPLORE;
        if(status == Status.DISRUPTING) return Status.DISRUPTING;
        if(IntelligenceAgency.getNumEnemyBases(getGs().getIH().enemy()) == 1 && currentVertex == enemyNaturalIndex){
            return Status.DISRUPTING;
        }
        return Status.EXPLORE;
    }

    private Position getFleePosition() {
        // if this is the first flee, we will not have a previous perimeter index
        if (currentVertex == -1) {
            // so return the closest position in the polygon
            int closestPolygonIndex = getClosestVertexIndex();
            if (closestPolygonIndex == -1) return getGs().getPlayer().getStartLocation().toPosition();
            // set the current index so we know how to iterate if we are still fleeing later
            currentVertex = closestPolygonIndex;
            return enemyBaseBorders.get(closestPolygonIndex);
        }
        if (currentVertex == enemyNaturalIndex && getGs().getGame().getBWMap().isVisible(getGs().enemyNaturalBase.getLocation())) {
            currentVertex = (currentVertex + 1) % enemyBaseBorders.size();
            return enemyBaseBorders.get(currentVertex);
        }
        // if we are still fleeing from the previous frame, get the next location if we are close enough
        double distanceFromCurrentVertex = enemyBaseBorders.get(currentVertex).getDistance(unit.getPosition());
        // keep going to the next vertex in the perimeter until we get to one we're far enough from to issue another move command
        while (distanceFromCurrentVertex < 128) {
            currentVertex = (currentVertex + 1) % enemyBaseBorders.size();
            distanceFromCurrentVertex = enemyBaseBorders.get(currentVertex).getDistance(unit.getPosition());
        }
        return enemyBaseBorders.get(currentVertex);
    }

    private int getClosestVertexIndex() {
        int chosen = -1;
        double distMax = Double.MAX_VALUE;
        for (int ii = 0; ii < enemyBaseBorders.size(); ii++) {
            double dist = enemyBaseBorders.get(ii).getDistance(unit.getPosition());
            if (dist < distMax) {
                chosen = ii;
                distMax = dist;
            }
        }
        return chosen;
    }

    private void followPerimeter() {
        Position fleeTo = getFleePosition();
        UtilMicro.move(unit, fleeTo);
    }

    private void updateBorders() {
        final Area enemyRegion = enemyBase.getArea();
        if (enemyRegion == null) return;
        final Position enemyCenter = enemyBase.getLocation().toPosition().add(new Position(64, 48));
        final List<TilePosition> closestTobase = new ArrayList<>(BuildingMap.tilesArea.get(enemyRegion));
        List<Position> unsortedVertices = new ArrayList<>();
        // check each tile position
        for (TilePosition tp : closestTobase) {
            if (getGs().bwem.getMap().getArea(tp) != enemyRegion) continue;
            // a tile is 'on an edge' unless
            // 1) in all 4 directions there's a tile position in the current region
            // 2) in all 4 directions there's a buildable tile
            TilePosition right = new TilePosition(tp.getX() + 1, tp.getY());
            TilePosition bottom = new TilePosition(tp.getX(), tp.getY() + 1);
            TilePosition left = new TilePosition(tp.getX() - 1, tp.getY());
            TilePosition up = new TilePosition(tp.getX(), tp.getY() - 1);
            final boolean edge =
                    (!getGs().getGame().getBWMap().isValidPosition(right) || (getGs().bwem.getMap().getArea(right) != enemyRegion || !getGs().getGame().getBWMap().isBuildable(right)))
                            || (!getGs().getGame().getBWMap().isValidPosition(bottom) || (getGs().bwem.getMap().getArea(bottom) != enemyRegion || !getGs().getGame().getBWMap().isBuildable(bottom)))
                            || (!getGs().getGame().getBWMap().isValidPosition(left) || (getGs().bwem.getMap().getArea(left) != enemyRegion || !getGs().getGame().getBWMap().isBuildable(left)))
                            || (!getGs().getGame().getBWMap().isValidPosition(up) || (getGs().bwem.getMap().getArea(up) != enemyRegion || !getGs().getGame().getBWMap().isBuildable(up)));

            // push the tiles that aren't surrounded
            if (edge && getGs().getGame().getBWMap().isBuildable(tp)) {
                Position vertex = tp.toPosition().add(new Position(16, 16));
                // Pull the vertex towards the enemy base center, unless it is already within 12 tiles
                double dist = enemyCenter.getDistance(vertex);
                if (dist > 368.0) {
                    double pullBy = Math.min(dist - 368.0, 120.0);
                    // Special case where the slope is infinite
                    if (vertex.getX() == enemyCenter.getX()) {
                        vertex = vertex.add(new Position(0, vertex.getY() > enemyCenter.getY() ? (int) (-pullBy) : (int) pullBy));
                    } else {
                        // First get the slope, m = (y1 - y0)/(x1 - x0)
                        double m = (double) (enemyCenter.getY() - vertex.getY()) / (double) (enemyCenter.getX() - vertex.getX());
                        // Now the equation for a new x is x0 +- d/sqrt(1 + m^2)
                        double x = vertex.getX() + (vertex.getX() > enemyCenter.getX() ? -1.0 : 1.0) * pullBy / (Math.sqrt(1 + m * m));
                        // And y is m(x - x0) + y0
                        double y = m * (x - vertex.getX()) + vertex.getY();
                        vertex = new Position((int) x, (int) y);
                    }
                }
                unsortedVertices.add(vertex);
            }
        }
        List<Position> sortedVertices = new ArrayList<>();
        Position current = unsortedVertices.get(0);
        enemyBaseBorders.add(current);
        unsortedVertices.remove(current);
        // while we still have unsorted vertices left, find the closest one remaining to current
        while (!unsortedVertices.isEmpty()) {
            double bestDist = 1000000;
            Position bestPos = null;
            for (final Position pos : unsortedVertices) {
                double dist = pos.getDistance(current);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestPos = pos;
                }
            }
            current = bestPos;
            sortedVertices.add(sortedVertices.size(), bestPos);
            unsortedVertices.remove(bestPos);
        }

        // let's close loops on a threshold, eliminating death grooves
        int distanceThreshold = 100;
        while (true) {
            // find the largest index difference whose distance is less than the threshold
            int maxFarthest = 0;
            int maxFarthestStart = 0;
            int maxFarthestEnd = 0;
            // for each starting vertex
            for (int i = 0; i < sortedVertices.size(); ++i) {
                int farthest = 0;
                int farthestIndex = 0;
                // only test half way around because we'll find the other one on the way back
                for (int j = 1; j < sortedVertices.size() / 2; ++j) {
                    int jindex = (i + j) % sortedVertices.size();
                    if (sortedVertices.get(i).getDistance(sortedVertices.get(jindex)) < distanceThreshold) {
                        farthest = j;
                        farthestIndex = jindex;
                    }
                }
                if (farthest > maxFarthest) {
                    maxFarthest = farthest;
                    maxFarthestStart = i;
                    maxFarthestEnd = farthestIndex;
                }
            }
            // stop when we have no long chains within the threshold
            if (maxFarthest < 4) break;
            List<Position> temp = new ArrayList<>();
            for (int s = maxFarthestEnd; s != maxFarthestStart; s = (s + 1) % sortedVertices.size()) {
                temp.add(temp.size(), sortedVertices.get(s));
            }
            sortedVertices = temp;
        }
        enemyBaseBorders = sortedVertices;
        // Set the initial index to the vertex closest to the enemy main, so we get scouting information as soon as possible
        double bestDist = 1000000;
        for (int i = 0; i < sortedVertices.size(); i++) {
            double dist = sortedVertices.get(i).getDistance(enemyCenter);
            if (dist < bestDist) {
                bestDist = dist;
                currentVertex = i;
            }
        }
        Base enemyNatural = getGs().enemyNaturalBase;
        if (enemyNatural != null) {
            Position enemyNaturalPos = enemyNatural.getLocation().toPosition();
            int index = -1;
            double distMax = Double.MAX_VALUE;
            for (int ii = 0; ii < enemyBaseBorders.size(); ii++) {
                double dist = Util.getGroundDistance(enemyBaseBorders.get(ii), enemyNaturalPos);
                if (index == -1 || dist < distMax) {
                    index = ii;
                    distMax = dist;
                }
            }
            enemyBaseBorders.add(index, enemyNaturalPos);
            this.enemyNaturalIndex = index;
        }
    }

    enum Status {EXPLORE, DISRUPTING, IDLE}

    public String statusToString() {
        if (status == Status.EXPLORE) return "Exploring";
        if (status == Status.DISRUPTING) return "Disrupting";
        if (status == Status.IDLE) return "Idle";
        return "None";
    }
}
