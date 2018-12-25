package ecgberht.Clustering;

import ecgberht.UnitStorage;
import org.openbw.bwapi4j.Position;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/*
Thanks to @Yegers for improving performance
*/
public class Cluster {
    public Set<UnitStorage.UnitInfo> units = new TreeSet<>();
    public double modeX = 0;
    public double modeY = 0;
    public double maxDistFromCenter = 0;

    void updateCentroid() {
        if (units.isEmpty()) return;
        int size = units.size();
        int x = 0;
        int y = 0;
        for (UnitStorage.UnitInfo u : units) {
            if (u.visible) {
                x += u.position.getX();
                y += u.position.getY();
            } else {
                x += u.lastPosition.getX();
                y += u.lastPosition.getY();
            }
        }
        modeX = ((double) x) / size;
        modeY = ((double) y) / size;
    }

    void updateCMaxDistFromCenter() {
        if (units.isEmpty() || units.size() == 1) {
            maxDistFromCenter = 0;
            return;
        }
        for (UnitStorage.UnitInfo u : units) {
            double dist = u.visible ? u.position.getDistance(new Position((int) mode()[0], (int) mode()[1])) : u.lastPosition.getDistance(new Position((int) mode()[0], (int) mode()[1]));
            if (dist > maxDistFromCenter) maxDistFromCenter = dist;
        }
    }

    public double[] mode() {
        return new double[]{modeX, modeY};
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) return true;
        else if (!(object instanceof Cluster)) return false;
        else {
            final Cluster cluster = (Cluster) object;
            return (Arrays.equals(this.mode(), cluster.mode()));
        }
    }
}