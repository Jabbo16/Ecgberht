package ecgberht;

import java.util.HashSet;
import java.util.Set;

import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;

public abstract class AStrategy {
	public String name = "";
    public int armyForBay = 0;
	public int armyForTurret = 0;
	public int numBays = 0;
	public int raxPerCC = 0;
	public int facPerCC = 0;
	public int numRaxForFac = 0;
	public boolean bunker = false;
	public int supplyForFirstRefinery = 0;
	public int armyForAttack = 0;
	public int armyForExpand = 20;
	public Set<UnitType> trainUnits = new HashSet<>();
	public Set<UnitType> buildUnits = new HashSet<>();
	public Set<UnitType> buildAddons = new HashSet<>();
	public Set<TechType> techToResearch = new HashSet<>();
	public Set<UpgradeType> upgradesToResearch = new HashSet<>();
	
	public abstract void initStrategy();
	public abstract void initTrainUnits();
	public abstract void initBuildUnits();
	public abstract void initBuildAddons();
	public abstract void initTechToResearch();
	public abstract void initUpgradesToResearch();
}
