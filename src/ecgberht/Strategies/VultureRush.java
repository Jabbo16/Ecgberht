package ecgberht.Strategies;

import ecgberht.Strategy;
import ecgberht.Util.Util;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.type.UpgradeType;

public class VultureRush extends Strategy {

    public VultureRush() {
        super();
        initStrategy();
    }

    @Override
    public void initStrategy() {
        name = "VultureRush";
        armyForBay = 15;
        armyForTurret = 10;
        numBays = 1;
        raxPerCC = 1;
        facPerCC = 3;
        numRaxForAca = 1;
        numRaxForFac = 1;
        bunker = false;
        supplyForFirstRefinery = 24;
        armyForAttack = 5;
        armyForExpand = 14;
        numCCForPort = 1;
        numFacForPort = 2;
        portPerCC = 1;
        workerGas = 3;
        numCCForScience = 2;
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
    }

    @Override
    public void initBuildUnits() {
        buildUnits.add(UnitType.Terran_Factory);
    }

    @Override
    public void initBuildAddons() {
        buildAddons.add(UnitType.Terran_Comsat_Station);
        buildAddons.add(UnitType.Terran_Machine_Shop);
    }

    @Override
    public void initTechToResearch() {
    }

    @Override
    public void initUpgradesToResearch() {
        upgradesToResearch.add(UpgradeType.Ion_Thrusters);
    }

    @Override
    public boolean requiredUnitsForAttack() {
        return Util.countUnitTypeSelf(UnitType.Terran_Vulture) >= 2;
    }
}
