package ecgberht;

import bwem.Base;
import org.openbw.bwapi4j.Position;

import java.util.Comparator;

import static ecgberht.Ecgberht.getGs;

public class BaseLocationComparator implements Comparator<Base> {
    private boolean enemy;

    public BaseLocationComparator(boolean enemy) {
        this.enemy = enemy;
    }

    @Override
    public int compare(Base a, Base b) {
        try {
            Position start = null;
            if (!enemy) {
                if (getGs().MainCC != null) {
                    start = getGs().MainCC.getPosition();
                }
                if (start == null) {
                    start = getGs().getPlayer().getStartLocation().toPosition();
                }
                Base closestBase = Util.getClosestBaseLocation(start);
                if (closestBase != null) {
                    if (Util.getClosestBaseLocation(start).getLocation().equals(a.getLocation())) {
                        return -1;
                    }
                    if (Util.getClosestBaseLocation(start).getLocation().equals(b.getLocation())) {
                        return 1;
                    }
                }

                if (a.getArea().getAccessibleNeighbors().isEmpty()) {
                    return 1;
                }
                if (b.getArea().getAccessibleNeighbors().isEmpty()) {
                    return -1;
                }
                double distA = getGs().bwta.getGroundDistance(a.getLocation(), start.toTilePosition());
                double distB = getGs().bwta.getGroundDistance(b.getLocation(), start.toTilePosition());
                if (Double.compare(distA, 0.0) == 0 && Double.compare(distB, 0.0) > 0) {
                    return 1;
                }
                if (Double.compare(distB, 0.0) == 0 && Double.compare(distA, 0.0) > 0) {
                    return -1;
                }
                if (getGs().strat.name != "FullBio" && getGs().strat.name != "FullBioFE") {
                    if ((a.getGeysers().isEmpty() && !a.getMinerals().isEmpty()) && (!b.getGeysers().isEmpty() && !b.getMinerals().isEmpty())) {
                        return 1;
                    }
                    if ((!a.getGeysers().isEmpty() && !a.getMinerals().isEmpty()) && (b.getGeysers().isEmpty() && !b.getMinerals().isEmpty())) {
                        return -1;
                    }
                }
                if (Double.compare(distA, distB) < 0 && Double.compare(distA, 0.0) > 0) {
                    if (getGs().blockedBLs.contains(a)) {
                        return 1;
                    }
                    return -1;
                } else {
                    if (Double.compare(distA, distB) > 0 && Double.compare(distB, 0.0) > 0) {
                        if (getGs().blockedBLs.contains(b)) {
                            return -1;
                        }
                        return 1;
                    }
                }
                return 1;

            } else {
                if (getGs().enemyBase != null) {
                    start = getGs().enemyBase.getLocation().toPosition();
                } else {
                    return -1;
                }
                if (a.getArea().getAccessibleNeighbors().isEmpty()) {
                    return 1;
                }
                if (b.getArea().getAccessibleNeighbors().isEmpty()) {
                    return -1;
                }
                double distA = getGs().bwta.getGroundDistance(a.getLocation(), start.toTilePosition());
                double distB = getGs().bwta.getGroundDistance(b.getLocation(), start.toTilePosition());

                if (Double.compare(distA, 0.0) == 0 && Double.compare(distB, 0.0) > 0) {
                    return 1;
                }
                if (Double.compare(distB, 0.0) == 0 && Double.compare(distA, 0.0) > 0) {
                    return -1;
                }
                if ((a.getGeysers().isEmpty() && !a.getMinerals().isEmpty()) && (!b.getGeysers().isEmpty() && !b.getMinerals().isEmpty())) {
                    return 1;
                }
                if ((!a.getGeysers().isEmpty() && !a.getMinerals().isEmpty()) && (b.getGeysers().isEmpty() && !b.getMinerals().isEmpty())) {
                    return -1;
                }
                if (Double.compare(distA, distB) < 0 && Double.compare(distA, 0.0) > 0) {
                    if (getGs().blockedBLs.contains(a)) {
                        return 1;
                    }
                    return 1;
                } else {
                    if (Double.compare(distA, distB) > 0 && Double.compare(distB, 0.0) > 0) {
                        if (getGs().blockedBLs.contains(b)) {
                            return -1;
                        }
                        return 1;
                    }
                }
                return 1;
            }
        } catch (Exception e) {
            System.err.println("Sorter");
            e.printStackTrace();
        }
        return 0;
    }
}
