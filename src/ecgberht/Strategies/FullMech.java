package ecgberht.Strategies;

import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;

import ecgberht.AStrategy;

public class FullMech extends AStrategy{

	public FullMech() {
		initStrategy();
	}

	@Override
	public void initStrategy() {
		name = "FullMech";
		armyForBay = 20;
		armyForTurret = 10;
		numBays = 2;
		raxPerCC = 1;
		facPerCC = 3;
		numRaxForAca = 1;
		numRaxForFac = 1;
		bunker = true;
		supplyForFirstRefinery = 28;
		armyForAttack = 25;
		armyForExpand = 10;
		numCCForPort = 2;
		numCCForScience = 2;
		portPerCC = 1;
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

	}
}
