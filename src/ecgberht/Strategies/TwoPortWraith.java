package ecgberht.Strategies;

import ecgberht.Strategy;
import org.openbw.bwapi4j.type.UnitType;

public class TwoPortWraith extends Strategy {

    public TwoPortWraith() {
        super();
        initStrategy();
    }

    @Override
    public void initStrategy() {
        name = "TwoPortWraith";
        armyForBay = 15;
        armyForTurret = 10;
        numBays = 1;
        raxPerCC = 1;
        facPerCC = 1;
        numRaxForAca = 1;
        numRaxForFac = 1;
        numCCForPort = 1;
        numCCForScience = 2;
        portPerCC = 2;
        bunker = true;
        supplyForFirstRefinery = 26;
        armyForAttack = 25;
        armyForExpand = 13;
        facForArmory = 2;
        workerGas = 2;
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
        trainUnits.add(UnitType.Terran_Wraith);
    }

    @Override
    public void initBuildUnits() {
        buildUnits.add(UnitType.Terran_Factory);
        buildUnits.add(UnitType.Terran_Starport);
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
