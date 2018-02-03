package ecgberht.Strategies;

import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import ecgberht.AStrategy;

public class BioBuild extends AStrategy{

	public BioBuild() {
		initStrategy();
	}
	
	@Override
	public void initStrategy() {
		name = "FullBio";
		armyForBay = 15;
		armyForTurret = 10;
		numBays = 2;
		raxPerCC = 3;
		facPerCC = 0;
		numRaxForFac = 0;
		bunker = true;
		supplyForFirstRefinery = 30;
		armyForAttack = 40;
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
	}

	@Override
	public void initBuildUnits() {
		buildUnits.add(UnitType.Terran_Academy);
		buildUnits.add(UnitType.Terran_Engineering_Bay);
		buildUnits.add(UnitType.Terran_Missile_Turret);
		if(bunker) {
			buildUnits.add(UnitType.Terran_Bunker);
		}
	}

	@Override
	public void initBuildAddons() {
		buildAddons.add(UnitType.Terran_Comsat_Station);
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
