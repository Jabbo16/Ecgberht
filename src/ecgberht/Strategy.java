package ecgberht;

import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.type.UpgradeType;

import java.util.HashSet;
import java.util.Set;

public class Strategy {

    public boolean bunker = false;
    public boolean proxy = false;
    public int armyForAttack = 0;
    public int armyForBay = 0;
    public int armyForExpand = 20;
    public int armyForTurret = 0;
    public int facPerCC = 0;
    public int numBays = 0;
    public int numCCForPort = 0;
    public int numCCForScience = 0;
    public int numRaxForAca = 1;
    public int numRaxForFac = 0;
    public int portPerCC = 0;
    public int raxPerCC = 0;
    public int supplyForFirstRefinery = 0;
    public Set<TechType> techToResearch = new HashSet<>();
    public Set<UnitType> buildAddons = new HashSet<>();
    public Set<UnitType> buildUnits = new HashSet<>();
    public Set<UnitType> trainUnits = new HashSet<>();
    public Set<UpgradeType> upgradesToResearch = new HashSet<>();
    public String name = "";
    public boolean isFine = false;

    public Strategy() {
    }

    public Strategy(AStrategy build) {
        this.armyForAttack = build.armyForAttack;
        this.armyForBay = build.armyForBay;
        this.armyForExpand = build.armyForExpand;
        this.armyForTurret = build.armyForTurret;
        this.buildAddons = build.buildAddons;
        this.buildUnits = build.buildUnits;
        this.bunker = build.bunker;
        this.facPerCC = build.facPerCC;
        this.isFine = true;
        this.name = build.name;
        this.numBays = build.numBays;
        this.numCCForPort = build.numCCForPort;
        this.numCCForScience = build.numCCForScience;
        this.numRaxForAca = build.numRaxForAca;
        this.numRaxForFac = build.numRaxForFac;
        this.portPerCC = build.portPerCC;
        this.proxy = build.proxy;
        this.raxPerCC = build.raxPerCC;
        this.supplyForFirstRefinery = build.supplyForFirstRefinery;
        this.techToResearch = build.techToResearch;
        this.trainUnits = build.trainUnits;
        this.upgradesToResearch = build.upgradesToResearch;
    }
}
