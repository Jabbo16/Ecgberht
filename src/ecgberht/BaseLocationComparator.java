package ecgberht;

import java.util.Comparator;

import bwapi.TilePosition;
import bwta.BWTA;
import bwta.BaseLocation;
import static ecgberht.Ecgberht.getGs;

public class BaseLocationComparator implements Comparator<BaseLocation>{
	private boolean enemy = false;
	
	public BaseLocationComparator(boolean enemy) {
		this.enemy = enemy;
	}

	@Override
	public int compare(BaseLocation a, BaseLocation b) {
		TilePosition start = null;
		if(!enemy) {
			if(getGs().MainCC != null) {
				start = getGs().MainCC.getTilePosition();
			}
			if(start == null) {
				start = getGs().getPlayer().getStartLocation();
			}
			if(BWTA.getNearestBaseLocation(start).equals(a)) {
				return -1;
			}
			if(BWTA.getNearestBaseLocation(start).equals(b)) {
				return 1;
			}
		} else {
			if(getGs().enemyBase != null) {
				start = getGs().enemyBase.getTilePosition();
			}
			else {
				double distA = getGs().getGroundDistance(a.getTilePosition(), start);
				double distB = getGs().getGroundDistance(b.getTilePosition(), start);
				
				if(distA == 0.0 && distB > 0.0) {
					return 1;
				}
				if(distB == 0.0 && distA > 0.0) {
					return -1;
				}
				if(distA < distB) {
					if(getGs().blockedBLs.contains(a)) {
						return 1;
					}
					return 1;
				}
				else {
					if(getGs().blockedBLs.contains(b)) {
						return -1;
					}
					return -1;
				}
			}
		}
		
		double distA = getGs().getGroundDistance(a.getTilePosition(), start);
		double distB = getGs().getGroundDistance(b.getTilePosition(), start);
		
		if(distA < distB) {
			if(getGs().blockedBLs.contains(a)) {
				return 1;
			}
			return -1;
		}
		else {
			if(getGs().blockedBLs.contains(b)) {
				return -1;
			}
			return 1;
		}
	}
}
