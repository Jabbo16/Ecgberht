package ecgberht.Strategies;

import ecgberht.Strategy;
import bwapi.UnitType;

public class FastCC extends Strategy {

    public FastCC() {
        super();
        initStrategy();
    }

    @Override
    public void initStrategy() {
        name = "14CC";
        armyForBay = 15;
        armyForTurret = 10;
        numBays = 1;
        raxPerCC = 2;
        facPerCC = 1;
        numRaxForAca = 2;
        numRaxForFac = 2;
        numCCForPort = 3;
        numCCForScience = 3;
        portPerCC = 0;
        bunker = true;
        supplyForFirstRefinery = 38;
        armyForAttack = 30;
        armyForExpand = 0;
        facForArmory = 2;
        numArmories = 1;
        initTrainUnits();
        initBuildUnits();
        initBuildAddons();
        initTechToResearch();
        initUpgradesToResearch();
    }

    @Override
    public void initTrainUnits() {
        trainUnits.add(UnitType.Terran_Marine);
    }

    @Override
    public void initBuildUnits() {
    }

    @Override
    public void initBuildAddons() {
    }

    @Override
    public void initTechToResearch() {
    }

    @Override
    public void initUpgradesToResearch() {
    }
}
