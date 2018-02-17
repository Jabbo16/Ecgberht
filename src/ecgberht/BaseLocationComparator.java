package ecgberht;

import java.util.Comparator;

import bwapi.TilePosition;
import bwta.BWTA;
import bwta.BaseLocation;
import static ecgberht.Ecgberht.getGs;

public class BaseLocationComparator implements Comparator<BaseLocation>{

	@Override
	public int compare(BaseLocation a, BaseLocation b) {
		TilePosition start = null;
		if(getGs().MainCC != null) {
			start = getGs().MainCC.getTilePosition();
		}
		if(start == null) {
			start = getGs().getPlayer().getStartLocation();
		}
		double distA = BWTA.getGroundDistance(a.getTilePosition(), start);
		double distB = BWTA.getGroundDistance(b.getTilePosition(), start);
		if(distA == 0.0 && distB > 0.0) {
			return 1;
		}
		if(distB == 0.0 && distA > 0.0) {
			return -1;
		}
		if(distA < distB) {
			return -1;
		}
		else {
			return 1;
		}
	}
}
