package ecgberht.Strategies;

import ecgberht.Strategy;
import ecgberht.Util.Util;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.type.UpgradeType;

public class BioMechFE extends Strategy {

    public BioMechFE() {
        super();
        initStrategy();
    }

    @Override
    public void initStrategy() {
        name = "BioMechFE";
        armyForBay = 15;
        armyForTurret = 10;
        numBays = 1;
        raxPerCC = 3;
        facPerCC = 1;
        numRaxForAca = 2;
        numRaxForFac = 2;
        numCCForPort = 2;
        numCCForScience = 2;
        portPerCC = 0;
        bunker = false;
        supplyForFirstRefinery = 38;
        armyForAttack = 25;
        armyForExpand = 9;
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
        trainUnits.add(UnitType.Terran_Wraith);
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
        upgradesToResearch.add(UpgradeType.Terran_Infantry_Armor);
        upgradesToResearch.add(UpgradeType.U_238_Shells);
        upgradesToResearch.add(UpgradeType.Terran_Vehicle_Weapons);
    }

    @Override
    public boolean requiredUnitsForAttack() {
        return Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Tank_Mode) >= 3;
    }
}
