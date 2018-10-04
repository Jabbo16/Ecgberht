package ecgberht.Clustering;

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

    public double[] mode() {
        return new double[]{modeX, modeY};
    }

    /*public boolean equals(Cluster cluster) {
        if (cluster == null) return false;
        return Arrays.equals(this.mode(), cluster.mode());
    }*/

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