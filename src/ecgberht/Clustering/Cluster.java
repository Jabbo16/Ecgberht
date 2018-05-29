package ecgberht.Clustering;

import org.openbw.bwapi4j.unit.Unit;

import java.util.Set;
import java.util.TreeSet;

/*
Thanks to @Yegers for improving performance
*/
public class Cluster {
    public int score = 0;
    public Set<Unit> units = new TreeSet<>();
    public double modeX = 0;
    public double modeY = 0;

    public void updateCentroid() {
        if (units.isEmpty()) return;
        int size = units.size();
        int x = 0;
        int y = 0;
        for (Unit u : units) {
            x += u.getPosition().getX();
            y += u.getPosition().getY();
        }
        modeX = ((double) x) / size;
        modeY = ((double) y) / size;
    }

    public double[] mode() {
        return new double[]{modeX, modeY};
    }
}