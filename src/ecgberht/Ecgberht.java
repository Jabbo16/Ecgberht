package ecgberht;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
//import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.composite.Selector;
import org.iaie.btree.task.composite.Sequence;
import org.iaie.btree.util.GameHandler;

import bwapi.*;
import bwta.BWTA;
import cameraModule.CameraModule;
import ecgberht.AddonBuild.*;
import ecgberht.Attack.*;
import ecgberht.Build.*;
import ecgberht.BuildingLot.*;
import ecgberht.Bunker.*;
import ecgberht.CombatStim.*;
import ecgberht.Defense.*;
import ecgberht.Expansion.*;
import ecgberht.Harass.*;
import ecgberht.MoveToBuild.*;
import ecgberht.Recollection.*;
import ecgberht.Repair.*;
import ecgberht.Scanner.*;
import ecgberht.Scouting.*;
import ecgberht.Training.*;
import ecgberht.Upgrade.*;
//import ecgberht.Weka.Weka;

public class Ecgberht extends DefaultBWListener {

	private Mirror mirror = new Mirror();
	private static Game game;
	private Player self;
	private static GameState gs;
	private BehavioralTree collectTree;
	private BehavioralTree trainTree;
	private BehavioralTree moveBuildTree;
	private BehavioralTree buildTree;
	private BehavioralTree scoutingTree;
	private BehavioralTree attackTree;
	private BehavioralTree defenseTree;
	private BehavioralTree upgradeTree;
	private BehavioralTree repairTree;
	private BehavioralTree expandTree;
	private BehavioralTree combatStimTree;
	private BehavioralTree addonBuildTree;
	private BehavioralTree buildingLotTree;
	private BehavioralTree bunkerTree;
	private BehavioralTree scannerTree;
	private BehavioralTree botherTree;
	private boolean first = false;
	private CameraModule observer;
	
	public void run() {
		mirror.getModule().setEventListener(this);
		mirror.startGame();
	}

	public static void main(String[] args) {
		new Ecgberht().run();
	}

	public static Game getGame() {
		return game;
	}
	public static GameState getGs() {
		return gs;
	}
	
	public void onStart() {
		//Disables System.err and System.Out
		OutputStream output = null;
		try {
			output = new FileOutputStream("NUL:");
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
		}
		PrintStream nullOut = new PrintStream(output);
		System.setErr(nullOut);
		System.setOut(nullOut);
		
		game = mirror.getGame();
		self = game.self();
		//game.enableFlag(1);
		//game.setLocalSpeed(0);
		System.out.println("Analyzing map...");
		BWTA.readMap();
		BWTA.analyze();
		System.out.println("Map data ready");
		observer = new CameraModule(self.getStartLocation().toPosition(), game);
		//observer.toggle();
		gs = new GameState(mirror);
		gs.initStartLocations();
		gs.initBaseLocations();
		gs.initClosestChoke();
		gs.initEnemyRace();
		gs.readOpponentInfo();
		CollectGas cg = new CollectGas("Collect Gas", gs);
		CollectMineral cm = new CollectMineral("Collect Mineral", gs);
		FreeWorker fw = new FreeWorker("No Union", gs);
		Selector<GameHandler> collectResources = new Selector<GameHandler>("Collect Melted Cash",cg,cm);
		Sequence collect = new Sequence("Collect",fw,collectResources);
		collectTree = new BehavioralTree("Recollection Tree");
		collectTree.addChild(collect);

		ChooseSCV cSCV = new ChooseSCV("Choose SCV", gs);
		ChooseMarine cMar = new ChooseMarine("Choose Marine", gs);
		ChooseMedic cMed = new ChooseMedic("Choose Medic", gs);
		ChooseTank cTan = new ChooseTank("Choose Tank", gs);
		CheckResourcesUnit cr = new CheckResourcesUnit("Check Cash", gs);
		TrainUnit tr = new TrainUnit("Train SCV", gs);
		//Selector<GameHandler> chooseUnit = new Selector<GameHandler>("Choose Recruit",cSCV,cTan,cMed,cMar);
		Selector<GameHandler> chooseUnit = new Selector<GameHandler>("Choose Recruit",cSCV);
		if(gs.strat.trainUnits.contains(UnitType.Terran_Siege_Tank_Tank_Mode)) {
			chooseUnit.addChild(cTan);
		}
		if(gs.strat.trainUnits.contains(UnitType.Terran_Medic)) {
			chooseUnit.addChild(cMed);
		}
		if(gs.strat.trainUnits.contains(UnitType.Terran_Marine)) {
			chooseUnit.addChild(cMar);
		}
		Sequence train = new Sequence("Train",chooseUnit,cr,tr);
		trainTree = new BehavioralTree("Training Tree");
		trainTree.addChild(train);

		ChooseSupply cSup = new ChooseSupply("Choose Supply Depot", gs);
		ChooseBunker cBun = new ChooseBunker("Choose Bunker", gs);
		ChooseBarracks cBar = new ChooseBarracks("Choose Barracks", gs);
		ChooseFactory cFar = new ChooseFactory("Choose Factory", gs);
		ChooseRefinery cRef = new ChooseRefinery("Choose Refinery", gs);
		ChooseBay cBay = new ChooseBay("Choose Bay", gs);
		ChooseTurret cTur = new ChooseTurret("Choose Turret", gs);
		ChooseAcademy cAca = new ChooseAcademy("Choose Academy", gs);
		CheckResourcesBuilding crb = new CheckResourcesBuilding("Check Cash", gs);
		ChoosePosition cp = new ChoosePosition("Choose Position", gs);
		ChooseWorker cw = new ChooseWorker("Choose Worker", gs);
		Move m = new Move("Move to chosen building position", gs);
		//Selector<GameHandler> chooseBuildingBuild = new Selector<GameHandler>("Choose Building to build",cSup,cBun,cTur,cRef,cAca,cBay,cBar,cFar);
		Selector<GameHandler> chooseBuildingBuild = new Selector<GameHandler>("Choose Building to build",cSup);
		if(gs.strat.bunker) {
			chooseBuildingBuild.addChild(cBun);
		}
		chooseBuildingBuild.addChild(cTur);
		chooseBuildingBuild.addChild(cRef);
		if(gs.strat.buildUnits.contains(UnitType.Terran_Academy)) {
			chooseBuildingBuild.addChild(cAca);
		}
		if(gs.strat.buildUnits.contains(UnitType.Terran_Engineering_Bay)) {
			chooseBuildingBuild.addChild(cBay);
		}
		chooseBuildingBuild.addChild(cBar);
		if(gs.strat.buildUnits.contains(UnitType.Terran_Factory)) {
			chooseBuildingBuild.addChild(cFar);
		}
		
		Sequence move = new Sequence("Move",chooseBuildingBuild,cp,cw,crb,m);
		moveBuildTree = new BehavioralTree("Building Tree");
		moveBuildTree.addChild(move);

		CheckWorkerBuild cWB = new CheckWorkerBuild("Check WorkerBuild", gs);
		Build b = new Build("Build", gs);
		Sequence build = new Sequence("Build",cWB,b);
		buildTree = new BehavioralTree("Build Tree");
		buildTree.addChild(build);
		
		CheckScout cSc = new CheckScout("Check Scout", gs);
		ChooseScout chSc = new ChooseScout("Choose Scouter",gs);
		SendScout sSc = new SendScout("Send Scout",gs);
		CheckVisibleBase cVB = new CheckVisibleBase("Check visible Base", gs);
		CheckEnemyBaseVisible cEBV = new CheckEnemyBaseVisible("Check Enemy Base Visible",gs);
		Sequence scoutFalse = new Sequence("Scout False",cSc,chSc,sSc);
		Selector<GameHandler> EnemyFound = new Selector<GameHandler>("Enemy found in base location",cEBV,sSc);
		Sequence scoutTrue = new Sequence("Scout True",cVB,EnemyFound);
		Selector<GameHandler> Scouting = new Selector<GameHandler>("Select Scouting Plan",scoutFalse,scoutTrue);
		scoutingTree = new BehavioralTree("Movement Tree");
		scoutingTree.addChild(Scouting);

		CheckArmy cA = new CheckArmy("Check Army",gs);
		ChooseAttackPosition cAP = new ChooseAttackPosition("Choose Attack Position",gs);
		SendArmy sA = new SendArmy("Send Army", gs);
		Sequence Attack = new Sequence("Attack",cA,cAP,sA);
		attackTree = new BehavioralTree("Attack Tree");
		attackTree.addChild(Attack);

		CheckPerimeter cP = new CheckPerimeter("Check Perimeter",gs);
		ChooseDefensePosition cDP = new ChooseDefensePosition("Choose Defence Position",gs);
		SendDefenders sD = new SendDefenders("Send Defenders", gs);
		Sequence Defense = new Sequence("Defence",cP,cDP,sD);
		defenseTree = new BehavioralTree("Defence Tree");
		defenseTree.addChild(Defense);

		CheckResourcesUpgrade cRU = new CheckResourcesUpgrade("Check Resources Upgrade", gs);
		ChooseArmorInfUp cAIU = new ChooseArmorInfUp("Choose Armor inf upgrade", gs);
		ChooseWeaponInfUp cWIU = new ChooseWeaponInfUp("Choose Weapon inf upgrade", gs);
		ChooseMarineRange cMR = new ChooseMarineRange("Choose Marine Range upgrade", gs);
		ChooseStimUpgrade cSU = new ChooseStimUpgrade("Choose Stimpack upgrade", gs);
		ChooseSiegeMode cSM = new ChooseSiegeMode("Choose Siege Mode", gs);
		ResearchUpgrade rU = new ResearchUpgrade("Research Upgrade", gs);
		//Selector<GameHandler> ChooseUP = new Selector<GameHandler>("Choose Upgrade", cAIU, cWIU, cSU, cMR, cSM);
		Selector<GameHandler> ChooseUP = new Selector<GameHandler>("Choose Upgrade");
		if(gs.strat.upgradesToResearch.contains(UpgradeType.Terran_Infantry_Weapons)) {
			ChooseUP.addChild(cWIU);
		}
		if(gs.strat.upgradesToResearch.contains(UpgradeType.Terran_Infantry_Armor)) {
			ChooseUP.addChild(cAIU);
		}
		if(gs.strat.techToResearch.contains(TechType.Stim_Packs)) {
			ChooseUP.addChild(cSU);
		}
		if(gs.strat.upgradesToResearch.contains(UpgradeType.U_238_Shells)) {
			ChooseUP.addChild(cMR);
		}
		if(gs.strat.techToResearch.contains(TechType.Tank_Siege_Mode)) {
			ChooseUP.addChild(cSM);
		}
		Sequence Upgrader = new Sequence("Upgrader",ChooseUP,cRU,rU);
		upgradeTree = new BehavioralTree("Technology");
		upgradeTree.addChild(Upgrader);

		CheckBuildingFlames cBF = new CheckBuildingFlames("Check building in flames",gs);
		ChooseRepairer cR = new ChooseRepairer("Choose Repairer",gs);
		Repair R = new Repair("Repair Building",gs);
		Sequence Repair = new Sequence("Repair",cBF,cR,R);
		repairTree = new BehavioralTree("RepairTree");
		repairTree.addChild(Repair);

		CheckExpansion cE = new CheckExpansion("Check Expansion",gs);
		CheckResourcesCC cRCC = new CheckResourcesCC("Check Resources CC",gs);
		ChooseBaseLocation cBL = new ChooseBaseLocation("Choose Base Location",gs);
		ChooseBuilderBL cBBL = new ChooseBuilderBL("Chose Builder Base Location",gs);
		SendBuilderBL sBBL = new SendBuilderBL("Send Builder To BL",gs);
		CheckVisibleBL cVBL = new CheckVisibleBL("Check Visible BL",gs);
		Expand E = new Expand("Expand",gs);
		Sequence Expander = new Sequence("Expander", cE, cRCC, cBL, cBBL, sBBL,cVBL, E);
		expandTree = new BehavioralTree("Expand Tree");
		expandTree.addChild(Expander);

		CheckStimResearched cSR = new CheckStimResearched("Check if Stim Packs researched",gs);
		Stim S = new Stim("Use Stim",gs);
		Sequence Stimmer = new Sequence("Stimmer", cSR, S);
		combatStimTree = new BehavioralTree("CombatStim Tree");
		combatStimTree.addChild(Stimmer);

		BuildAddon bA = new BuildAddon("Build Addon",gs);
		CheckResourcesAddon cRA = new CheckResourcesAddon("Check Resources Addon",gs);
		ChooseComsatStation cCS = new ChooseComsatStation("Choose Comsat Station",gs);
		ChooseMachineShop cMS = new ChooseMachineShop("Choose Machine Shop",gs);
		Selector<GameHandler> ChooseAddon = new Selector<GameHandler>("Choose Addon",cMS,cCS);
		Sequence Addon = new Sequence("Addon", ChooseAddon, cRA, bA);
		addonBuildTree = new BehavioralTree("Addon Build Tree");
		addonBuildTree.addChild(Addon);

		CheckBuildingsLot chBL = new CheckBuildingsLot("Check Buildings Lot", gs);
		ChooseBlotWorker cBW = new ChooseBlotWorker("Choose Building Lot worker", gs);
		ChooseBuildingLot cBLot = new ChooseBuildingLot("Choose Building Lot building", gs);
		FinishBuilding fB = new FinishBuilding("Finish Building", gs);
		Sequence BLot = new Sequence("Building Lot", chBL, cBLot, cBW, fB);
		buildingLotTree = new BehavioralTree("Building Lot Tree");
		buildingLotTree.addChild(BLot);

		ChooseBunkerToLoad cBu = new ChooseBunkerToLoad("Choose Bunker to Load",gs);
		EnterBunker eB = new EnterBunker("Enter bunker",gs);
		ChooseMarineToEnter cMTE = new ChooseMarineToEnter("Choose Marine To Enter", gs);
		Sequence Bunker = new Sequence("Bunker", cBu, cMTE, eB);
		bunkerTree = new BehavioralTree("Bunker Tree");
		bunkerTree.addChild(Bunker);

		CheckScan cScan = new CheckScan("Check scan",gs);
		Scan s = new Scan("Scan",gs);
		Sequence Scanning = new Sequence("Scanning", cScan, s);
		scannerTree = new BehavioralTree("Scanner Tree");
		scannerTree.addChild(Scanning);
		
		CheckHarasser cH = new CheckHarasser("Check Harasser", gs);
		ChooseWorkerToHarass cWTH = new ChooseWorkerToHarass("Check Worker to Harass", gs);
		ChooseBuilderToHarass cWTB = new ChooseBuilderToHarass("Check Worker to Harass", gs);
		CheckHarasserAttacked cHA = new CheckHarasserAttacked("Check Harasser Attacked",gs);
		ChooseBuildingToHarass cBTH = new ChooseBuildingToHarass("Check Building to Harass", gs);
		HarassWorker hW = new HarassWorker("Bother SCV", gs);
		Selector<GameHandler> bOw = new Selector<GameHandler>("Choose Builder or Worker or Building",cWTH,cWTB, cBTH);
		Sequence harass = new Sequence("Harass", cH, cHA, bOw, hW);
		botherTree = new BehavioralTree("Harass Tree");
		botherTree.addChild(harass);
	}

	public void onFrame() {
		observer.onFrame();
		gs.inMapUnits = new InfluenceMap(game,self,game.mapHeight(), game.mapWidth());
		gs.updateEnemyCombatUnits();
		gs.updateEnemyBuildingsMemory();
		//gs.checkEnemyAttackingWT();
		buildingLotTree.run();
		repairTree.run();
		collectTree.run();
		expandTree.run();
		upgradeTree.run();
		moveBuildTree.run();
		buildTree.run();
		addonBuildTree.run();
		trainTree.run();
		scoutingTree.run();
		botherTree.run();
		bunkerTree.run();
		scannerTree.run();
		gs.siegeTanks();
		defenseTree.run();
		attackTree.run();
		//gs.MarineMicro();
		gs.updateSquadOrderAndMicro();
		combatStimTree.run();
		gs.checkMainEnemyBase();
		gs.fix();
		gs.mergeSquads();
		if(game.elapsedTime() < 150 && gs.enemyBase != null && gs.enemyRace == Race.Zerg && !gs.EI.naughty) {
			boolean found_pool = false;
			int drones = game.enemy().allUnitCount(UnitType.Zerg_Drone);
			for(EnemyBuilding u  : gs.enemyBuildingMemory.values()) {
				if(u.type == UnitType.Zerg_Spawning_Pool) {
					found_pool = true;
					break;
				}
			}
			if(found_pool && drones <= 5) {
				gs.EI.naughty = true;
				game.sendText("Bad zerg!, bad!");
			}
		}
		if(game.getFrameCount() > 0 && game.getFrameCount() % 5 == 0) {
			gs.mineralLocking();
		}
		gs.printer();
	}

	@Override
	public void onEnd(boolean arg0) {
		String name = game.enemy().getName();
		gs.EI.updateStrategyOpponentHistory(gs.strat.name, gs.mapSize, arg0);
		if(arg0) {
			gs.EI.wins++;
			game.sendText("gg wp "+ name);
		} else {
			gs.EI.losses++;
			game.sendText("gg wp! "+ name + ", next game I will win!");
		}
//		Weka weka = new Weka();
//		try {
//			weka.createAndWriteInstance(game.enemy().getName(),gs.strat.name, gs.mapSize, arg0);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			System.err.println(e);
//		}
		gs.writeOpponentInfo(name);
	}

	@Override
	public void onNukeDetect(Position arg0) {

	}

	@Override
	public void onPlayerDropped(Player arg0) {

	}

	@Override
	public void onPlayerLeft(Player arg0) {

	}

	@Override
	public void onReceiveText(Player arg0, String arg1) {

	}

	@Override
	public void onSaveGame(String arg0) {

	}

	@Override
	public void onSendText(String arg0) {

	}

	@Override
	public void onUnitCreate(Unit arg0) {
		if(!arg0.getType().isNeutral() && !arg0.getType().isSpecialBuilding()) {
			if(arg0.getType().isBuilding()) {
				gs.inMap.updateMap(arg0,false);
				if(arg0.getPlayer().getID() == self.getID()) {
					if(arg0.getType() != UnitType.Terran_Command_Center) {
						gs.map.actualizaMapa(arg0.getTilePosition(),arg0.getType(),false);
						gs.testMap = gs.map.clone();
					}
					for(Pair<Unit,Pair<UnitType,TilePosition> > u: gs.workerBuild) {
						if(u.first.equals(arg0.getBuildUnit()) && u.second.first.equals(arg0.getType())) {
							gs.workerBuild.remove(u);
							gs.workerTask.add(new Pair<Unit,Unit>(u.first,arg0));
							gs.deltaCash.first -= arg0.getType().mineralPrice();
							gs.deltaCash.second -= arg0.getType().gasPrice();
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public void onUnitComplete(Unit arg0) {
		observer.moveCameraUnitCreated(arg0);
		if(!arg0.getType().isNeutral() && arg0.getPlayer().getID() == self.getID()) {
			if(arg0.getType().isBuilding()) {
				gs.builtBuildings++;
				if(arg0.getType().isRefinery()) {
					for(Pair<Pair<Unit,Integer>,Boolean> r:gs.refineriesAssigned) {
						if(r.first.first.getTilePosition().equals(arg0.getTilePosition())) {
							gs.refineriesAssigned.get(gs.refineriesAssigned.indexOf(r)).second = true;
							gs.refineriesAssigned.get(gs.refineriesAssigned.indexOf(r)).first.second++;
							break;
						}
					}
					gs.builtRefinery++;
				} else {
					if(arg0.getType() == UnitType.Terran_Command_Center) {
						gs.CCs.add(arg0);
						gs.addNewResources(arg0);
						if(arg0.getAddon() != null && !gs.CSs.contains(arg0.getAddon())) {
							gs.CSs.add(arg0.getAddon());
						}
						if(game.getFrameCount() == 0) {
							gs.MainCC = arg0;
						}
						gs.builtCC++;
					}
					if(arg0.getType() == UnitType.Terran_Comsat_Station) {
						gs.CSs.add(arg0);
					}
					if(arg0.getType() == UnitType.Terran_Bunker) {
						gs.DBs.add(new Pair<Unit,List<Unit> >(arg0,new ArrayList<Unit>()));
					}
					if(arg0.getType() == UnitType.Terran_Engineering_Bay || arg0.getType() == UnitType.Terran_Academy) {
						gs.UBs.add(arg0);
					}
					if(arg0.getType() == UnitType.Terran_Barracks) {
						gs.MBs.add(arg0);
					}
					if(arg0.getType() == UnitType.Terran_Factory) {
						gs.Fs.add(arg0);
					}
					if(arg0.getType() == UnitType.Terran_Supply_Depot) {
						gs.SBs.add(arg0);
					}
					if(arg0.getType() == UnitType.Terran_Machine_Shop) {
						gs.UBs.add(arg0);
					}
					if(arg0.getType() == UnitType.Terran_Missile_Turret) {
						gs.Ts.add(arg0);
					}
					for(Pair<Unit, Unit> u : gs.workerTask) {
						if(u.second.equals(arg0)) {
							gs.workerTask.remove(u);
							gs.workerIdle.add(u.first);
							break;
						}
					}
				}
			}
			else{
				if(arg0.getType().isWorker()) {
					gs.workerIdle.add(arg0);
					gs.trainedWorkers++;
				}
				else{
					if(arg0.getType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
						if(!gs.TTMs.containsKey(arg0.getID())) {
							String nombre = gs.addToSquad(arg0);
							gs.TTMs.put(arg0.getID(),nombre);
							if(gs.closestChoke != null) {
								arg0.attack(gs.closestChoke.toPosition());
							}else{
								arg0.attack(BWTA.getNearestChokepoint(self.getStartLocation()).getCenter());
							}
						}
						else {
							Position beforeSiege = gs.squads.get(gs.TTMs.get(arg0.getID())).attack;
							if(beforeSiege != null && beforeSiege != Position.None) {
								arg0.attack(beforeSiege);
							}
						}
					}
					if(arg0.getType() == UnitType.Terran_Marine || arg0.getType() == UnitType.Terran_Medic) {
						gs.addToSquad(arg0);
						if(!gs.EI.naughty || gs.enemyRace != Race.Zerg) {
							if(gs.closestChoke != null) {
								arg0.attack(gs.closestChoke.toPosition());
							}else{
								arg0.attack(BWTA.getNearestChokepoint(self.getStartLocation()).getCenter());
							}
						}
					}
					gs.trainedCombatUnits++;
				}
			}
		}
	}

	@Override
	public void onUnitDestroy(Unit arg0) {
		if(!arg0.getType().isBuilding()) {
			if(!first ) {
				gs.playSound("first.wav");
				first = true;
			}
		}
		if(!arg0.getType().isNeutral()  && (!arg0.getType().isSpecialBuilding() || arg0.getType().isRefinery())) {
			if(arg0.getPlayer().getID() == game.enemy().getID()) {
				if(arg0.equals(gs.chosenUnitToHarass)) {
					gs.chosenUnitToHarass = null;
				}
				if(arg0.getType().isBuilding()) {
					gs.inMap.updateMap(arg0,true);
					gs.enemyBuildingMemory.remove(arg0);
					gs.initAttackPosition = arg0.getTilePosition();
					gs.map.actualizaMapa(arg0.getTilePosition(), arg0.getType(), true);
				} else {
					gs.initDefensePosition = arg0.getTilePosition();
				}
			}
			if(arg0.getPlayer().getID() == self.getID()) {
				if(arg0.getType().isWorker()) {
					for(Pair<Unit,Unit> r : gs.repairerTask) {
						if(r.first.equals(arg0)) {
							gs.repairerTask.remove(r);
							break;
						}
					}
					if(gs.workerIdle.contains(arg0)) {
						gs.workerIdle.remove(arg0);
					}
					if(gs.chosenScout != null && arg0.equals(gs.chosenScout)) {
						gs.chosenScout = null;
					}
					if(gs.chosenHarasser != null && arg0.equals(gs.chosenHarasser)) {
						gs.chosenHarasser = null;
					}
					if(gs.chosenWorker != null && arg0.equals(gs.chosenWorker)) {
						gs.chosenWorker = null;
					}
					for(Pair<Unit,Unit> r : gs.repairerTask) {
						if(arg0.equals(r.first)) {
							gs.repairerTask.remove(r);
							break;
						}
					}
					if(gs.chosenBuilderBL != null && arg0.equals(gs.chosenBuilderBL)) {
						gs.chosenBuilderBL = null;
						gs.expanding = false;
						gs.chosenBaseLocation = null;
						gs.movingToExpand = false;
						gs.deltaCash.first -= arg0.getType().mineralPrice();
						gs.deltaCash.second -= arg0.getType().gasPrice();
					}
					for(Pair<Unit,Position> u:gs.workerDefenders) {
						if(arg0.equals(u.first)) {
							gs.workerDefenders.remove(u);
							break;
						}
					}
					for(Pair<Unit,Unit> w: gs.workerTask) {
						if(w.first.equals(arg0)) {
							gs.workerTask.remove(w);
							if(w.second.getType().isRefinery()) {
								for(Pair<Pair<Unit,Integer>,Boolean> r: gs.refineriesAssigned) {
									if(r.first.first.equals(w.second)) {
										gs.refineriesAssigned.get(gs.refineriesAssigned.indexOf(r)).first.second--;
										break;
									}
								}
							}
							if(w.second.getType().isMineralField()) {
								for(Pair<Unit,Integer> r: gs.mineralsAssigned) {
									if(r.first.equals(w.second)) {
										gs.mineralsAssigned.get(gs.mineralsAssigned.indexOf(r)).second--;
										break;
									}
								}
							}
							if(w.second.getType().isBuilding() && !w.second.isCompleted()) {
								gs.buildingLot.add(w.second);
							}
							break;
						}
					}
					for(Pair<Unit,Pair<UnitType,TilePosition> > w: gs.workerBuild) {
						if(w.first.equals(arg0)) {
							gs.workerBuild.remove(w);
							gs.deltaCash.first -= w.second.first.mineralPrice();
							gs.deltaCash.second -= w.second.first.gasPrice();
							break;
						}
					}
				} else if(arg0.getType().isBuilding()) {
					gs.inMap.updateMap(arg0,true);
					gs.map.actualizaMapa(arg0.getTilePosition(), arg0.getType(), true);
					for(Pair<Unit,Unit> r : gs.repairerTask) {
						if(r.second.equals(arg0)) {
							gs.workerIdle.add(r.first);
							gs.repairerTask.remove(r);
							break;
						}
					}
					for(Pair<Unit, Unit> w: gs.workerTask) {
						if(w.second.equals(arg0)) {
							gs.workerTask.remove(w);
							gs.workerIdle.add(w.first);
							break;
						}
					}
					for(Unit w: gs.buildingLot) {
						if(w.equals(arg0)) {
							gs.buildingLot.remove(w);
							break;
						}
					}
					if(gs.CCs.contains(arg0)) {
						gs.removeResources(arg0);
						if(arg0.getAddon() != null && gs.CSs.contains(arg0.getAddon())) {
							gs.CSs.remove(arg0.getAddon());
						}
						gs.CCs.remove(arg0);
						if(arg0.equals(gs.MainCC)) {
							if(gs.CCs.size() > 0) {
								for(Unit u : gs.CCs) {
									gs.MainCC = u;
									break;
								}
							}
							else {
								gs.MainCC = null;
							}
						}
					}
					if(gs.CSs.contains(arg0)) {
						gs.CCs.remove(arg0);
					}
					if(gs.Fs.contains(arg0)) {
						gs.Fs.remove(arg0);
					}
					if(gs.MBs.contains(arg0)) {
						gs.MBs.remove(arg0);
					}
					if(gs.UBs.contains(arg0)) {
						gs.UBs.remove(arg0);
					}
					if(gs.SBs.contains(arg0)) {
						gs.SBs.remove(arg0);
					}
					if(gs.Ts.contains(arg0)) {
						gs.Ts.remove(arg0);
					}
					if(arg0.getType() == UnitType.Terran_Bunker) {
						for(Pair<Unit,List<Unit> > b : gs.DBs) {
							if(b.first.equals(arg0)) {
								for(Unit u : b.second) {
									gs.addToSquad(u);
								}
								gs.DBs.remove(b);
								break;
							}
						}
					}
					if(arg0.getType().isRefinery()) {
						for(Pair<Pair<Unit,Integer>,Boolean> r: gs.refineriesAssigned) {
							if(r.first.first.equals(arg0)) {
								gs.refineriesAssigned.get(gs.refineriesAssigned.indexOf(r)).second = false;
								List<Pair<Unit,Unit> > aux = new ArrayList<Pair<Unit,Unit> >();
								for(Pair<Unit,Unit> w: gs.workerTask) {
									if(r.first.first.equals(w.second)) {
										aux.add(w);
										gs.workerIdle.add(w.first);
									}
								}
								gs.workerTask.removeAll(aux);
								break;
							}
						}
					}
					gs.testMap = gs.map.clone();
				} else {
					if(arg0.getType() == UnitType.Terran_Siege_Tank_Siege_Mode || arg0.getType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
						if(gs.TTMs.containsKey(arg0.getID())) {
							gs.TTMs.remove(arg0.getID());
							gs.removeFromSquad(arg0);
						}
					}
					else if(arg0.getType() == UnitType.Terran_Marine || arg0.getType() == UnitType.Terran_Medic) {
						gs.removeFromSquad(arg0);
					}
				}
			}
		} else if(arg0.getType().isMineralField()) {
			for(Pair<Unit,Integer> r: gs.mineralsAssigned) {
				if(r.first.equals(arg0)) {
					gs.mineralsAssigned.remove(r);
					gs.map.actualizaMapa(arg0.getTilePosition(), arg0.getType(), true);
					gs.testMap = gs.map.clone();
					List<Pair<Unit,Unit> > aux = new ArrayList<Pair<Unit,Unit> >();
					for(Pair<Unit,Unit> w: gs.workerTask) {
						if(r.first.equals(w.second)) {
							w.first.stop();
							gs.workerIdle.add(w.first);
							aux.add(w);
						}
					}
					gs.workerTask.removeAll(aux);
					break;
				}
			}
		}
	}

	@Override
	public void onUnitMorph(Unit arg0) {
		if(arg0.getPlayer().getID() == game.enemy().getID()) {
			if(arg0.getType().isBuilding() && !arg0.getType().isRefinery()) {
				if(!gs.enemyBuildingMemory.containsKey(arg0)) {
					gs.inMap.updateMap(arg0,false);
					gs.enemyBuildingMemory.put(arg0,new EnemyBuilding(arg0));
				}
			}
		}
		if(arg0.getType().isRefinery() && arg0.getPlayer().getID() == self.getID()) {
			for(Pair<Pair<Unit,Integer>,Boolean> r:gs.refineriesAssigned) {
				if(r.first.first.getTilePosition().equals(arg0.getTilePosition())) {
					gs.map.actualizaMapa(arg0.getTilePosition(), arg0.getType(),false);
					gs.testMap = gs.map.clone();
					break;
				}
			}
			for(Pair<Unit,Pair<UnitType,TilePosition> > u: (gs.workerBuild)) {
				if(u.first.equals(arg0.getBuildUnit()) && u.second.first.equals(arg0.getType())) {
					gs.workerBuild.remove(u);
					gs.workerTask.add(new Pair<Unit,Unit>(u.first,arg0));
					gs.deltaCash.first -= arg0.getType().mineralPrice();
					gs.deltaCash.second -= arg0.getType().gasPrice();
					break;
				}
			}
		}
	}

	@Override
	public void onUnitDiscover(Unit arg0) {

	}

	@Override
	public void onUnitEvade(Unit arg0) {

	}

	@Override
	public void onUnitHide(Unit arg0) {

	}

	@Override
	public void onUnitRenegade(Unit arg0) {

	}

	@Override
	public void onUnitShow(Unit arg0) {
		if(game.enemy().getID() == arg0.getPlayer().getID()) {
			if(gs.enemyRace == Race.Unknown) {
				gs.enemyRace = arg0.getType().getRace();
			}
			if(arg0.getType().isBuilding()) {
				if(!gs.enemyBuildingMemory.containsKey(arg0)) {
					gs.enemyBuildingMemory.put(arg0,new EnemyBuilding(arg0));
					gs.inMap.updateMap(arg0,false);
					gs.map.actualizaMapa(arg0.getTilePosition(), arg0.getType(), false);
				}
			}
			
		}
	}
}
