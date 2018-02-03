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
		armyForBay = 15;
		armyForTurret = 10;
		numBays = 1;
		raxPerCC = 3;
		facPerCC = 1;
		numRaxForFac = 2;
		bunker = true;
		supplyForFirstRefinery = 30;
		armyForAttack = 35;
		armyForExpand = 20;
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
		buildUnits.add(UnitType.Terran_Missile_Turret);
		buildUnits.add(UnitType.Terran_Factory);
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
