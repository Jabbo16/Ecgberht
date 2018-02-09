package ecgberht;

import java.util.HashSet;
import java.util.Set;

import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;

public class Strategy {
	public String name = "";
 	public boolean isFine = false;
	public int armyForBay = 0;
	public int armyForTurret = 0;
	public int numBays = 0;
	public int raxPerCC = 0;
	public int facPerCC = 0;
	public int numRaxForFac = 0;
	public boolean bunker = false;
	public boolean proxy = false;
	public int supplyForFirstRefinery = 0;
	public int armyForAttack = 0;
	public int armyForExpand = 20;
	public Set<UnitType> trainUnits = new HashSet<>();
	public Set<UnitType> buildUnits = new HashSet<>();
	public Set<UnitType> buildAddons = new HashSet<>();
	public Set<TechType> techToResearch = new HashSet<>();
	public Set<UpgradeType> upgradesToResearch = new HashSet<>();
	
	public Strategy() {
	}
	
	public Strategy(AStrategy build) {
		this.name = build.name;
		this.isFine = true;
		this.armyForBay = build.armyForBay;
		this.armyForTurret = build.armyForTurret;
		this.numBays = build.numBays;
		this.raxPerCC =build.raxPerCC;
		this.facPerCC =build.facPerCC;
		this.numRaxForFac = build.numRaxForFac;
		this.bunker = build.bunker;
		this.proxy = build.proxy;
		this.supplyForFirstRefinery =build.supplyForFirstRefinery;
		this.armyForAttack = build.armyForAttack;
		this.armyForExpand = build.armyForExpand;
		this.trainUnits = build.trainUnits;
		this.buildUnits = build.buildUnits;
		this.buildAddons = build.buildAddons;
		this.techToResearch = build.techToResearch;
		this.upgradesToResearch = build.upgradesToResearch;
	}
}
