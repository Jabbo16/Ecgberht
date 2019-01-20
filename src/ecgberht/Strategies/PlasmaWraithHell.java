package ecgberht.Strategies;

import ecgberht.Strategy;
import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;

public class PlasmaWraithHell extends Strategy {

    public PlasmaWraithHell() {
        super();
        initStrategy();
    }

    @Override
    public void initStrategy() {
        name = "PlasmaWraithHell";
        armyForBay = 20;
        armyForTurret = 10;
        numBays = 1;
        raxPerCC = 2;
        facPerCC = 0;
        numRaxForAca = 2;
        numRaxForFac = 1;
        bunker = false;
        supplyForFirstRefinery = 28;
        armyForAttack = 15;
        armyForExpand = 0;
        numCCForPort = 1;
        numFacForPort = 1;
        portPerCC = 2;
        numCCForScience = 2;
        facForArmory = 1;
        numArmories = 1;
        initTrainUnits();
        initBuildUnits();
        initBuildAddons();
        initTechToResearch();
        initUpgradesToResearch();
    }

    @Override
    public void initTrainUnits() {
        trainUnits.add(UnitType.Terran_Wraith);
    }

    @Override
    public void initBuildUnits() {
        buildUnits.add(UnitType.Terran_Armory);
        buildUnits.add(UnitType.Terran_Engineering_Bay);
        buildUnits.add(UnitType.Terran_Missile_Turret);
        buildUnits.add(UnitType.Terran_Factory);
        buildUnits.add(UnitType.Terran_Starport);
        buildUnits.add(UnitType.Terran_Science_Facility);
    }

    @Override
    public void initBuildAddons() {
        buildAddons.add(UnitType.Terran_Comsat_Station);
        buildAddons.add(UnitType.Terran_Control_Tower);
    }

    @Override
    public void initTechToResearch() {
        techToResearch.add(TechType.Cloaking_Field);
    }

    @Override
    public void initUpgradesToResearch() {
        upgradesToResearch.add(UpgradeType.Terran_Ship_Weapons);
        upgradesToResearch.add(UpgradeType.Terran_Ship_Plating);
    }
}
