package ecgberht;

import bwem.BWEM;
import bwem.Base;
import cameraModule.CameraModule;
import ecgberht.Agents.DropShipAgent;
import ecgberht.Agents.VesselAgent;
import ecgberht.Agents.VultureAgent;
import ecgberht.Agents.WraithAgent;
import ecgberht.BehaviourTrees.AddonBuild.*;
import ecgberht.BehaviourTrees.Build.*;
import ecgberht.BehaviourTrees.BuildingLot.CheckBuildingsLot;
import ecgberht.BehaviourTrees.BuildingLot.ChooseBlotWorker;
import ecgberht.BehaviourTrees.BuildingLot.ChooseBuildingLot;
import ecgberht.BehaviourTrees.BuildingLot.FinishBuilding;
import ecgberht.BehaviourTrees.Defense.CheckPerimeter;
import ecgberht.BehaviourTrees.Defense.ChooseDefensePosition;
import ecgberht.BehaviourTrees.Defense.SendDefenders;
import ecgberht.BehaviourTrees.Harass.*;
import ecgberht.BehaviourTrees.IslandExpansion.*;
import ecgberht.BehaviourTrees.Recollection.CollectGas;
import ecgberht.BehaviourTrees.Recollection.CollectMineral;
import ecgberht.BehaviourTrees.Recollection.FreeWorker;
import ecgberht.BehaviourTrees.Repair.CheckBuildingFlames;
import ecgberht.BehaviourTrees.Repair.ChooseRepairer;
import ecgberht.BehaviourTrees.Repair.Repair;
import ecgberht.BehaviourTrees.Scanner.CheckScan;
import ecgberht.BehaviourTrees.Scanner.Scan;
import ecgberht.BehaviourTrees.Scouting.*;
import ecgberht.BehaviourTrees.Training.*;
import ecgberht.BehaviourTrees.Upgrade.*;
import ecgberht.CherryVis.CherryVisDumper;
import ecgberht.Strategies.BioMechFE;
import ecgberht.Strategies.FullBio;
import ecgberht.Strategies.FullBioFE;
import ecgberht.Strategies.FullMech;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.composite.Selector;
import org.iaie.btree.task.composite.Sequence;
import org.openbw.bwapi4j.*;
import org.openbw.bwapi4j.type.*;
import org.openbw.bwapi4j.unit.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeSet;

public class Ecgberht implements BWEventListener {

    private static BW bw;
    private static InteractionHandler ih;
    private static GameState gs;
    private static BehavioralTree addonBuildTree;
    private static BehavioralTree buildTree;
    private static BehavioralTree trainTree;
    private static BehavioralTree upgradeTree;
    private BehavioralTree botherTree;
    private BehavioralTree buildingLotTree;
    private BehavioralTree collectTree;
    private BehavioralTree defenseTree;
    private BehavioralTree repairTree;
    private BehavioralTree scannerTree;
    private BehavioralTree scoutingTree;
    private BehavioralTree islandTree;
    private boolean first = false;
    private Player self;
    private BWEM bwem = null;
    private DebugManager debugManager = null;
    private CameraModule skycladObserver = null;
    private CherryVisDumper cherryVisDumper;
    private org.bk.ass.path.Result path;

    public static void main(String[] args) {
        new Ecgberht().run();
    }

    public static BW getGame() {
        return bw;
    }

    public static GameState getGs() {
        return gs;
    }

    static void transition() {
        initTrainTree();
        initBuildTree();
        initUpgradeTree();
        initAddonBuildTree();
        //gs.scipio.updateStrat();
    }

    private static void initTrainTree() {
        ChooseSituationalUnit cSU = new ChooseSituationalUnit("Choose situational unit", gs);
        ChooseNothingTrain cNT = new ChooseNothingTrain("Choose Nothing To Train", gs);
        ChooseSCV cSCV = new ChooseSCV("Choose SCV", gs);
        ChooseFireBat cFir = new ChooseFireBat("Choose Firebat", gs);
        ChooseMarine cMar = new ChooseMarine("Choose Marine", gs);
        ChooseMedic cMed = new ChooseMedic("Choose Medic", gs);
        ChooseTank cTan = new ChooseTank("Choose Tank", gs);
        ChooseVulture cVul = new ChooseVulture("Choose vulture", gs);
        ChooseGoliath cGol = new ChooseGoliath("Choose Goliath", gs);
        ChooseWraith cWra = new ChooseWraith("Choose Wraith", gs);
        CheckResourcesUnit cr = new CheckResourcesUnit("Check Cash", gs);
        TrainUnit tr = new TrainUnit("Train Unit", gs);
        Selector chooseUnit = new Selector("Choose Recruit", cNT, cSU, cSCV);
        if (gs.getStrategyFromManager().trainUnits.contains(UnitType.Terran_Goliath)) chooseUnit.addChild(cGol);
        if (gs.getStrategyFromManager().trainUnits.contains(UnitType.Terran_Siege_Tank_Tank_Mode)) chooseUnit.addChild(cTan);
        if (gs.getStrategyFromManager().trainUnits.contains(UnitType.Terran_Vulture)) chooseUnit.addChild(cVul);
        if (gs.getStrategyFromManager().trainUnits.contains(UnitType.Terran_Wraith)) chooseUnit.addChild(cWra);
        if (gs.getStrategyFromManager().trainUnits.contains(UnitType.Terran_Medic)) chooseUnit.addChild(cMed);
        if (gs.getStrategyFromManager().trainUnits.contains(UnitType.Terran_Firebat) && gs.enemyRace == Race.Zerg)
            chooseUnit.addChild(cFir);
        if (gs.getStrategyFromManager().trainUnits.contains(UnitType.Terran_Marine)) chooseUnit.addChild(cMar);
        Sequence train = new Sequence("Train", chooseUnit, cr, tr);
        trainTree = new BehavioralTree("Training Tree");
        trainTree.addChild(train);
    }

    private static void initBuildTree() {
        Build b = new Build("Build", gs);
        WorkerWalkBuild wwB = new WorkerWalkBuild("worker walk build", gs);
        CheckMineralWalkGoldRush cMWGR = new CheckMineralWalkGoldRush("Mineral Walk Gold Rush", gs);
        ChooseNothingBuilding cNB = new ChooseNothingBuilding("Choose Nothing", gs);
        ChooseExpand cE = new ChooseExpand("Choose Expansion", gs);
        ChooseSupply cSup = new ChooseSupply("Choose Supply Depot", gs);
        ChooseBunker cBun = new ChooseBunker("Choose Bunker", gs);
        ChooseBarracks cBar = new ChooseBarracks("Choose Barracks", gs);
        ChooseFactory cFar = new ChooseFactory("Choose Factory", gs);
        ChoosePort cPor = new ChoosePort("Choose Star Port", gs);
        ChooseScience cSci = new ChooseScience("Choose Science Facility", gs);
        ChooseRefinery cRef = new ChooseRefinery("Choose Refinery", gs);
        ChooseBay cBay = new ChooseBay("Choose Bay", gs);
        ChooseTurret cTur = new ChooseTurret("Choose Turret", gs);
        ChooseAcademy cAca = new ChooseAcademy("Choose Academy", gs);
        ChooseArmory cArm = new ChooseArmory("Choose Armory", gs);
        CheckResourcesBuilding crb = new CheckResourcesBuilding("Check Cash", gs);
        ChoosePosition cp = new ChoosePosition("Choose Position", gs);
        ChooseWorker cw = new ChooseWorker("Choose Worker", gs);
        Move m = new Move("Move to chosen building position", gs);
        Selector chooseBuildingBuild = new Selector("Choose Building to build", cNB, cE, cBun, cSup);
        chooseBuildingBuild.addChild(cTur);
        chooseBuildingBuild.addChild(cRef);
        if (gs.getStrategyFromManager().buildUnits.contains(UnitType.Terran_Academy)) chooseBuildingBuild.addChild(cAca);
        if (gs.getStrategyFromManager().buildUnits.contains(UnitType.Terran_Engineering_Bay)) chooseBuildingBuild.addChild(cBay);
        if (gs.getStrategyFromManager().buildUnits.contains(UnitType.Terran_Armory)) chooseBuildingBuild.addChild(cArm);
        if (gs.getStrategyFromManager().buildUnits.contains(UnitType.Terran_Factory)) chooseBuildingBuild.addChild(cFar);
        if (gs.getStrategyFromManager().buildUnits.contains(UnitType.Terran_Starport)) chooseBuildingBuild.addChild(cPor);
        if (gs.getStrategyFromManager().buildUnits.contains(UnitType.Terran_Science_Facility)) chooseBuildingBuild.addChild(cSci);
        chooseBuildingBuild.addChild(cBar);
        Sequence buildMove;
        if (bw.getBWMap().mapHash().equals("83320e505f35c65324e93510ce2eafbaa71c9aa1"))
            buildMove = new Sequence("BuildMove", wwB, b, chooseBuildingBuild, cp, cw, crb, m);
        else if (bw.getBWMap().mapHash().equals("666dd28cd3c85223ebc749a481fc281e58221e4a")) // GoldRush
            buildMove = new Sequence("BuildMove", cMWGR, b, chooseBuildingBuild, cp, cw, crb, m);
        else
            buildMove = new Sequence("BuildMove", b, chooseBuildingBuild, cp, cw, crb, m);
        buildTree = new BehavioralTree("Building Tree");
        buildTree.addChild(buildMove);
    }

    private static void initUpgradeTree() {
        CheckResourcesUpgrade cRU = new CheckResourcesUpgrade("Check Resources Upgrade", gs);
        ChooseIrradiate cI = new ChooseIrradiate("Choose Irradiate", gs);
        ChooseEMP cEMP = new ChooseEMP("Choose EMP", gs);
        ChooseArmorMechUp cAMU = new ChooseArmorMechUp("Choose Armor mech upgrade", gs);
        ChooseWeaponMechUp cWMU = new ChooseWeaponMechUp("Choose weapon mech upgrade", gs);
        ChooseArmorInfUp cAIU = new ChooseArmorInfUp("Choose Armor inf upgrade", gs);
        ChooseWeaponInfUp cWIU = new ChooseWeaponInfUp("Choose Weapon inf upgrade", gs);
        ChooseMarineRange cMR = new ChooseMarineRange("Choose Marine Range upgrade", gs);
        ChooseStimUpgrade cSU = new ChooseStimUpgrade("Choose Stimpack upgrade", gs);
        ChooseSiegeMode cSM = new ChooseSiegeMode("Choose Siege Mode", gs);
        ChooseCharonBoosters cCB = new ChooseCharonBoosters("Choose Charon Boosters", gs);
        ChooseVultureSpeed cVS = new ChooseVultureSpeed("Choose Vulture Speed", gs);
        ResearchUpgrade rU = new ResearchUpgrade("Research Upgrade", gs);
        Selector ChooseUP = new Selector("Choose Upgrade");
        ChooseUP.addChild(cI);
        ChooseUP.addChild(cEMP);
        if (gs.getStrategyFromManager().upgradesToResearch.contains(UpgradeType.Terran_Infantry_Weapons)) ChooseUP.addChild(cWIU);
        if (gs.getStrategyFromManager().upgradesToResearch.contains(UpgradeType.Terran_Infantry_Armor)) ChooseUP.addChild(cAIU);
        if (gs.getStrategyFromManager().techToResearch.contains(TechType.Stim_Packs)) ChooseUP.addChild(cSU);
        if (gs.getStrategyFromManager().upgradesToResearch.contains(UpgradeType.U_238_Shells)) ChooseUP.addChild(cMR);
        if (gs.getStrategyFromManager().techToResearch.contains(TechType.Tank_Siege_Mode)) ChooseUP.addChild(cSM);
        if (gs.getStrategyFromManager().upgradesToResearch.contains(UpgradeType.Ion_Thrusters)) ChooseUP.addChild(cVS);
        if (gs.getStrategyFromManager().upgradesToResearch.contains(UpgradeType.Charon_Boosters)) ChooseUP.addChild(cCB);
        if (gs.getStrategyFromManager().upgradesToResearch.contains(UpgradeType.Terran_Vehicle_Weapons)) ChooseUP.addChild(cWMU);
        if (gs.getStrategyFromManager().upgradesToResearch.contains(UpgradeType.Terran_Vehicle_Plating)) ChooseUP.addChild(cAMU);
        Sequence Upgrader = new Sequence("Upgrader", ChooseUP, cRU, rU);
        upgradeTree = new BehavioralTree("Technology");
        upgradeTree.addChild(Upgrader);
    }

    private static void initAddonBuildTree() {
        BuildAddon bA = new BuildAddon("Build Addon", gs);
        CheckResourcesAddon cRA = new CheckResourcesAddon("Check Resources Addon", gs);
        ChooseComsatStation cCS = new ChooseComsatStation("Choose Comsat Station", gs);
        ChooseMachineShop cMS = new ChooseMachineShop("Choose Machine Shop", gs);
        ChooseTower cT = new ChooseTower("Choose Control Tower", gs);
        Selector ChooseAddon = new Selector("Choose Addon");
        if (gs.getStrategyFromManager().buildAddons.contains(UnitType.Terran_Machine_Shop)) ChooseAddon.addChild(cMS);
        if (gs.getStrategyFromManager().buildAddons.contains(UnitType.Terran_Comsat_Station)) ChooseAddon.addChild(cCS);
        if (gs.getStrategyFromManager().buildAddons.contains(UnitType.Terran_Control_Tower)) ChooseAddon.addChild(cT);
        Sequence Addon = new Sequence("Addon", ChooseAddon, cRA, bA);
        addonBuildTree = new BehavioralTree("Addon Build Tree");
        addonBuildTree.addChild(Addon);
    }

    private void initIslandTree() {
        CheckIslands chI = new CheckIslands("Check islands", gs);
        CheckExpandingIsland cEI = new CheckExpandingIsland("Check Expanding To Island", gs);
        CheckDropped chD = new CheckDropped("Check Dropped", gs);
        CheckBlockingMinerals cBM = new CheckBlockingMinerals("Check Blocking minerals", gs);
        CheckResourcesIsland cRI = new CheckResourcesIsland("Check resources Island", gs);
        MoveIsland mI = new MoveIsland("Move Island", gs);
        ChooseDropShip cD = new ChooseDropShip("Choose DropShip", gs);
        ChooseIsland cI = new ChooseIsland("Choose Island", gs);
        ChooseWorkerDrop cWD = new ChooseWorkerDrop("Choose Worker Drop", gs);
        SendToDrop sD = new SendToDrop("Send To Drop", gs);
        Sequence chooseThings = new Sequence("Choose things", cD, cI, cWD, sD);
        Sequence expand = new Sequence("Island expand", cEI, chD, cBM, cRI, mI);
        Selector expanding = new Selector("Check if already expanding", expand, cEI, chooseThings);
        Sequence islandExpansion = new Sequence("island expansion", chI, expanding);
        islandTree = new BehavioralTree("islandTree");
        islandTree.addChild(islandExpansion);
    }

    private void run() {
        Ecgberht.bw = new BW(this);
        Ecgberht.bw.startGame();
    }

    private void initScoutingTree() {
        CheckScout cSc = new CheckScout("Check Scout", gs);
        ChooseScout chSc = new ChooseScout("Choose Scouter", gs);
        SendScout sSc = new SendScout("Send Scout", gs);
        CheckMineralWalk cMW = new CheckMineralWalk("Check Mineral Walk", gs);
        CheckVisibleBase cVB = new CheckVisibleBase("Check visible Base", gs);
        CheckEnemyBaseVisible cEBV = new CheckEnemyBaseVisible("Check Enemy Base Visible", gs);
        Sequence scoutFalse = new Sequence("Scout ", cSc, chSc, sSc);
        Selector EnemyFound = new Selector("Enemy found in base location", cEBV, sSc);
        Sequence scoutTrue = new Sequence("Scout True", cMW, cVB, EnemyFound);
        Selector Scouting = new Selector("Select Scouting Plan", scoutFalse, scoutTrue);
        scoutingTree = new BehavioralTree("Movement Tree");
        scoutingTree.addChild(Scouting);
    }

    private void initDefenseTree() {
        CheckPerimeter cP = new CheckPerimeter("Check Perimeter", gs);
        ChooseDefensePosition cDP = new ChooseDefensePosition("Choose Defence Position", gs);
        SendDefenders sD = new SendDefenders("Send Defenders", gs);
        Sequence Defense = new Sequence("Defence", cP, cDP, sD);
        defenseTree = new BehavioralTree("Defence Tree");
        defenseTree.addChild(Defense);
    }

    private void initRepairTree() {
        CheckBuildingFlames cBF = new CheckBuildingFlames("Check building in flames", gs);
        ChooseRepairer cR = new ChooseRepairer("Choose Repairer", gs);
        Repair R = new Repair("Repair Building", gs);
        Sequence Repair = new Sequence("Repair", cBF, cR, R);
        repairTree = new BehavioralTree("RepairTree");
        repairTree.addChild(Repair);
    }

    private void initBuildingLotTree() {
        CheckBuildingsLot chBL = new CheckBuildingsLot("Check Buildings Lot", gs);
        ChooseBlotWorker cBW = new ChooseBlotWorker("Choose Building Lot worker", gs);
        ChooseBuildingLot cBLot = new ChooseBuildingLot("Choose Building Lot building", gs);
        FinishBuilding fB = new FinishBuilding("Finish Building", gs);
        Sequence BLot = new Sequence("Building Lot", chBL, cBLot, cBW, fB);
        buildingLotTree = new BehavioralTree("Building Lot Tree");
        buildingLotTree.addChild(BLot);
    }

    private void initScanTree() {
        CheckScan cScan = new CheckScan("Check scan", gs);
        Scan s = new Scan("Scan", gs);
        Sequence Scanning = new Sequence("Scanning", cScan, s);
        scannerTree = new BehavioralTree("Scanner Tree");
        scannerTree.addChild(Scanning);
    }

    private void initHarassTree() {
        CheckHarasser cH = new CheckHarasser("Check Harasser", gs);
        CheckExplorer cE = new CheckExplorer("Check Explorer", gs);
        ChooseWorkerToHarass cWTH = new ChooseWorkerToHarass("Check Worker to Harass", gs);
        ChooseBuilderToHarass cWTB = new ChooseBuilderToHarass("Check Worker to Harass", gs);
        CheckHarasserAttacked cHA = new CheckHarasserAttacked("Check Harasser Attacked", gs);
        ChooseBuildingToHarass cBTH = new ChooseBuildingToHarass("Check Building to Harass", gs);
        Explore E = new Explore("Explore", gs);
        HarassWorker hW = new HarassWorker("Bother SCV", gs);
        Selector bOw = new Selector("Choose Builder or Worker or Building", cWTH, cWTB, cBTH);
        Sequence harassAttack = new Sequence("Harass", cHA, bOw, hW);
        Sequence explorer = new Sequence("Explorer", cE, E);
        Selector eOh = new Selector("Explorer or harasser", explorer, harassAttack);
        Sequence harass = new Sequence("Harass", cH, eOh);
        botherTree = new BehavioralTree("Harass Tree");
        botherTree.addChild(harass);
    }

    private void initCollectTree() {
        CollectGas cg = new CollectGas("Collect Gas", gs);
        CollectMineral cm = new CollectMineral("Collect Mineral", gs);
        FreeWorker fw = new FreeWorker("No Union", gs);
        Selector collectResources = new Selector("Collect Melted Cash", cg, cm);
        Sequence collect = new Sequence("Collect", fw, collectResources);
        collectTree = new BehavioralTree("Recollection Tree");
        collectTree.addChild(collect);
    }

    @Override
    public void onStart() {
        try {
            ConfigManager.readConfig();
            if (!ConfigManager.getConfig().ecgConfig.debugConsole) {
                // Disables System.err and System.Out
                OutputStream output = null;
                try {
                    output = new FileOutputStream("NUL:");
                } catch (FileNotFoundException ignored) {
                }
                PrintStream nullOut = new PrintStream(Objects.requireNonNull(output));
                System.setErr(nullOut);
                System.setOut(nullOut);
            }
            if (ConfigManager.getConfig().ecgConfig.humanMode) {
                ConfigManager.getConfig().ecgConfig.sounds = false;
                ConfigManager.getConfig().ecgConfig.debugConsole = false;
                ConfigManager.getConfig().ecgConfig.debugScreen = false;
                ConfigManager.getConfig().ecgConfig.debugText = false;
                ConfigManager.getConfig().ecgConfig.enableCherryVisDump = false;
                ConfigManager.getConfig().ecgConfig.enableLatCom = true;
                ConfigManager.getConfig().ecgConfig.forceStrat = "";
                ConfigManager.getConfig().ecgConfig.sscait = true;
            }
            self = bw.getInteractionHandler().self();
            skycladObserver = new CameraModule(self.getStartLocation(), bw);
            ih = bw.getInteractionHandler();
            debugManager = new DebugManager(bw.getMapDrawer(), bw.getInteractionHandler(), skycladObserver);
            IntelligenceAgency.onStartIntelligenceAgency(ih.enemy());
            if (!ConfigManager.getConfig().ecgConfig.enableLatCom) ih.enableLatCom(false);
            else ih.enableLatCom(true);
            if (ConfigManager.getConfig().bwapiConfig.completeMapInformation) ih.enableCompleteMapInformation();
            if (ConfigManager.getConfig().bwapiConfig.frameSkip != 0)
                ih.setFrameSkip(ConfigManager.getConfig().bwapiConfig.frameSkip);
            if (ConfigManager.getConfig().bwapiConfig.localSpeed >= 0)
                ih.setLocalSpeed(ConfigManager.getConfig().bwapiConfig.localSpeed);
            if (ConfigManager.getConfig().bwapiConfig.userInput) ih.enableUserInput();
            bwem = new BWEM(bw);
            if (bw.getBWMap().mapHash().equals("69a3b6a5a3d4120e47408defd3ca44c954997948")) { // Hitchhiker
                Util.sendText("Hitchhiker :(");
            }
            bwem.initialize();
            if (!bw.getBWMap().mapHash().equals("e6d0144e14315118d916905ff5e7045f68db541e")) // Aztec KSL crash fix
                bwem.getMap().assignStartingLocationsToSuitableBases();
            gs = new GameState(bw, bwem);
            gs.baseManager = new BaseManager(bwem);
            gs.initEnemyRace();
            gs.learningManager.onStart(ih.enemy().getName(), Util.raceToString(bw.getInteractionHandler().enemy().getRace()));
            gs.alwaysPools();
            if (gs.enemyRace == Race.Zerg && gs.learningManager.isNaughty()) gs.playSound("rushed.mp3");
            gs.singletonManager = StrategyManager.getInstance();
            //gs.scipio.updateStrat();
            IntelligenceAgency.setStartStrat(gs.getStrategyFromManager().name);
            gs.initStartLocations();
            boolean fortress = bw.getBWMap().mapHash().equals("83320e505f35c65324e93510ce2eafbaa71c9aa1"); // Fortress
            for (Base b : bwem.getMap().getBases()) {
                if (fortress) {
                    if (b.getMinerals().size() < 3) continue;
                    if (gs.fortressSpecialBLsTiles.contains(b.getLocation()))
                        gs.fortressSpecialBLs.put(b, gs.getMineralWalkPatchesFortress(b));
                    gs.BLs.add(b);

                } else if (b.getArea().getAccessibleNeighbors().isEmpty())
                    gs.islandBases.add(b); // Island expansions re-enabled
                else gs.BLs.add(b);
            }
            if (bw.getBWMap().mapHash().equals("666dd28cd3c85223ebc749a481fc281e58221e4a")) { // GoldRush
                gs.initMineralWalkPatches();
            }
            gs.initBlockingMinerals();
            gs.initBaseLocations();
            gs.checkBasesWithBLockingMinerals();
            gs.initChokes();
            IntelligenceAgency.EnemyStrats ES = IntelligenceAgency.getEnemyStrat();
            if (gs.mainChoke != null && (ES == IntelligenceAgency.EnemyStrats.ZealotRush
                    || ES == IntelligenceAgency.EnemyStrats.EarlyPool)) {
                gs.defendPosition = gs.mainChoke.getCenter().toPosition();
            }
            gs.map = new BuildingMap(bw, ih.self(), bwem);
            gs.map.initMap();
            gs.testMap = gs.map.clone();
            // Trees Initializations
            initCollectTree();
            initTrainTree();
            initBuildTree();
            initScoutingTree();
            initDefenseTree();
            initUpgradeTree();
            initRepairTree();
            initAddonBuildTree();
            initBuildingLotTree();
            initScanTree();
            initHarassTree();
            initIslandTree();
            gs.silentCartographer = new Cartographer(bw.getBWMap().mapWidth(), bw.getBWMap().mapHeight());
            if (ConfigManager.getConfig().ecgConfig.enableSkyCladObserver) skycladObserver.toggle();
            cherryVisDumper = new CherryVisDumper(gs);
        } catch (Exception e) {
            System.err.println("onStart Exception");
            e.printStackTrace();
        }
    }

    @Override
    public void onFrame() {
        try {
            // Testing
            if (gs.enemyMainBase != null) {
                if (path == null) {
                    if (gs.naturalChoke != null) {
                        path = gs.silentCartographer.getWalkablePath(gs.naturalChoke.getCenter(), gs.enemyMainBase.getLocation().toWalkPosition());
                    } else {
                        path = gs.silentCartographer.getWalkablePath(self.getStartLocation().toWalkPosition(), gs.enemyMainBase.getLocation().toWalkPosition());
                    }
                } else if(ConfigManager.getConfig().ecgConfig.debugScreen) {
                    for (org.bk.ass.path.Position p : path.path) {
                        Position pos = new WalkPosition(p.x, p.y).toPosition();
                        bw.getMapDrawer().drawCircleMap(pos, 4, Color.RED, true);
                    }
                }
            }
            gs.frameCount = ih.getFrameCount();
            gs.baseManager.updateGarrisons();
            skycladObserver.onFrame();
            gs.fix();
            OnFrame frame= new OnFrame(gs.unitStorage);
        	frame.action();
            if (gs.frameCount == 1500) gs.sendCustomMessage();
            if (gs.frameCount == 2300) gs.sendRandomMessage();
            if (gs.frameCount == 1000 && bw.getBWMap().mapHash().equals("69a3b6a5a3d4120e47408defd3ca44c954997948")) {
                Util.sendText("RIP"); // Hitchhiker
                gs.getIH().leaveGame();
            }
            // If lategame vs Terran and we are Bio (Stim) -> transition to Mech
            if (gs.frameCount == 24 * 60 * 14 && gs.enemyRace == Race.Terran && gs.getStrategyFromManager().techToResearch.contains(TechType.Stim_Packs) && !gs.getStrategyFromManager().trainUnits.contains(UnitType.Terran_Siege_Tank_Tank_Mode)) {
                gs.setStrategyToManager(new FullMech());
                transition();
            }
            // If rushing and enough time has passed -> transition to best strat
            if (gs.frameCount == 24 * 60 * 8 && gs.getStrategyFromManager().proxy) {
                gs.singletonManager.chooseProxyTransition();
                List<UnitInfo> workersToDelete = new ArrayList<>();
                for (UnitInfo u : gs.myArmy) {
                    if (!(u.unit instanceof Worker)) continue;
                    workersToDelete.add(u);
                }
                gs.myArmy.removeAll(workersToDelete);
                workersToDelete.forEach(u -> gs.workerIdle.add((Worker) u.unit));
                transition();
            }
            if (bw.getBWMap().mapHash().equals("6f5295624a7e3887470f3f2e14727b1411321a67") && // Plasma transition
                    gs.getStrategyFromManager().name.equals("PlasmaWraithHell") && gs.frameCount == 24 * 700) {
                FullBio b = new FullBio();
                b.buildUnits.remove(UnitType.Terran_Bunker);
                gs.setStrategyToManager(b);
                gs.maxWraiths = 5;
                transition();
            }
            if (bw.getBWMap().mapHash().equals("83320e505f35c65324e93510ce2eafbaa71c9aa1") && // Fortress wraiths
                    !gs.getStrategyFromManager().trainUnits.contains(UnitType.Terran_Wraith) && gs.frameCount == 24 * 900) {
                gs.getStrategyFromManager().trainUnits.add(UnitType.Terran_Wraith);
                gs.maxWraiths = 999;
                transition();
            }
            if (bw.getBWMap().mapHash().equals("6f5295624a7e3887470f3f2e14727b1411321a67") && // Plasma special eggs
                    !gs.getStrategyFromManager().name.equals("PlasmaWraithHell")) {
                for (Unit u : bw.getAllUnits()) {
                    if (u.getType() != UnitType.Zerg_Egg && u instanceof PlayerUnit && !Util.isEnemy(((PlayerUnit) u).getPlayer()))
                        continue;
                    if (!u.isVisible() && gs.enemyCombatUnitMemory.contains(u)) gs.enemyCombatUnitMemory.remove(u);
                    else if (u.getType() == UnitType.Zerg_Egg && u instanceof PlayerUnit &&
                            !Util.isEnemy(((PlayerUnit) u).getPlayer())) {
                        gs.enemyCombatUnitMemory.add(u);
                    }
                }
            }
            if (gs.getStrategyFromManager().name.equals("TwoPortWraith") && Util.countBuildingAll(UnitType.Terran_Command_Center) > 1 && gs.wraithsTrained >= 4) {
                if (gs.enemyRace == Race.Zerg) {
                    if (IntelligenceAgency.enemyHasType(UnitType.Zerg_Lurker)) gs.setStrategyToManager(new BioMechFE());
                    else gs.setStrategyToManager(new FullBioFE());
                    if (gs.proxyBuilding != null) gs.getStrategyFromManager().trainUnits.add(UnitType.Terran_Vulture);
                } else if (gs.enemyRace == Race.Terran) gs.setStrategyToManager(new FullMech());
                gs.getStrategyFromManager().armyForAttack += 5;
                transition();
            }
            if (gs.getStrategyFromManager().name.equals("VultureRush") && Util.countBuildingAll(UnitType.Terran_Command_Center) > 1) {
                gs.setStrategyToManager(new FullMech());
                if (gs.naturalChoke != null) gs.defendPosition = gs.naturalChoke.getCenter().toPosition();
                transition();
            }
            if (gs.getStrategyFromManager().name.equals("TheNitekat") || gs.getStrategyFromManager().name.equals("JoyORush") && gs.CCs.size() > 1) {
                gs.setStrategyToManager(new FullMech());
                if (gs.naturalChoke != null) gs.defendPosition = gs.naturalChoke.getCenter().toPosition();
                transition();
            }
            gs.cancelDyingThings();
            //IntelligenceAgency.updateBullets(); //Disabled because its not actually used yet and its slow
            gs.wizard.onFrameSpellManager();
            IntelligenceAgency.onFrame();
            gs.sim.onFrameSim();
            gs.vespeneManager(); //Disabled until it works
            gs.sqManager.updateBunkers();
            gs.checkDisrupter();
            buildingLotTree.run();
            repairTree.run();
            collectTree.run();
            upgradeTree.run();
            islandTree.run();
            buildTree.run();
            addonBuildTree.run();
            trainTree.run();
            scoutingTree.run();
            botherTree.run();
            scannerTree.run();
            if (gs.getStrategyFromManager().name.equals("ProxyBBS")) gs.checkWorkerMilitia(2);
            else if (gs.getStrategyFromManager().name.equals("ProxyEightRax")) gs.checkWorkerMilitia(1);
            defenseTree.run();
            gs.updateAttack();
            gs.runAgents();
            gs.sqManager.updateSquadOrderAndMicro();
            gs.checkMainEnemyBase();
            if (gs.frameCount > 10 && gs.frameCount % 5 == 0) gs.mineralLocking();
            debugManager.onFrame(gs);
            cherryVisDumper.onFrame();
        } catch (Exception e) {
            System.err.println("onFrame Exception");
            e.printStackTrace();
        }
    }

    @Override
    public void onEnd(boolean arg0) {
        try {
            String name = ih.enemy().getName();
            if (arg0) Util.sendText("gg wp " + name);
            else Util.sendText("gg wp! " + name + ", next game I will not lose!");
            if (bw.getBWMap().mapHash().equals("6f5295624a7e3887470f3f2e14727b1411321a67"))
                gs.getStrategyFromManager().name = "PlasmaWraithHell";
            String oldStrat = IntelligenceAgency.getStartStrat();
            cherryVisDumper.onEnd(arg0, oldStrat);
            if (oldStrat != null && !oldStrat.equals(gs.getStrategyFromManager().name)) gs.getStrategyFromManager().name = oldStrat;
            gs.learningManager.onEnd(gs.getStrategyFromManager().name, gs.mapSize, arg0, name, gs.enemyRace, bw.getBWMap().mapFileName().replace(".scx", ""), gs.enemyIsRandom, IntelligenceAgency.getEnemyStrat());
        } catch (Exception e) {
            System.err.println("onEnd Exception");
            e.printStackTrace();
        }
    }

    @Override
    public void onNukeDetect(Position arg0) {

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
        debugManager.keyboardInteraction(arg0);
    }

    @Override
    public void onUnitCreate(Unit arg0) {
        try {
            if (arg0 == null) return;
            if (arg0 instanceof MineralPatch || arg0 instanceof VespeneGeyser || arg0 instanceof SpecialBuilding
                    || arg0 instanceof Critter || arg0 instanceof ScannerSweep) return;
            PlayerUnit pU = (PlayerUnit) arg0;
            if (arg0 instanceof ResourceDepot) gs.baseManager.onCreate((ResourceDepot) arg0);
            UnitType type = arg0.getType();
            if (!type.isNeutral() && !type.isSpecialBuilding()) {
                if (arg0 instanceof Building) {
                    if (pU.getPlayer().getId() == self.getId()) {
                    	OnUnitAction createAction= new OnUnitCreate(arg0,gs.unitStorage);
                    	createAction.action();
                        if (!(arg0 instanceof CommandCenter)) {
                            gs.map.updateMap(arg0.getTilePosition(), type, false);
                            gs.testMap = gs.map.clone();
                        } else if (getGs().iReallyWantToExpand) getGs().iReallyWantToExpand = false;
                        if (arg0 instanceof Addon) return;
                        if (arg0 instanceof CommandCenter && ih.getFrameCount() == 0) return;
                        if (arg0 instanceof Bunker && gs.learningManager.isNaughty() && gs.enemyRace == Race.Zerg) {
                            gs.defendPosition = arg0.getPosition();
                        }
                        SCV worker = (SCV) ((Building) arg0).getBuildUnit();
                        if (worker != null) {
                            if (gs.workerBuild.containsKey(worker) && type.equals(gs.workerBuild.get(worker).first)) {
                                gs.workerTask.put(worker, (Building) arg0);
                                gs.deltaCash.first -= type.mineralPrice();
                                gs.deltaCash.second -= type.gasPrice();
                                gs.workerBuild.remove(worker);
                            }
                        }
                    }
                } else if (pU.getPlayer().getId() == self.getId()) {
                    if (gs.ih.getFrameCount() > 0) gs.supplyMan.onCreate(arg0);
                    if (arg0 instanceof Vulture) gs.vulturesTrained++;
                    if (arg0 instanceof Wraith) gs.wraithsTrained++;
                    if (arg0 instanceof SiegeTank) {
                        gs.tanksTrained++;
                        if (gs.tanksTrained == 3 && gs.getStrategyFromManager().name.equals("JoyORush")) {
                            gs.getStrategyFromManager().trainUnits.add(UnitType.Terran_Vulture);
                            gs.getStrategyFromManager().upgradesToResearch.add(UpgradeType.Ion_Thrusters);
                            //gs.getStrat().techToResearch.add(TechType.Spider_Mines);
                            transition();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("onUnitCreate exception");
            e.printStackTrace();
        }
    }

    @Override
    public void onUnitComplete(Unit arg0) {
        try {
            if (arg0 instanceof MineralPatch || arg0 instanceof VespeneGeyser || arg0 instanceof SpecialBuilding
                    || arg0 instanceof Critter || arg0 instanceof ScannerSweep) {
                return;
            }
            skycladObserver.moveCameraUnitCompleted(arg0);
            PlayerUnit pU = (PlayerUnit) arg0;
            UnitType type = arg0.getType();
            if (!type.isNeutral() && pU.getPlayer().getId() == self.getId()) {
            	OnUnitAction completeAction= new OnUnitComplete(arg0,gs.unitStorage);
            	completeAction.action();
                if (gs.ih.getFrameCount() > 0) gs.supplyMan.onComplete(arg0);
                if (type.isBuilding()) {
                    gs.builtBuildings++;
                    if (type.isRefinery()) {
                        for (Entry<VespeneGeyser, Boolean> r : gs.vespeneGeysers.entrySet()) {
                            if (r.getKey().getTilePosition().equals(arg0.getTilePosition())) {
                                gs.vespeneGeysers.put(r.getKey(), true);
                                break;
                            }
                        }
                        for (Entry<SCV, Building> u : gs.workerTask.entrySet()) {
                            if (u.getValue().equals(arg0)) {
                                gs.workerGas.put(u.getKey(), (GasMiningFacility) arg0);
                                gs.workerTask.remove(u.getKey());
                                break;
                            }
                        }
                        gs.refineriesAssigned.put((GasMiningFacility) arg0, 1);
                        gs.builtRefinery++;
                    } else {
                        if (type == UnitType.Terran_Command_Center) {
                            Base ccBase = Util.getClosestBaseLocation(arg0.getPosition());
                            if (!gs.islandBases.isEmpty() && gs.islandBases.contains(ccBase))
                                gs.islandCCs.put(ccBase, (CommandCenter) arg0);
                            else gs.CCs.put(ccBase, (CommandCenter) arg0);
                            if (gs.getStrategyFromManager().name.equals("BioMechGreedyFE") && Util.getNumberCCs() > 2)
                                gs.getStrategyFromManager().raxPerCC = 3;
                            else if (gs.getStrategyFromManager().name.equals("BioMechGreedyFE") && Util.getNumberCCs() < 3)
                                gs.getStrategyFromManager().raxPerCC = 2;
                            gs.addNewResources(ccBase);
                            if (gs.frameCount != 0 && gs.firstExpand && ccBase.getArea().equals(gs.naturalArea) && !gs.defense)
                                gs.workerTransfer();
                            if (gs.frameCount != 0 && gs.firstExpand) {
                                gs.firstExpand = false;
                                if (gs.getStrategyFromManager().name.equals("14CC")) {
                                    gs.singletonManager.chooseTransitionForEnemy();
                                    transition();
                                }
                                if (gs.naturalChoke != null)
                                    gs.defendPosition = gs.naturalChoke.getCenter().toPosition();
                            }
                            if (((CommandCenter) arg0).getAddon() != null && !gs.CSs.contains(((CommandCenter) arg0).getAddon())) {
                                gs.CSs.add((ComsatStation) ((CommandCenter) arg0).getAddon());
                            }
                            if (gs.frameCount == 0) gs.mainCC = new MutablePair<>(ccBase, arg0);
                        }
                        if (type == UnitType.Terran_Comsat_Station) gs.CSs.add((ComsatStation) arg0);
                        if (type == UnitType.Terran_Bunker) gs.DBs.put((Bunker) arg0, new TreeSet<>());
                        if (type == UnitType.Terran_Engineering_Bay || type == UnitType.Terran_Academy) {
                            gs.UBs.add((ResearchingFacility) arg0);
                        }
                        if (type == UnitType.Terran_Barracks) gs.MBs.add((Barracks) arg0);
                        if (type == UnitType.Terran_Factory) gs.Fs.add((Factory) arg0);
                        if (type == UnitType.Terran_Starport) gs.Ps.add((Starport) arg0);
                        if (type == UnitType.Terran_Science_Facility) gs.UBs.add((ResearchingFacility) arg0);
                        if (type == UnitType.Terran_Control_Tower) gs.UBs.add((ResearchingFacility) arg0);
                        if (type == UnitType.Terran_Armory) gs.UBs.add((ResearchingFacility) arg0);
                        if (type == UnitType.Terran_Supply_Depot) gs.SBs.add((SupplyDepot) arg0);
                        if (type == UnitType.Terran_Machine_Shop) gs.UBs.add((ResearchingFacility) arg0);
                        if (type == UnitType.Terran_Missile_Turret) gs.Ts.add((MissileTurret) arg0);
                        for (Entry<SCV, Building> u : gs.workerTask.entrySet()) {
                            if (u.getValue().equals(arg0)) {
                                gs.workerIdle.add(u.getKey());
                                gs.workerTask.remove(u.getKey());
                                break;
                            }
                        }
                    }
                } else if (type.isWorker()) gs.workerIdle.add((Worker) arg0);
                else if (type == UnitType.Terran_Vulture && !gs.getStrategyFromManager().name.equals("TheNitekat") && !bw.getBWMap().mapHash().equals("666dd28cd3c85223ebc749a481fc281e58221e4a"))
                    gs.agents.put(arg0, new VultureAgent(arg0));
                else if (type == UnitType.Terran_Dropship) {
                    DropShipAgent d = new DropShipAgent(arg0);
                    gs.agents.put(arg0, d);
                } else if (type == UnitType.Terran_Science_Vessel) {
                    VesselAgent v = new VesselAgent(arg0);
                    gs.agents.put(arg0, v);
                } else if (type == UnitType.Terran_Wraith) {
                    if (!gs.getStrategyFromManager().name.equals("PlasmaWraithHell")) {
                        String name = gs.pickShipName();
                        gs.agents.put(arg0, new WraithAgent(arg0, name));
                    }
                } else {
                    gs.myArmy.add(gs.unitStorage.getAllyUnits().get(arg0));
                    if (gs.enemyMainBase != null && gs.silentCartographer.mapCenter.getDistance(gs.enemyMainBase.getLocation()) < arg0.getTilePosition().getDistance(gs.enemyMainBase.getLocation())) {
                        ((MobileUnit) arg0).move(gs.silentCartographer.mapCenter.toPosition());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("onUnitComplete exception");
            e.printStackTrace();
        }
    }

    @Override
    public void onUnitDestroy(Unit arg0) {
        try {
            UnitType type = arg0.getType();
            if (type.isMineralField()) {
                if (gs.mineralsAssigned.containsKey(arg0)) {
                    gs.map.updateMap(arg0.getTilePosition(), type, true);
                    gs.testMap = gs.map.clone();
                    List<Unit> aux = new ArrayList<>();
                    for (Entry<Worker, MineralPatch> w : gs.workerMining.entrySet()) {
                        if (arg0.equals(w.getValue())) {
                            w.getKey().stop(false);
                            gs.workerIdle.add(w.getKey());
                            aux.add(w.getKey());
                        }
                    }
                    for (Unit u : aux) gs.workerMining.remove(u);
                    gs.mineralsAssigned.remove(arg0);
                }
            }
            if (!type.isBuilding() && !type.isRefinery() && type != UnitType.Resource_Vespene_Geyser
                    && type != UnitType.Spell_Scanner_Sweep && !first) {
                gs.playSound("first.mp3");
                first = true;
            }
            if (!type.isNeutral() && (!type.isSpecialBuilding() || type.isRefinery())) {
                if (arg0 instanceof ResourceDepot) gs.baseManager.onDestroy((ResourceDepot) arg0);
                if (arg0 instanceof PlayerUnit && ((PlayerUnit) arg0).getPlayer().isEnemy()) {
                    IntelligenceAgency.onDestroy(arg0, type);
                    if (arg0.equals(gs.chosenUnitToHarass)) gs.chosenUnitToHarass = null;
                    if (type.isBuilding()) {
                        if (!type.isResourceDepot()) gs.map.updateMap(arg0.getTilePosition(), type, true);
                    } else gs.initDefensePosition = arg0.getTilePosition();
                } else if (arg0 instanceof PlayerUnit && ((PlayerUnit) arg0).getPlayer().getId() == self.getId()) {
                    if (gs.ih.getFrameCount() > 0) gs.supplyMan.onDestroy(arg0);
                    if (arg0 instanceof Worker) {
                        if (gs.getStrategyFromManager().name.equals("ProxyBBS") || gs.getStrategyFromManager().name.equals("ProxyEightRax")) {
                            UnitInfo ally = gs.unitStorage.getAllyUnits().get(arg0);
                            if (ally != null) gs.myArmy.remove(ally);
                        }
                        for (SCV r : gs.repairerTask.keySet()) {
                            if (r.equals(arg0)) {
                                gs.workerIdle.add((Worker) arg0);
                                gs.repairerTask.remove(r);
                                break;
                            }
                        }
                        gs.workerIdle.remove(arg0);
                        if (arg0.equals(gs.naughtySCV)) gs.naughtySCV = null;
                        if (arg0.equals(gs.chosenScout)) gs.chosenScout = null;
                        if (arg0.equals(gs.chosenHarasser)) {
                            gs.chosenHarasser = null;
                            gs.chosenUnitToHarass = null;
                            getGs().firstScout = false;
                        }
                        if (arg0.equals(gs.chosenWorker)) gs.chosenWorker = null;
                        if (arg0.equals(gs.chosenRepairer)) gs.chosenRepairer = null;
                        for (Worker u : gs.workerDefenders.keySet()) {
                            if (arg0.equals(u)) {
                                gs.workerDefenders.remove(u);
                                break;
                            }
                        }
                        if (gs.workerMining.containsKey(arg0)) {
                            MineralPatch mineral = gs.workerMining.get(arg0);
                            gs.workerMining.remove(arg0);
                            if (gs.mineralsAssigned.containsKey(mineral)) {
                                gs.mining--;
                                gs.mineralsAssigned.put(mineral, gs.mineralsAssigned.get(mineral) - 1);
                            }
                        }
                        if (gs.workerGas.containsKey(arg0)) {
                            GasMiningFacility aux = gs.workerGas.get(arg0);
                            Integer auxInt = gs.refineriesAssigned.get(aux);
                            gs.refineriesAssigned.put(aux, auxInt - 1);
                            gs.workerGas.remove(arg0);
                        }
                        if (gs.workerTask.containsKey(arg0)) {
                            if (!gs.islandBases.isEmpty() && gs.workerTask.get(arg0) instanceof CommandCenter) {
                                Base ccBase = Util.getClosestBaseLocation(gs.workerTask.get(arg0).getPosition());
                                if (gs.islandBases.contains(ccBase)) gs.islandExpand = false;
                            }
                            gs.buildingLot.add(gs.workerTask.get(arg0));
                            gs.workerTask.remove(arg0);
                        }
                        if (gs.workerBuild.containsKey(arg0)) {
                            if (gs.workerBuild.get(arg0).first == UnitType.Terran_Command_Center) {
                                if (bwem.getMap().getArea(gs.workerBuild.get(arg0).second).equals(gs.naturalArea)) {
                                    Bunker b = !gs.DBs.isEmpty() ? gs.DBs.keySet().iterator().next() : null;
                                    if (b != null) gs.defendPosition = b.getPosition();
                                    else gs.defendPosition = gs.mainChoke.getCenter().toPosition();
                                }
                                if (!gs.islandBases.isEmpty()) {
                                    Base ccBase = Util.getClosestBaseLocation(arg0.getPosition());
                                    if (gs.islandBases.contains(ccBase)) gs.islandExpand = false;
                                }
                                gs.deltaCash.first -= gs.workerBuild.get(arg0).first.mineralPrice();
                                gs.deltaCash.second -= gs.workerBuild.get(arg0).first.gasPrice();
                                gs.workerBuild.remove(arg0);
                            }
                        }
                    } else if (type.isBuilding()) {
                        if (type != UnitType.Terran_Command_Center) {
                            gs.map.updateMap(arg0.getTilePosition(), type, true);
                        }
                        for (Entry<SCV, Mechanical> r : gs.repairerTask.entrySet()) {
                            if (r.getValue().equals(arg0)) {
                                gs.workerIdle.add(r.getKey());
                                gs.repairerTask.remove(r.getKey());
                                break;
                            }
                        }
                        if (arg0.equals(gs.proxyBuilding)) gs.proxyBuilding = null;
                        for (Entry<SCV, Building> w : gs.workerTask.entrySet()) {
                            if (w.getValue().equals(arg0)) {
                                if (w.getValue() instanceof CommandCenter) {
                                    if (bwem.getMap().getArea(w.getValue().getTilePosition()).equals(gs.naturalArea)) {
                                        Bunker b = gs.DBs.keySet().iterator().next();
                                        if (b != null) gs.defendPosition = b.getPosition();
                                        else gs.defendPosition = gs.mainChoke.getCenter().toPosition();
                                    }
                                    if (!gs.islandBases.isEmpty()) {
                                        Base ccBase = Util.getClosestBaseLocation(arg0.getPosition());
                                        if (gs.islandBases.contains(ccBase)) gs.islandExpand = false;
                                    }
                                }
                                gs.workerTask.remove(w.getKey());
                                gs.workerIdle.add(w.getKey());
                                break;
                            }
                        }
                        for (Unit w : gs.buildingLot) {
                            if (w.equals(arg0)) {
                                if (w instanceof CommandCenter
                                        && bwem.getMap().getArea(w.getTilePosition()).equals(gs.naturalArea)) {
                                    Bunker b = !gs.DBs.isEmpty() ? gs.DBs.keySet().iterator().next() : null;
                                    if (b != null) gs.defendPosition = b.getPosition();
                                    else gs.defendPosition = gs.mainChoke.getCenter().toPosition();
                                }
                                gs.buildingLot.remove(w);
                                break;
                            }
                        }
                        for (CommandCenter u : gs.CCs.values()) {
                            if (u.equals(arg0)) {
                                gs.removeResources(arg0);
                                if (u.getAddon() != null) gs.CSs.remove(u.getAddon());
                                if (bwem.getMap().getArea(arg0.getTilePosition()).equals(gs.naturalArea)) {
                                    gs.defendPosition = gs.mainChoke.getCenter().toPosition();
                                }
                                gs.CCs.remove(Util.getClosestBaseLocation(arg0.getPosition()));
                                if (arg0.equals(gs.mainCC.second)) {
                                    if (gs.CCs.size() > 0) {
                                        for (Unit c : gs.CCs.values()) {
                                            if (!c.equals(arg0)) {
                                                gs.mainCC = new MutablePair<>(Util.getClosestBaseLocation(u.getPosition()), u);
                                                break;
                                            }
                                        }
                                    } else {
                                        gs.mainCC = null;
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                        for (CommandCenter u : gs.islandCCs.values()) {
                            if (u.equals(arg0)) {
                                gs.removeResources(arg0);
                                if (u.getAddon() != null) gs.CSs.remove(u.getAddon());
                                gs.islandCCs.remove(Util.getClosestBaseLocation(arg0.getPosition()));
                                break;
                            }
                        }
                        gs.CSs.remove(arg0);
                        gs.Fs.remove(arg0);
                        gs.MBs.remove(arg0);
                        if (arg0.equals(gs.proxyBuilding)) {
                            gs.proxyBuilding = null;
                            gs.getStrategyFromManager().trainUnits.remove(UnitType.Terran_Vulture);
                        }
                        if (arg0 instanceof ResearchingFacility) gs.UBs.remove(arg0);
                        gs.SBs.remove(arg0);
                        gs.Ts.remove(arg0);
                        gs.Ps.remove(arg0);
                        if (type == UnitType.Terran_Bunker && gs.DBs.containsKey(arg0)) {
                            gs.myArmy.addAll(gs.DBs.get(arg0));
                            gs.DBs.remove(arg0);
                        }
                        if (type.isRefinery() && gs.refineriesAssigned.containsKey(arg0)) {
                            List<Unit> aux = new ArrayList<>();
                            for (Entry<Worker, GasMiningFacility> w : gs.workerGas.entrySet()) {
                                if (arg0.equals(w.getValue())) {
                                    gs.workerIdle.add(w.getKey());
                                    aux.add(w.getKey());
                                }
                            }
                            for (Unit u : aux) gs.workerGas.remove(u);
                            gs.refineriesAssigned.remove(arg0);
                            for (VespeneGeyser g : gs.vespeneGeysers.keySet()) {
                                if (g.getTilePosition().equals(arg0.getTilePosition())) gs.vespeneGeysers.put(g, false);
                            }
                        }
                        gs.testMap = gs.map.clone();
                    } else if (type == UnitType.Terran_Vulture) gs.agents.remove(arg0);
                    else if (type == UnitType.Terran_Dropship) gs.agents.remove(arg0);
                    else if (type == UnitType.Terran_Science_Vessel) gs.agents.remove(arg0);
                    else if (type == UnitType.Terran_Wraith && !gs.getStrategyFromManager().name.equals("PlasmaWraithHell") && gs.agents.containsKey(arg0)) {
                        String wraith = ((WraithAgent) gs.agents.get(arg0)).name;
                        gs.shipNames.add(wraith);
                        gs.agents.remove(arg0);
                    }
                    UnitInfo ally = gs.unitStorage.getAllyUnits().get(arg0);
                    if (ally != null) gs.myArmy.remove(ally);
                }
                OnUnitAction destroyAction= new OnUnitDestroy(arg0,gs.unitStorage);
            	destroyAction.action();
            }
        } catch (Exception e) {
            System.err.println("OnUnitDestroy Exception");
            e.printStackTrace();
        }
    }

    @Override
    public void onUnitMorph(Unit arg0) {
        try {
            UnitType type = arg0.getType();
            if (arg0 instanceof PlayerUnit && ((PlayerUnit) arg0).getPlayer().isEnemy()) {
            	OnUnitAction morphAction= new OnUnitMorph(arg0,gs.unitStorage);
            	morphAction.action();
                if (!type.isBuilding() && (type.canAttack() || type.isSpellcaster() || type.spaceProvided() > 0)) {
                    gs.enemyCombatUnitMemory.add(arg0);
                }
            }
            if (arg0 instanceof Refinery && ((PlayerUnit) arg0).getPlayer().equals(self)) {
                for (Entry<GasMiningFacility, Integer> r : gs.refineriesAssigned.entrySet()) {
                    if (r.getKey().getTilePosition().equals(arg0.getTilePosition())) {
                        gs.map.updateMap(arg0.getTilePosition(), type, false);
                        gs.testMap = gs.map.clone();
                        break;
                    }
                }
                for (Entry<SCV, MutablePair<UnitType, TilePosition>> u : gs.workerBuild.entrySet()) {
                    if (u.getKey().equals(((Building) arg0).getBuildUnit()) && u.getValue().first == type) {
                        gs.workerBuild.remove(u.getKey());
                        gs.workerTask.put(u.getKey(), (Building) arg0);
                        gs.deltaCash.first -= type.mineralPrice();
                        gs.deltaCash.second -= type.gasPrice();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("onUnitMorph Exception");
            e.printStackTrace();
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
        gs.enemyCombatUnitMemory.remove(arg0);
    }

    @Override
    public void onUnitRenegade(Unit arg0) {
        if (arg0 instanceof PlayerUnit && ((PlayerUnit) arg0).getPlayer().equals(self)) {
        	OnUnitAction completeAction = new OnUnitComplete(arg0,gs.unitStorage);
        	completeAction.action();
        }
    }

    @Override
    public void onUnitShow(Unit arg0) {
        try {
            cherryVisDumper.onUnitShow(arg0);
            if (arg0 instanceof MineralPatch || arg0 instanceof VespeneGeyser || arg0 instanceof SpecialBuilding ||
                    arg0 instanceof Critter || arg0 instanceof ScannerSweep) return;
            UnitType type = arg0.getType();
            Player p = ((PlayerUnit) arg0).getPlayer();
            if (p != null && p.isEnemy()) {
            	OnUnitAction showAction= new OnUnitShow(arg0,gs.unitStorage);
            	showAction.action();
                IntelligenceAgency.onShow(arg0, type);
                if (gs.enemyRace == Race.Unknown && getGs().getIH().enemies().size() == 1) {
                    gs.enemyRace = type.getRace();
                    if (gs.enemyRace == Race.Zerg && gs.getStrategyFromManager().trainUnits.contains(UnitType.Terran_Firebat))
                        gs.maxBats = 3;
                }
                if (!type.isBuilding() && (type.canAttack() || type.isSpellcaster() || type.spaceProvided() > 0)) {
                    gs.enemyCombatUnitMemory.add(arg0);
                }
                if (type.isBuilding() && !gs.unitStorage.getEnemyUnits().containsKey(arg0)) {
                    gs.map.updateMap(arg0.getTilePosition(), type, false);
                }
            }
        } catch (Exception e) {
            System.err.println("OnUnitShow Exception");
            e.printStackTrace();
        }

    }
}
