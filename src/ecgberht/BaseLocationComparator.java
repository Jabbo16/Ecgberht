package ecgberht;

import java.util.Comparator;

import bwta.BaseLocation;
import static ecgberht.Ecgberht.getGs;

public class BaseLocationComparator implements Comparator<BaseLocation>{

	@Override
	public int compare(BaseLocation a, BaseLocation b) {
		if(getGs().broodWarDistance(a.getPosition(), getGs().MainCC.getPosition()) < getGs().broodWarDistance(b.getPosition(), getGs().MainCC.getPosition())) {
			return -1;
		}
		else {
			return 1;
		}
	}
}
