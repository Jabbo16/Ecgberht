package ecgberht.Clustering;

import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.util.Pair;

import java.util.Set;
import java.util.TreeSet;

public class Cluster implements Comparable<Cluster>{
    public int score = 0;
    public Set<Unit> units = new TreeSet<>();
    public Pair<Double,Double> mode = new Pair<>(0.0,0.0);

    @Override
    public int compareTo(Cluster o) {
        return this.score - o.score;
    }
}
