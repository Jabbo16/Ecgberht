package ecgberht.Clustering;

import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.util.Pair;

import java.util.*;
import java.util.Map.Entry;

public class MeanShift {
    private int radius = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange();
    private Map<Unit, Pair<Double,Double>> points = new TreeMap<>();
    public long time = 0;



    public MeanShift(Collection<Unit> units){
        for(Unit u : units){
            Position p = u.getPosition();
            this.points.put(u, new Pair<>((double)p.getX(),(double)p.getY()));
        }
    }

    public List<Cluster> run(){
        try{
            time = System.currentTimeMillis();
            int iterations = 50;
            for(int iter = 0; iter< iterations; iter++){
                //System.out.println("-----Iter " + iter + "------");
                for(Entry<Unit, Pair<Double, Double>> i : points.entrySet()){
                    Pair<Double,Double> initial = i.getValue();
                    List<Pair<Double,Double>> neighbours = getNeighbours(i.getKey(),initial);
                    Pair<Double,Double> numerator = new Pair<>(0.0,0.0);
                    double denominator = 0;
                    for(Pair<Double, Double> neighbour : neighbours){
                        double distance = euclideanDistance(neighbour, initial);
                        int bandwidth = 2;
                        double weight = gaussianKernel2(distance, bandwidth);
                        numerator = new Pair<>(numerator.first + weight*neighbour.first,
                                numerator.second + weight*neighbour.second);
                        denominator += weight;
                    }
                    Pair<Double,Double> newPoint = new Pair<>((numerator.first/denominator),
                            (numerator.second/denominator));
                    if(neighbours.isEmpty()){
                        newPoint = initial;
                    }
                    if(Double.isInfinite(newPoint.first) || Double.isNaN(newPoint.first)) // HACK
                        newPoint.first = initial.first;
                    if(Double.isInfinite(newPoint.second) || Double.isNaN(newPoint.second)) // HACK
                        newPoint.second = initial.second;
                    //System.out.println("Original Point : " + initial + " , shifted point: " + newPoint);
                    points.put(i.getKey(), newPoint);
                }
            }
            List<Cluster> clusters = new ArrayList<>();
            for (Entry<Unit, Pair<Double, Double>> i :  points.entrySet()) {
                int c = 0;
                for (Cluster cluster : clusters) {
                    if (euclideanDistance(i.getValue(), cluster.mode) <= 400) {
                        break;
                    }
                    c++;
                }
                if (c == clusters.size()) {
                    Cluster cluster = new Cluster();
                    cluster.mode = i.getValue();
                    clusters.add(cluster);
                }
                clusters.get(c).units.add(i.getKey());
                clusters.get(c).updateCentroid();
            }
            time = System.currentTimeMillis() - time;
            return clusters;
        } catch(Exception e){
            System.err.println("MeanShift run exception");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private double gaussianKernel2(double dist, double bandwidth){
        return Math.exp(-1.0/2.0 * (dist*dist) / (bandwidth*bandwidth));
    }

    private double gaussianKernel(double dist, double bandwidth){
        return (1/(bandwidth*Math.sqrt(2*Math.PI))) * Math.exp(Math.pow(-0.5*((dist / bandwidth)),2));
    }

    private List<Pair<Double,Double>> getNeighbours(Unit unit,Pair<Double,Double> point){
        List<Pair<Double,Double>> neighbours = new ArrayList<>();
        for(Entry<Unit, Pair<Double, Double>> u : this.points.entrySet()){
            if(unit.equals(u.getKey())) continue;
            double dist = euclideanDistance(point, u.getValue());
            if(dist <= radius) neighbours.add(u.getValue());
        }
        return neighbours;
    }

    private double euclideanDistance(Pair<Double,Double> point1, Pair<Double,Double> point2){
        return Math.sqrt(Math.pow(point1.first-point2.first,2) + Math.pow(point1.second-point2.second,2));
    }
}
