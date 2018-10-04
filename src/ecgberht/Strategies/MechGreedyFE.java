package ecgberht.Strategies;

import ecgberht.Strategy;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.type.UpgradeType;

public class MechGreedyFE extends Strategy {

    public MechGreedyFE() {
        super();
        initStrategy();
    }

    @Override
    public void initStrategy() {
        name = "MechGreedyFE";
        armyForBay = 15;
        armyForTurret = 10;
        numBays = 1;
        raxPerCC = 1;
        facPerCC = 2;
        numRaxForAca = 2;
        numRaxForFac = 1;
        numCCForPort = 2;
        numCCForScience = 2;
        portPerCC = 1;
        bunker = true;
        supplyForFirstRefinery = 36;
        armyForAttack = 25;
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
        trainUnits.add(UnitType.Terran_Vulture);
        trainUnits.add(UnitType.Terran_Siege_Tank_Tank_Mode);
        trainUnits.add(UnitType.Terran_Wraith);
        trainUnits.add(UnitType.Terran_Goliath);
    }

    @Override
    public void initBuildUnits() {
        buildUnits.add(UnitType.Terran_Armory);
        buildUnits.add(UnitType.Terran_Engineering_Bay);
        buildUnits.add(UnitType.Terran_Missile_Turret);
        buildUnits.add(UnitType.Terran_Factory);
        buildUnits.add(UnitType.Terran_Academy);
        buildUnits.add(UnitType.Terran_Starport);
        buildUnits.add(UnitType.Terran_Science_Facility);
        if (bunker) buildUnits.add(UnitType.Terran_Bunker);
    }

    @Override
    public void initBuildAddons() {
        buildAddons.add(UnitType.Terran_Comsat_Station);
        buildAddons.add(UnitType.Terran_Machine_Shop);
        buildAddons.add(UnitType.Terran_Control_Tower);
    }

    @Override
    public void initTechToResearch() {
        techToResearch.add(TechType.Tank_Siege_Mode);
    }

    @Override
    public void initUpgradesToResearch() {
        upgradesToResearch.add(UpgradeType.Terran_Vehicle_Weapons);
        upgradesToResearch.add(UpgradeType.Terran_Vehicle_Plating);
        upgradesToResearch.add(UpgradeType.Charon_Boosters);
    }
}
