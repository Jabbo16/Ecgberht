package ecgberht.Util;

import bwem.Base;
import org.openbw.bwapi4j.Position;

import java.util.Comparator;

import static ecgberht.Ecgberht.getGs;

public class BaseLocationComparator implements Comparator<Base> {
    private Base base;

    public BaseLocationComparator(Base base) {
        this.base = base;
    }

    @Override
    public int compare(Base a, Base b) {
        try {
            if (base == null) throw new Exception("No Base");
            Position start = this.base.getLocation().toPosition();
            if (getGs().blockedBLs.contains(a)) return 1;
            if (getGs().blockedBLs.contains(b)) return -1;
            if (a.getArea().getAccessibleNeighbors().isEmpty()) return 1;
            if (b.getArea().getAccessibleNeighbors().isEmpty()) return -1;
            if (a.equals(base)) return -1;
            if (b.equals(base)) return 1;
            double distA = Util.getGroundDistance(a.getLocation().toPosition(), start);
            double distB = Util.getGroundDistance(b.getLocation().toPosition(), start);
            if (Double.compare(distA, 0.0) == 0 && Double.compare(distB, 0.0) > 0) return 1;
            if (Double.compare(distB, 0.0) == 0 && Double.compare(distA, 0.0) > 0) return -1;
            if (!getGs().getStrategyFromManager().name.equals("FullBio") && !getGs().getStrategyFromManager().name.equals("FullBioFE") &&
                    !getGs().getStrategyFromManager().name.equals("BioGreedyFE")) {
                if ((a.getGeysers().isEmpty() && !a.getMinerals().isEmpty()) &&
                        (!b.getGeysers().isEmpty() && !b.getMinerals().isEmpty())) {
                    return 1;
                }
                if ((!a.getGeysers().isEmpty() && !a.getMinerals().isEmpty()) &&
                        (b.getGeysers().isEmpty() && !b.getMinerals().isEmpty())) {
                    return -1;
                }
            }
            if (Double.compare(distA, distB) < 0 && Double.compare(distA, 0.0) > 0) return -1;
            else if (Double.compare(distA, distB) > 0 && Double.compare(distB, 0.0) > 0) return 1;
            return 1;
        } catch (Exception e) {
            System.err.println("Sorter");
            e.printStackTrace();
        }
        return 0;
    }
}
