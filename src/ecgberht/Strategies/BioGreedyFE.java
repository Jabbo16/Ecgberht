package ecgberht.Strategies;

import ecgberht.Strategy;
import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;

public class BioGreedyFE extends Strategy {

    public BioGreedyFE() {
        super();
        initStrategy();
    }

    @Override
    public void initStrategy() {
        name = "BioGreedyFE";
        armyForBay = 15;
        armyForTurret = 10;
        numBays = 2;
        raxPerCC = 3;
        facPerCC = 0;
        numRaxForAca = 2;
        numRaxForFac = 3;
        numCCForPort = 2;
        numCCForScience = 2;
        portPerCC = 0;
        bunker = true;
        supplyForFirstRefinery = 38;
        armyForAttack = 25;
        armyForExpand = 0;
        facForArmory = 0;
        numArmories = 0;
        initTrainUnits();
        initBuildUnits();
        initBuildAddons();
        initTechToResearch();
        initUpgradesToResearch();
    }

    @Override
    public void initTrainUnits() {
        trainUnits.add(UnitType.Terran_Marine);
        trainUnits.add(UnitType.Terran_Firebat);
        trainUnits.add(UnitType.Terran_Medic);
    }

    @Override
    public void initBuildUnits() {
        buildUnits.add(UnitType.Terran_Academy);
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
        techToResearch.add(TechType.Stim_Packs);
    }

    @Override
    public void initUpgradesToResearch() {
        upgradesToResearch.add(UpgradeType.Terran_Infantry_Weapons);
        upgradesToResearch.add(UpgradeType.Terran_Infantry_Armor);
        upgradesToResearch.add(UpgradeType.U_238_Shells);
    }
}
