package ecgberht;

import bwapi.TilePosition;
import bwta.BWTA;
import bwta.BaseLocation;

import java.util.Comparator;

import static ecgberht.Ecgberht.getGs;

<<<<<<< HEAD
public class BaseLocationComparator implements Comparator<BaseLocation>{
	private boolean enemy = false;

	public BaseLocationComparator(boolean enemy) {
		this.enemy = enemy;
	}

	@Override
	public int compare(BaseLocation a, BaseLocation b) {
		try {
			TilePosition start = null;
			if(!enemy) {
				if(getGs().MainCC != null) {
					start = getGs().MainCC.getTilePosition();
				}
				if(start == null) {
					start = getGs().getPlayer().getStartLocation();
				}
				BaseLocation closestBase = BWTA.getNearestBaseLocation(start);
				if(closestBase != null) {
					if(BWTA.getNearestBaseLocation(start).getTilePosition().equals(a.getTilePosition())) {
						return -1;
					}
					if(BWTA.getNearestBaseLocation(start).getTilePosition().equals(b.getTilePosition())) {
						return 1;
					}
				}
				if(a.isIsland()) {
					return 1;
				}
				if(b.isIsland()) {
					return -1;
				}
				double distA = getGs().getGroundDistance(a.getTilePosition(), start);
				double distB = getGs().getGroundDistance(b.getTilePosition(), start);
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
					start = getGs().enemyBase.getTilePosition();
				}else {
					return -1;
				}

				if(a.isIsland()) {
					return 1;
				}
				if(b.isIsland()) {
					return -1;
				}
				double distA = getGs().getGroundDistance(a.getTilePosition(), start);
				double distB = getGs().getGroundDistance(b.getTilePosition(), start);

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
=======
public class BaseLocationComparator implements Comparator<BaseLocation> {
    private boolean enemy;

    public BaseLocationComparator(boolean enemy) {
        this.enemy = enemy;
    }

    @Override
    public int compare(BaseLocation a, BaseLocation b) {
        try {
            TilePosition start = null;
            if (!enemy) {
                if (getGs().MainCC != null) {
                    start = getGs().MainCC.getTilePosition();
                }
                if (start == null) {
                    start = getGs().getPlayer().getStartLocation();
                }
                BaseLocation closestBase = BWTA.getNearestBaseLocation(start);
                if (closestBase != null) {
                    if (BWTA.getNearestBaseLocation(start).getTilePosition().equals(a.getTilePosition())) {
                        return -1;
                    }
                    if (BWTA.getNearestBaseLocation(start).getTilePosition().equals(b.getTilePosition())) {
                        return 1;
                    }
                }
                if (a.isIsland()) {
                    return 1;
                }
                if (b.isIsland()) {
                    return -1;
                }
                double distA = getGs().getGroundDistance(a.getTilePosition(), start);
                double distB = getGs().getGroundDistance(b.getTilePosition(), start);
                if (distA == 0.0 && distB > 0.0) {
                    return 1;
                }
                if (distB == 0.0 && distA > 0.0) {
                    return -1;
                }
                if (getGs().strat.name != "FullBio" && getGs().strat.name != "FullBioFE") {
                    if (a.isMineralOnly() && !b.isMineralOnly()) {
                        return 1;
                    }
                    if (b.isMineralOnly() && !a.isMineralOnly()) {
                        return -1;
                    }
                }
                if (distA < distB && distA > 0.0) {
                    if (getGs().blockedBLs.contains(a)) {
                        return 1;
                    }
                    return -1;
                } else {
                    if (distA > distB && distB > 0.0) {
                        if (getGs().blockedBLs.contains(b)) {
                            return -1;
                        }
                        return 1;
                    }
                }
                return 1;

            } else {
                if (getGs().enemyBase != null) {
                    start = getGs().enemyBase.getTilePosition();
                } else {
                    return -1;
                }

                if (a.isIsland()) {
                    return 1;
                }
                if (b.isIsland()) {
                    return -1;
                }
                double distA = getGs().getGroundDistance(a.getTilePosition(), start);
                double distB = getGs().getGroundDistance(b.getTilePosition(), start);

                if (distA == 0.0 && distB > 0.0) {
                    return 1;
                }
                if (distB == 0.0 && distA > 0.0) {
                    return -1;
                }
                if (a.isMineralOnly() && !b.isMineralOnly()) {
                    return 1;
                }
                if (b.isMineralOnly() && !a.isMineralOnly()) {
                    return -1;
                }
                if (distA < distB && distA > 0.0) {
                    if (getGs().blockedBLs.contains(a)) {
                        return 1;
                    }
                    return 1;
                } else {
                    if (distA > distB && distB > 0.0) {
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
            System.err.println(e);
        }
        return 0;
    }
>>>>>>> b24a1af6c5b214342294862fd5d7584b5f566171
}
