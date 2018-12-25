package ecgberht.Clustering;


import ecgberht.UnitStorage;
import ecgberht.Util.Util;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.Building;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/*
Thanks to @Yegers for improving performance
*/
public class MeanShift {

    public long time = 0;
    private double radius;
    private List<UnitPos> points = new ArrayList<>();

    public MeanShift(Collection<UnitStorage.UnitInfo> units, double radius) {
        this.radius = Math.pow(radius, 2);
        for (UnitStorage.UnitInfo u : units) {
            if (u.unit instanceof Building && !Util.isStaticDefense(u.unit) && !u.visible) continue;
            Position p = u.position;
            this.points.add(new UnitPos(u, p.getX(), p.getY()));
        }
    }

    public List<Cluster> run(int iterations) {
        try {
            time = System.currentTimeMillis();
            int bandwidth = 2;
            for (int iter = 0; iter < iterations; iter++) {
                //System.out.println("-----Iter " + iter + "------");
                for (int i = 0; i < points.size(); i++) {
                    UnitPos aux = points.get(i);
                    double initialX = aux.x;
                    double initialY = aux.y;
                    List<double[]> neighbours = getNeighbours(aux.unit, initialX, initialY);
                    double numeratorX = 0;
                    double numeratorY = 0;
                    double denominator = 0;
                    for (double[] neighbour : neighbours) {
                        double distanceSquared = euclideanDistanceSquared(neighbour[0], neighbour[1], initialX, initialY);
                        double weight = gaussianKernel2(distanceSquared, bandwidth);
                        numeratorX += weight * neighbour[0];
                        numeratorY += weight * neighbour[1];
                        denominator += weight;
                    }
                    double newPointX = numeratorX / denominator;
                    double newPointY = numeratorY / denominator;
                    if (neighbours.isEmpty()) {
                        newPointX = initialX;
                        newPointY = initialY;
                    }
                    if (Double.isInfinite(newPointX) || Double.isNaN(newPointX)) newPointX = initialX;  // HACK
                    if (Double.isInfinite(newPointY) || Double.isNaN(newPointY)) newPointY = initialY; // HACK
                    //System.out.println("Original Point : " + initial + " , shifted point: " + newPoint);
                    points.set(i, new UnitPos(aux.unit, newPointX, newPointY));
                }
            }
            List<Cluster> clusters = new ArrayList<>();
            for (UnitPos i : points) {
                int c = 0;
                for (Cluster cluster : clusters) {
                    if (euclideanDistanceSquared(i.x, i.y, cluster.modeX, cluster.modeY) <= 400 * 400) break;
                    c++;
                }
                if (c == clusters.size()) {
                    Cluster cluster = new Cluster();
                    cluster.modeX = i.x;
                    cluster.modeY = i.y;
                    clusters.add(cluster);
                }
                clusters.get(c).units.add(i.unit);
                clusters.get(c).updateCentroid();
            }
            time = System.currentTimeMillis() - time;
            clusters.forEach(Cluster::updateCMaxDistFromCenter);
            return clusters;
        } catch (Exception e) {
            System.err.println("MeanShift run exception");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private double gaussianKernel2(double distanceSquared, double bandwidth) {
        return Math.exp(-1.0 / 2.0 * distanceSquared / (bandwidth * bandwidth));
    }

    private List<double[]> getNeighbours(UnitStorage.UnitInfo unit, double pointX, double pointY) {
        List<double[]> neighbours = new ArrayList<>();
        for (UnitPos u : this.points) {
            if (unit.equals(u.unit)) continue;
            double dist = euclideanDistanceSquared(pointX, pointY, u.x, u.y);
            if (dist <= radius) neighbours.add(new double[]{u.x, u.y});
        }
        return neighbours;
    }

    private double euclideanDistanceSquared(double point1X, double point1Y, double point2X, double point2Y) {
        return Math.pow(point1X - point2X, 2) + Math.pow(point1Y - point2Y, 2);
    }

    static class UnitPos {

        UnitStorage.UnitInfo unit;
        double x;
        double y;

        UnitPos(UnitStorage.UnitInfo unit, double x, double y) {
            this.unit = unit;
            this.x = x;
            this.y = y;
        }
    }
}
