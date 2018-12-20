package ecgberht.Strategies;

import ecgberht.Strategy;
import ecgberht.Util.Util;
import org.openbw.bwapi4j.type.UnitType;

public class ProxyEightRax extends Strategy {

    public ProxyEightRax() {
        super();
        initStrategy();
    }

    @Override
    public void initStrategy() {
        name = "ProxyEightRax";
        armyForBay = 0;
        armyForTurret = 0;
        numBays = 0;
        raxPerCC = 1;
        facPerCC = 0;
        numRaxForFac = 0;
        bunker = false;
        proxy = true;
        supplyForFirstRefinery = 400;
        armyForAttack = 7;
        armyForExpand = 100;
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

    @Override
    public boolean requiredUnitsForAttack() {
        return Util.countUnitTypeSelf(UnitType.Terran_Marine) >= 3;
    }
}
