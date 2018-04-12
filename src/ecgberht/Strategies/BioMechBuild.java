package ecgberht.Strategies;

import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import ecgberht.AStrategy;

public class BioMechBuild extends AStrategy{

	public BioMechBuild() {
		initStrategy();
	}
	
	@Override
	public void initStrategy() {
		name = "BioMech";
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
		bunker = true;
		supplyForFirstRefinery = 36;
		armyForAttack = 30;
		armyForExpand = 18;
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
		trainUnits.add(UnitType.Terran_Vulture);
		trainUnits.add(UnitType.Terran_Siege_Tank_Tank_Mode);
	}

	@Override
	public void initBuildUnits() {
		buildUnits.add(UnitType.Terran_Academy);
		buildUnits.add(UnitType.Terran_Engineering_Bay);
		buildUnits.add(UnitType.Terran_Missile_Turret);
		buildUnits.add(UnitType.Terran_Factory);
		buildUnits.add(UnitType.Terran_Starport);
		buildUnits.add(UnitType.Terran_Science_Facility);
		if(bunker) {
			buildUnits.add(UnitType.Terran_Bunker);
		}
	}

	@Override
	public void initBuildAddons() {
		buildAddons.add(UnitType.Terran_Comsat_Station);
		buildAddons.add(UnitType.Terran_Machine_Shop);
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
	}
}
