package ecgberht.Strategies;

import ecgberht.Strategy;
import ecgberht.Util.Util;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;

public class JoyORush extends Strategy {

    public JoyORush() {
        super();
        initStrategy();
    }

    @Override
    public void initStrategy() {
        name = "JoyORush";
        armyForBay = 30;
        armyForTurret = 30;
        numBays = 0;
        raxPerCC = 1;
        facPerCC = 2;
        numRaxForAca = 3;
        numRaxForFac = 1;
        numCCForPort = 2;
        numCCForScience = 2;
        portPerCC = 0;
        bunker = false;
        supplyForFirstRefinery = 26;
        armyForAttack = 5;
        armyForExpand = 25;
        facForArmory = 3;
        extraSCVs = 1;
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
        trainUnits.add(UnitType.Terran_Siege_Tank_Tank_Mode);
    }

    @Override
    public void initBuildUnits() {
        buildUnits.add(UnitType.Terran_Factory);
    }

    @Override
    public void initBuildAddons() {
        buildAddons.add(UnitType.Terran_Machine_Shop);
    }

    @Override
    public void initTechToResearch() {
    }

    @Override
    public void initUpgradesToResearch() {
    }

    @Override
    public boolean requiredUnitsForAttack() {
        return Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Tank_Mode) >= 3;
    }
}
