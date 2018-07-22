package ecgberht;

import bwem.Base;
import org.openbw.bwapi4j.TilePosition;

import java.util.Comparator;

import static ecgberht.Ecgberht.getGs;

public class BaseLocationComparator implements Comparator<Base> {
    private Base base;

    public BaseLocationComparator(Base base) {
        this.base = base;
        for (Base b : getGs().blockedBLs) System.out.println(b.getLocation());
    }

    @Override
    public int compare(Base a, Base b) {
        try {
            if (base == null) throw new Exception("No Base");
            TilePosition start = this.base.getLocation();
            if (getGs().blockedBLs.contains(a)) return 1;
            if (getGs().blockedBLs.contains(b)) return -1;
            if (a.getArea().getAccessibleNeighbors().isEmpty()) return 1;
            if (b.getArea().getAccessibleNeighbors().isEmpty()) return -1;
            if (a.equals(base)) return -1;
            if (b.equals(base)) return 1;
            double distA = getGs().bwta.getGroundDistance(a.getLocation(), start);
            double distB = getGs().bwta.getGroundDistance(b.getLocation(), start);
            if (Double.compare(distA, 0.0) == 0 && Double.compare(distB, 0.0) > 0) return 1;
            if (Double.compare(distB, 0.0) == 0 && Double.compare(distA, 0.0) > 0) return -1;
            if (!getGs().strat.name.equals("FullBio") && !getGs().strat.name.equals("FullBioFE") &&
                    !getGs().strat.name.equals("BioGreedyFE")) {
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
