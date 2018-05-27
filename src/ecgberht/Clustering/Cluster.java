package ecgberht.Clustering;

import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.util.Pair;

import java.util.Set;
import java.util.TreeSet;

public class Cluster implements Comparable<Cluster> {
    public int score = 0;
    public Set<Unit> units = new TreeSet<>();
    public Pair<Double, Double> mode = new Pair<>(0.0, 0.0);

    @Override
    public int compareTo(Cluster o) {
        return this.score - o.score;
    }

    public void updateCentroid() {
        if (units.isEmpty()) return;
        if (units.size() > 1) {
            Position point = new Position(0, 0);
            for (Unit u : units) {
                point = new Position(point.getX() + u.getPosition().getX(), point.getY() + u.getPosition().getY());
            }
            mode = new Pair<>((double) point.getX() / units.size(), (double) point.getY() / units.size());
        } else {
            Position pos = units.iterator().next().getPosition();
            mode = new Pair<>((double) pos.getX(), (double) pos.getY());
        }
    }
}
