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
import org.openbw.bwapi4j.unit.*;

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
    private SimInfo mySim;
    private boolean removedIndex = false;

    public WorkerScoutAgent(Unit unit, Base enemyBase) {
        this.unit = (SCV) unit;
        this.enemyBase = enemyBase;
        this.myUnit = unit;
    }

    public boolean runAgent() {
        if (unit == null || !unit.exists()) {
            if (disrupter != null) getGs().disrupterBuilding = disrupter;
            return true;
        }
        if (getGs().strat.proxy && mySim.allies.stream().anyMatch(u -> u instanceof Marine)) {
            getGs().myArmy.add(unit);
            return true;
        }
        if (enemyBaseBorders.isEmpty()) updateBorders();
        mySim = getGs().sim.getSimulation(unit, SimInfo.SimType.GROUND);
        if (enemyNaturalIndex != -1 && (IntelligenceAgency.getEnemyStrat() == IntelligenceAgency.EnemyStrats.EarlyPool
                || IntelligenceAgency.getEnemyStrat() == IntelligenceAgency.EnemyStrats.ZealotRush
                || getGs().learningManager.isNaughty() || getGs().basicCombatUnitsDetected(mySim.enemies)
                || IntelligenceAgency.getNumEnemyBases(getGs().getIH().enemy()) > 1)) {
            enemyBaseBorders.remove(enemyNaturalIndex);
            enemyNaturalIndex = -1;
            removedIndex = true;
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
        if (stoppedDisrupting && disrupter != null && disrupter.getHitPoints() <= 20) {
            disrupter.cancelConstruction();
            disrupter = null;
        }
    }

    private void disrupt() {
        if (disrupter == null) {
            if (unit.getBuildUnit() != null) {
                disrupter = (Building) unit.getBuildUnit();
                return;
            }
            if (unit.getOrder() != Order.PlaceBuilding) {
                unit.build(getGs().enemyNaturalBase.getLocation(), UnitType.Terran_Engineering_Bay);
            }
        } else if (disrupter.getRemainingBuildTime() <= 25) {
            unit.haltConstruction();
            stoppedDisrupting = true;
            removedIndex = true;
        } else if (mySim.enemies.stream().anyMatch(u -> u.getDistance(unit) <= 4 * 32)) {
            if (mySim.enemies.stream().anyMatch(u -> u instanceof Zergling)) {
                unit.haltConstruction();
                stoppedDisrupting = true;
                if (!removedIndex) {
                    enemyBaseBorders.remove(enemyNaturalIndex);
                    enemyNaturalIndex = -1;
                    removedIndex = true;
                }
                return;
            }
            if (mySim.enemies.size() == 1) {
                Unit closest = mySim.enemies.iterator().next();
                Area enemyArea = getGs().bwem.getMap().getArea(closest.getTilePosition());
                if (closest instanceof Drone && enemyArea != null && enemyArea.equals(getGs().enemyNaturalArea)) {
                    if (mySim.lose) {
                        unit.haltConstruction();
                        stoppedDisrupting = true;
                        if (!removedIndex) {
                            enemyBaseBorders.remove(enemyNaturalIndex);
                            enemyNaturalIndex = -1;
                            removedIndex = true;
                        }
                    } else UtilMicro.attack(unit, mySim.enemies.iterator().next()); // TODO add attack state
                }
            }
        } else unit.resumeBuilding(disrupter);
    }

    private Status chooseNewStatus() {
        String strat = getGs().strat.name;
        if (getGs().luckyDraw >= 0.35 || strat.equals("BioGreedyFE") || strat.equals("MechGreedyFE")
                || strat.equals("BioMechGreedyFE") || strat.equals("ProxyBBS") || strat.equals("ProxyEightRax") || getGs().learningManager.isNaughty())
            return Status.EXPLORE;
        if (getGs().enemyRace != Race.Zerg || stoppedDisrupting) return Status.EXPLORE;
        if (status == Status.DISRUPTING) return Status.DISRUPTING;
        if (IntelligenceAgency.getNumEnemyBases(getGs().getIH().enemy()) == 1 && currentVertex == enemyNaturalIndex) {
            return Status.DISRUPTING;
        }
        return Status.EXPLORE;
    }

    private Position getNextPosition() {
        if (currentVertex == -1) {
            int closestPolygonIndex = getClosestVertexIndex();
            if (closestPolygonIndex == -1) return getGs().getPlayer().getStartLocation().toPosition();
            currentVertex = closestPolygonIndex;
            return enemyBaseBorders.get(closestPolygonIndex);
        }
        if (currentVertex == enemyNaturalIndex && getGs().getGame().getBWMap().isVisible(getGs().enemyNaturalBase.getLocation())) {
            currentVertex = (currentVertex + 1) % enemyBaseBorders.size();
            return enemyBaseBorders.get(currentVertex);
        }
        double distanceFromCurrentVertex = enemyBaseBorders.get(currentVertex).getDistance(unit.getPosition());
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
        UtilMicro.move(unit, getNextPosition());
    }

    private void updateBorders() {
        final Area enemyRegion = enemyBase.getArea();
        if (enemyRegion == null) return;
        final Position enemyCenter = enemyBase.getLocation().toPosition().add(new Position(64, 48));
        final List<TilePosition> closestTobase = new ArrayList<>(BuildingMap.tilesArea.get(enemyRegion));
        List<Position> unsortedVertices = new ArrayList<>();
        for (TilePosition tp : closestTobase) {
            if (getGs().bwem.getMap().getArea(tp) != enemyRegion) continue;
            TilePosition right = new TilePosition(tp.getX() + 1, tp.getY());
            TilePosition bottom = new TilePosition(tp.getX(), tp.getY() + 1);
            TilePosition left = new TilePosition(tp.getX() - 1, tp.getY());
            TilePosition up = new TilePosition(tp.getX(), tp.getY() - 1);
            final boolean edge =
                    (!getGs().getGame().getBWMap().isValidPosition(right) || (getGs().bwem.getMap().getArea(right) != enemyRegion || !getGs().getGame().getBWMap().isBuildable(right)))
                            || (!getGs().getGame().getBWMap().isValidPosition(bottom) || (getGs().bwem.getMap().getArea(bottom) != enemyRegion || !getGs().getGame().getBWMap().isBuildable(bottom)))
                            || (!getGs().getGame().getBWMap().isValidPosition(left) || (getGs().bwem.getMap().getArea(left) != enemyRegion || !getGs().getGame().getBWMap().isBuildable(left)))
                            || (!getGs().getGame().getBWMap().isValidPosition(up) || (getGs().bwem.getMap().getArea(up) != enemyRegion || !getGs().getGame().getBWMap().isBuildable(up)));
            if (edge && getGs().getGame().getBWMap().isBuildable(tp)) {
                Position vertex = tp.toPosition().add(new Position(16, 16));
                double dist = enemyCenter.getDistance(vertex);
                if (dist > 368.0) {
                    double pullBy = Math.min(dist - 368.0, 120.0);
                    if (vertex.getX() == enemyCenter.getX()) {
                        vertex = vertex.add(new Position(0, vertex.getY() > enemyCenter.getY() ? (int) (-pullBy) : (int) pullBy));
                    } else {
                        double m = (double) (enemyCenter.getY() - vertex.getY()) / (double) (enemyCenter.getX() - vertex.getX());
                        double x = vertex.getX() + (vertex.getX() > enemyCenter.getX() ? -1.0 : 1.0) * pullBy / (Math.sqrt(1 + m * m));
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

        int distanceThreshold = 100;
        while (true) {
            int maxFarthest = 0;
            int maxFarthestStart = 0;
            int maxFarthestEnd = 0;
            for (int i = 0; i < sortedVertices.size(); ++i) {
                int farthest = 0;
                int farthestIndex = 0;
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
            if (maxFarthest < 4) break;
            List<Position> temp = new ArrayList<>();
            for (int s = maxFarthestEnd; s != maxFarthestStart; s = (s + 1) % sortedVertices.size()) {
                temp.add(temp.size(), sortedVertices.get(s));
            }
            sortedVertices = temp;
        }
        enemyBaseBorders = sortedVertices;
        /*double bestDist = 1000000;
        for (int i = 0; i < sortedVertices.size(); i++) {
            double dist = sortedVertices.get(i).getDistance(enemyCenter);
            if (dist < bestDist) {
                bestDist = dist;
                currentVertex = i;
            }
        }*/
        currentVertex = 0;
        if (!getGs().learningManager.isNaughty()) {
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
        } else {
            enemyNaturalIndex = -1;
            removedIndex = true;
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
