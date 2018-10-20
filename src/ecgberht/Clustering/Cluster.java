package ecgberht.Clustering;

import ecgberht.Util.Util;
import org.openbw.bwapi4j.unit.Unit;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/*
Thanks to @Yegers for improving performance
*/
public class Cluster {
    public Set<Unit> units = new TreeSet<>();
    public double modeX = 0;
    public double modeY = 0;
    public double maxDistFromCenter = 0;

    void updateCentroid() {
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

    void updateCMaxDistFromCenter() {
        if(units.isEmpty() || units.size() == 1){
            maxDistFromCenter = 0;
            return;
        }
        for (Unit u : units) {
            double dist = Util.broodWarDistance(u, mode());
            if (dist > maxDistFromCenter) maxDistFromCenter = dist;
        }
    }

    public double[] mode() {
        return new double[]{modeX, modeY};
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof Cluster)) {
            return false;
        } else {
            final Cluster cluster = (Cluster) object;
            return (Arrays.equals(this.mode(), cluster.mode()));
        }
    }

}