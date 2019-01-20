package ecgberht.Agents;

import bwapi.*;
import bwem.Base;
import bwem.area.Area;
import ecgberht.BuildingMap;
import ecgberht.IntelligenceAgency;
import ecgberht.Simulation.SimInfo;
import ecgberht.UnitInfo;
import ecgberht.Util.Util;
import ecgberht.Util.UtilMicro;
import org.bk.ass.path.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static ecgberht.Ecgberht.getGs;

// Based on SteamHammer worker scout management, props to @JayScott
public class WorkerScoutAgent extends Agent {
    private Unit unit;
    private int currentVertex;
    private List<Position> enemyBaseBorders = new ArrayList<>();
    private Base enemyBase;
    private Status status = Status.IDLE;
    private int enemyNaturalIndex = -1;
    private Unit disrupter = null;
    private Unit proxier = null;
    private boolean stoppedDisrupting = false;
    private SimInfo mySim;
    List<TilePosition> validTiles = new ArrayList<>();
    private boolean removedIndex = false;
    private boolean ableToProxy = false;
    private TilePosition proxyTile = null;

    public WorkerScoutAgent(Unit unit, Base enemyBase) {
        this.unit = unit;
        this.unitInfo = getGs().unitStorage.getAllyUnits().get(unit);
        this.enemyBase = enemyBase;
        this.myUnit = unit;
        canProxyInThisMap();
    }

    private void canProxyInThisMap() { // TODO dont build near enemy main chokepoint or path from his depot to choke
        Area enemyArea = this.enemyBase.getArea();
        Set<TilePosition> tilesArea = getGs().map.getTilesArea(enemyArea);
        if (tilesArea == null) return;
        Result path = getGs().silentCartographer.getWalkablePath(enemyBase.getLocation().toWalkPosition(), getGs().enemyNaturalBase.getLocation().toWalkPosition());
        for (TilePosition t : tilesArea) {
            if (!getGs().map.tileBuildable(t, UnitType.Terran_Factory)) continue;
            if (t.getDistance(Util.getUnitCenterPosition(enemyBase.getLocation().toPosition(), UnitType.Zerg_Hatchery).toTilePosition()) <= 13)
                continue;
            if (enemyBase.getGeysers().stream().anyMatch(u -> t.getDistance(u.getCenter().toTilePosition()) <= 9))
                continue;
            if (path.path.stream().anyMatch(u -> t.getDistance(new WalkPosition(u.x, u.y).toTilePosition()) <= 10))
                continue;
            validTiles.add(t);
        }
        if (validTiles.isEmpty()) return;
        double bestDist = 0.0;
        for (TilePosition p : validTiles) {
            double dist = p.getDistance(enemyBase.getLocation());
            if (dist > bestDist) {
                bestDist = dist;
                proxyTile = p;
            }
        }
        if (proxyTile != null) ableToProxy = true;
    }

    public boolean runAgent() {
        if (unit == null || !unit.exists() || unitInfo == null) {
            if (disrupter != null) getGs().disrupterBuilding = disrupter;
            getGs().firstScout = false;
            if(getGs().proxyBuilding != null && !getGs().proxyBuilding.isCompleted()) getGs().proxyBuilding.cancelConstruction();
            return true;
        }
        if (status == Status.EXPLORE && getGs().getStrat().proxy && mySim.allies.stream().anyMatch(u -> u.unitType == UnitType.Terran_Marine)) {
            getGs().myArmy.add(unitInfo);
            getGs().firstScout = false;
            if(getGs().proxyBuilding != null && !getGs().proxyBuilding.isCompleted()) getGs().proxyBuilding.cancelConstruction();
            return true;
        }
        /*for(TilePosition p : validTiles){
            getGs().bw.getMapDrawer().drawCircleMap(p.toPosition(), 8, Color.YELLOW, true);
        }*/
        if (enemyBaseBorders.isEmpty()) updateBorders();
        mySim = getGs().sim.getSimulation(unitInfo, SimInfo.SimType.GROUND);
        if (enemyNaturalIndex != -1 && (IntelligenceAgency.getEnemyStrat() == IntelligenceAgency.EnemyStrats.EarlyPool
                || IntelligenceAgency.getEnemyStrat() == IntelligenceAgency.EnemyStrats.ZealotRush
                || getGs().learningManager.isNaughty() || getGs().basicCombatUnitsDetected(mySim.enemies)
                || IntelligenceAgency.getNumEnemyBases(getGs().bw.enemy()) > 1)) {
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
            case PROXYING:
                proxy();
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

    private void proxy() {
        if (proxier == null) {
            if (unit.getBuildUnit() != null) {
                proxier = unit.getBuildUnit();
                return;
            }
            if (unit.getOrder() != Order.PlaceBuilding) {
                if (proxyTile.getDistance(unitInfo.tileposition) <= 3) unit.build(UnitType.Terran_Factory, proxyTile);
                else UtilMicro.move(unit, proxyTile.toPosition());
            }
        }
    }

    private void disrupt() {
        if (disrupter == null) {
            if (unit.getBuildUnit() != null) {
                disrupter = unit.getBuildUnit();
                return;
            }
            if (unit.getOrder() != Order.PlaceBuilding) {
                unit.build(UnitType.Terran_Engineering_Bay, getGs().enemyNaturalBase.getLocation());
            }
        } else if (disrupter.getRemainingBuildTime() <= 25) {
            unit.haltConstruction();
            stoppedDisrupting = true;
            removedIndex = true;
        } else if (mySim.enemies.stream().anyMatch(u -> u.unit.getDistance(unit) <= 4 * 32)) {
            if (mySim.enemies.stream().anyMatch(u -> u.unitType == UnitType.Zerg_Zergling)) {
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
                UnitInfo closest = mySim.enemies.iterator().next();
                Area enemyArea = getGs().bwem.getMap().getArea(closest.tileposition);
                if (closest.unitType == UnitType.Zerg_Drone && enemyArea != null && enemyArea.equals(getGs().enemyNaturalArea)) {
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
        } else unit.rightClick(disrupter);
    }

    private Status chooseNewStatus() {
        if(stoppedDisrupting || !getGs().firstScout) return Status.EXPLORE;
        if (status == Status.DISRUPTING) return Status.DISRUPTING;
        if (status == Status.PROXYING) {
            if (proxyTile == null) {
                ableToProxy = false;
            } else {
                if (proxier != null && proxier.isCompleted()) {
                    ableToProxy = false;
                    getGs().proxyBuilding = proxier;
                } else {
                    double dist = unitInfo.tileposition.getDistance(proxyTile);
                    if (dist <= 3) {
                        if (!unitInfo.attackers.isEmpty()) {
                            if (proxier != null && proxier.isBeingConstructed()) {
                                unit.haltConstruction();
                                ableToProxy = false;
                                proxier.cancelConstruction();
                                proxier = null;
                                return Status.EXPLORE;
                            }
                        }
                    }
                    return Status.PROXYING;
                }
            }
        }
        String strat = getGs().getStrat().name;
        if (getGs().luckyDraw >= 0.7 && ableToProxy && strat.equals("TwoPortWraith") && !getGs().learningManager.isNaughty() && !getGs().MBs.isEmpty() && !getGs().refineriesAssigned.isEmpty()) {
            return Status.PROXYING;
        }
        if (getGs().luckyDraw >= 0.35 || strat.equals("BioGreedyFE") || strat.equals("MechGreedyFE")
                || strat.equals("BioMechGreedyFE") || strat.equals("ProxyBBS") || strat.equals("ProxyEightRax") || getGs().learningManager.isNaughty())
            return Status.EXPLORE;
        if (getGs().enemyRace != Race.Zerg || stoppedDisrupting) return Status.EXPLORE;
        if (IntelligenceAgency.getNumEnemyBases(getGs().bw.enemy()) == 1 && currentVertex == enemyNaturalIndex) {
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
        if (currentVertex == enemyNaturalIndex && getGs().bw.isVisible(getGs().enemyNaturalBase.getLocation())) {
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
                    (!right.isValid(getGs().bw) || getGs().bwem.getMap().getArea(right) != enemyRegion || !getGs().bw.isBuildable(right))
                            || (!bottom.isValid(getGs().bw) || getGs().bwem.getMap().getArea(bottom) != enemyRegion || !getGs().bw.isBuildable(bottom))
                            || (!left.isValid(getGs().bw) || getGs().bwem.getMap().getArea(left) != enemyRegion || !getGs().bw.isBuildable(left))
                            || (!up.isValid(getGs().bw) || getGs().bwem.getMap().getArea(up) != enemyRegion || !getGs().bw.isBuildable(up));
            if (edge && getGs().bw.isBuildable(tp)) {
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
                if (vertex.isValid(getGs().bw) && getGs().bw.isWalkable(vertex.toWalkPosition()))
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

    enum Status {
        EXPLORE, DISRUPTING, IDLE, PROXYING;
    }

    public String statusToString() {
        if (status == Status.EXPLORE) return "Exploring";
        if (status == Status.DISRUPTING) return "Disrupting";
        if (status == Status.PROXYING) return "Proxying";
        if (status == Status.IDLE) return "Idle";
        return "None";
    }
}
