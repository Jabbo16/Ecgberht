package ecgberht.Strategies;

import ecgberht.Strategy;
import ecgberht.Util.Util;
import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;

public class BioMechGreedyFE extends Strategy {

    public BioMechGreedyFE() {
        super();
        initStrategy();
    }

    @Override
    public void initStrategy() {
        name = "BioMechGreedyFE";
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
        trainUnits.add(UnitType.Terran_Medic);
        trainUnits.add(UnitType.Terran_Siege_Tank_Tank_Mode);
    }

    @Override
    public void initBuildUnits() {
        buildUnits.add(UnitType.Terran_Academy);
        buildUnits.add(UnitType.Terran_Engineering_Bay);
        buildUnits.add(UnitType.Terran_Armory);
        buildUnits.add(UnitType.Terran_Missile_Turret);
        buildUnits.add(UnitType.Terran_Factory);
        buildUnits.add(UnitType.Terran_Starport);
        buildUnits.add(UnitType.Terran_Science_Facility);
    }

    @Override
    public void initBuildAddons() {
        buildAddons.add(UnitType.Terran_Comsat_Station);
        buildAddons.add(UnitType.Terran_Machine_Shop);
        buildAddons.add(UnitType.Terran_Control_Tower);
    }

    @Override
    public void initTechToResearch() {
        techToResearch.add(TechType.Stim_Packs);
        techToResearch.add(TechType.Tank_Siege_Mode);
    }

    @Override
    public void initUpgradesToResearch() {
        upgradesToResearch.add(UpgradeType.Terran_Infantry_Weapons);
        upgradesToResearch.add(UpgradeType.Terran_Vehicle_Weapons);
        upgradesToResearch.add(UpgradeType.Terran_Infantry_Armor);
        upgradesToResearch.add(UpgradeType.U_238_Shells);
    }

    @Override
    public boolean requiredUnitsForAttack() {
        return Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Tank_Mode) >= 3;
    }
}
