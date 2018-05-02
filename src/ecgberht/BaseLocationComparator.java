package ecgberht;

import java.util.Comparator;

import org.openbw.bwapi4j.Position;

import bwta.BaseLocation;
import static ecgberht.Ecgberht.getGs;

public class BaseLocationComparator implements Comparator<BaseLocation>{
	private boolean enemy = false;

	public BaseLocationComparator(boolean enemy) {
		this.enemy = enemy;
	}

	@Override
	public int compare(BaseLocation a, BaseLocation b) {
		try {
			Position start = null;
			if(!enemy) {
				if(getGs().MainCC != null) {
					start = getGs().MainCC.getPosition();
				}
				if(start == null) {
					start = getGs().getPlayer().getStartLocation().toPosition();
				}
				BaseLocation closestBase = Util.getClosestBaseLocation(start); // TODO similar method / change to BWEM
				if(closestBase != null) {
					if(Util.getClosestBaseLocation(start).getTilePosition().equals(a.getTilePosition())) {
						return -1;
					}
					if(Util.getClosestBaseLocation(start).getTilePosition().equals(b.getTilePosition())) {
						return 1;
					}
				}

				if(a.isIsland()) {
					return 1;
				}
				if(b.isIsland()) {
					return -1;
				}
				double distA = getGs().bwta.getGroundDistance(a.getTilePosition(), start.toTilePosition());
				double distB = getGs().bwta.getGroundDistance(b.getTilePosition(), start.toTilePosition());
				if(distA == 0.0 && distB > 0.0) {
					return 1;
				}
				if(distB == 0.0 && distA > 0.0) {
					return -1;
				}
				if(getGs().strat.name != "FullBio" && getGs().strat.name != "FullBioFE") {
					if(a.isMineralOnly() && !b.isMineralOnly()) {
						return 1;
					}
					if(b.isMineralOnly() && !a.isMineralOnly()) {
						return -1;
					}
				}
				if(distA < distB  && distA > 0.0) {
					if(getGs().blockedBLs.contains(a)) {
						return 1;
					}
					return -1;
				}
				else {
					if(distA > distB && distB > 0.0) {
						if(getGs().blockedBLs.contains(b)) {
							return -1;
						}
						return 1;
					}
				}
				return 1;

			} else {
				if(getGs().enemyBase != null) {
					start = getGs().enemyBase.getPosition();
				}else {
					return -1;
				}

				if(a.isIsland()) {
					return 1;
				}
				if(b.isIsland()) {
					return -1;
				}
				double distA = getGs().bwta.getGroundDistance(a.getTilePosition(), start.toTilePosition());
				double distB = getGs().bwta.getGroundDistance(b.getTilePosition(), start.toTilePosition());

				if(distA == 0.0 && distB > 0.0) {
					return 1;
				}
				if(distB == 0.0 && distA > 0.0) {
					return -1;
				}
				if(a.isMineralOnly() && !b.isMineralOnly()) {
					return 1;
				}
				if(b.isMineralOnly() && !a.isMineralOnly()) {
					return -1;
				}
				if(distA < distB && distA > 0.0) {
					if(getGs().blockedBLs.contains(a)) {
						return 1;
					}
					return 1;
				}
				else {
					if(distA > distB && distB > 0.0) {
						if(getGs().blockedBLs.contains(b)) {
							return -1;
						}
						return 1;
					}
				}
				return 1;
			}
		} catch(Exception e) {
			System.err.println("Sorter");
			System.err.println(e);
		}
		return 0;
	}
}
