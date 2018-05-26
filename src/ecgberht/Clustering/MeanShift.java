package ecgberht.Clustering;

import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.util.Pair;

import java.util.*;
import java.util.Map.Entry;

public class MeanShift {
    private int radius = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange();
    private int iterations = 1000;
    private Map<Unit, Pair<Integer,Integer>> points = new TreeMap<>();

    public MeanShift(Set<Unit> units){
        for(Unit u : units){
            Position p = u.getPosition();
            this.points.put(u, new Pair<>(p.getX(),p.getY()));
        }
    }

    public List<Cluster> run(){
        try{
            for(int iter = 0;iter< iterations;iter++){
                for(Entry<Unit, Pair<Integer, Integer>> i : points.entrySet()){
                    Pair<Integer,Integer> initial = i.getValue();
                    Map<Unit, Pair<Integer,Integer>> neighbours = getNeighbours(initial);
                    Pair<Double,Double> numerator = new Pair<>(0.0,0.0);
                    double denominator = 0;
                    for(Entry<Unit, Pair<Integer, Integer>> neighbour : neighbours.entrySet()){
                        double distance = euclideanDistance(neighbour.getValue(), initial);
                        int bandwidth = 2;
                        double weight = gaussianKernel(distance, bandwidth);
                        numerator = new Pair<>(numerator.first + weight*neighbour.getValue().first,
                                numerator.second + weight*neighbour.getValue().second);
                        denominator += weight;
                    }
                    Pair<Integer,Integer> newPoint = new Pair<>((int)(numerator.first/denominator),
                            (int)(numerator.second/denominator));
                    points.put(i.getKey(), newPoint);
                }
            }
            List<Cluster> clusters = new ArrayList<>();
            for (Entry<Unit, Pair<Integer, Integer>> i :  points.entrySet()) {
                int c = 0;
                for (Cluster cluster : clusters) {
                    if (euclideanDistance(i.getValue(), cluster.mode) <= 0.5) {
                        break;
                    }
                    c++;
                }
                if (c == clusters.size()) {
                    Cluster clus = new Cluster();
                    clus.mode = i.getValue();
                    clusters.add(clus);
                }
                clusters.get(c).units.add(i.getKey());
            }
            return clusters;
        } catch(Exception e){
            System.err.println("MeanShift run exception");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private double gaussianKernel(double dist, double bandwidth){
        return (1/(bandwidth*Math.sqrt(2*Math.PI))) * Math.exp(Math.pow(-0.5*((dist / bandwidth)),2));
    }

    private Map<Unit, Pair<Integer,Integer>> getNeighbours(Pair<Integer,Integer> point){
        Map<Unit, Pair<Integer,Integer>> neighbours = new TreeMap<>();
        for(Entry<Unit, Pair<Integer, Integer>> u : this.points.entrySet()){
            double dist = euclideanDistance(point, u.getValue());
            if(dist <= radius) neighbours.put(u.getKey(),u.getValue());
        }
        return neighbours;
    }

    private double euclideanDistance(Pair<Integer,Integer> point1, Pair<Integer,Integer> point2){
        return Math.sqrt(Math.pow(point1.first-point2.first,2) + Math.pow(point1.second-point2.second,2));
    }
}
