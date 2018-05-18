package ecgberht;

import bwem.BWEM;
import bwta.BWTA;
import ecgberht.AddonBuild.BuildAddon;
import ecgberht.AddonBuild.CheckResourcesAddon;
import ecgberht.AddonBuild.ChooseComsatStation;
import ecgberht.AddonBuild.ChooseMachineShop;
import ecgberht.Agents.VultureAgent;
import ecgberht.Attack.CheckArmy;
import ecgberht.Attack.ChooseAttackPosition;
import ecgberht.Build.Build;
import ecgberht.Build.CheckWorkerBuild;
import ecgberht.BuildingLot.CheckBuildingsLot;
import ecgberht.BuildingLot.ChooseBlotWorker;
import ecgberht.BuildingLot.ChooseBuildingLot;
import ecgberht.BuildingLot.FinishBuilding;
import ecgberht.Bunker.ChooseBunkerToLoad;
import ecgberht.Bunker.ChooseMarineToEnter;
import ecgberht.Bunker.EnterBunker;
import ecgberht.CombatStim.CheckStimResearched;
import ecgberht.CombatStim.Stim;
import ecgberht.Config.ConfigManager;
import ecgberht.Defense.CheckPerimeter;
import ecgberht.Defense.ChooseDefensePosition;
import ecgberht.Defense.SendDefenders;
import ecgberht.Expansion.*;
import ecgberht.Harass.*;
import ecgberht.MoveToBuild.*;
import ecgberht.Recollection.CollectGas;
import ecgberht.Recollection.CollectMineral;
import ecgberht.Recollection.FreeWorker;
import ecgberht.Repair.CheckBuildingFlames;
import ecgberht.Repair.ChooseRepairer;
import ecgberht.Repair.Repair;
import ecgberht.Scanner.CheckScan;
import ecgberht.Scanner.Scan;
import ecgberht.Scouting.*;
import ecgberht.Training.*;
import ecgberht.Upgrade.*;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.composite.Selector;
import org.iaie.btree.task.composite.Sequence;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.*;
import org.openbw.bwapi4j.type.*;
import org.openbw.bwapi4j.unit.*;
import org.openbw.bwapi4j.util.Pair;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

public class Ecgberht implements BWEventListener {

    private BehavioralTree addonBuildTree;
    private BehavioralTree attackTree;
    private BehavioralTree botherTree;
    private BehavioralTree buildingLotTree;
    private BehavioralTree buildTree;
    private BehavioralTree bunkerTree;
    private BehavioralTree collectTree;
    private BehavioralTree combatStimTree;
    private BehavioralTree defenseTree;
    private BehavioralTree expandTree;
    private BehavioralTree moveBuildTree;
    private BehavioralTree repairTree;
    private BehavioralTree scannerTree;
    private BehavioralTree scoutingTree;
    private BehavioralTree trainTree;
    private BehavioralTree upgradeTree;
    private boolean first = false;
    private Player self;
    private static BW bw;
    private static InteractionHandler ih;
    private static GameState gs;
    private BWTA bwta;
    private BWEM bwem;

    private void run() {
        Ecgberht.bw = new BW(this);
        Ecgberht.bw.startGame();
    }

    public static void main(String[] args) {
        new Ecgberht().run();
    }

    public static BW getGame() {
        return bw;
    }

    public static InteractionHandler getIH() {
        return ih;
    }

    public static GameState getGs() {
        return gs;
    }

    @Override
    public void onStart() {
        ConfigManager.readConfig();
        if(ConfigManager.getConfig().debugText){
            // Disables System.err and System.Out
            OutputStream output = null;
            try {
                output = new FileOutputStream("NUL:");
            } catch (FileNotFoundException e) {
                //e.printStackTrace();
            }
            PrintStream nullOut = new PrintStream(output);
            System.setErr(nullOut);
            System.setOut(nullOut);
        }

        self = bw.getInteractionHandler().self();
        ih = bw.getInteractionHandler();
        // game.enableFlag(1);
        // game.setLocalSpeed(0);
        System.out.println("Analyzing map...");
        ;
        bwta = new BWTA();
        bwta.analyze();
        System.out.println("Map data ready");
        //observer.toggle();
        bwem = new BWEM(bw);
        bwem.initialize();
        bwem.getMap().assignStartingLocationsToSuitableBases();
        gs = new GameState(bw, bwta, bwem);
        gs.initEnemyRace();
        gs.readOpponentInfo();
        if (gs.enemyRace == Race.Zerg) {
            if (gs.EI.naughty) {
                gs.playSound("rushed.mp3");
            }
        }
        gs.EI.naughty = true;
        gs.strat = gs.initStrat();
        gs.initStartLocations();
        gs.initBaseLocations();
        gs.initBlockingMinerals();
        gs.checkBasesWithBLockingMinerals();
        gs.initClosestChoke();

        // Trees Initializations
        initCollectTree();
        initTrainTree();
        initMoveBuildTree();
        initBuildTree();
        initScoutingTree();
        initAttackTree();
        initDefenseTree();
        initUpgradeTree();
        initRepairTree();
        initExpandTree();
        initCombatStimTree();
        initAddonBuildTree();
        initBuildingLotTree();
        initBunkerTree();
        initScanTree();
        initHarassTree();
    }

    public void transition() {
        initTrainTree();
        initMoveBuildTree();
        initUpgradeTree();
        initAddonBuildTree();
    }

    private void initTrainTree() {
        ChooseSCV cSCV = new ChooseSCV("Choose SCV", gs);
        ChooseMarine cMar = new ChooseMarine("Choose Marine", gs);
        ChooseMedic cMed = new ChooseMedic("Choose Medic", gs);
        ChooseTank cTan = new ChooseTank("Choose Tank", gs);
        ChooseVulture cVul = new ChooseVulture("Choose vulture", gs);
        CheckResourcesUnit cr = new CheckResourcesUnit("Check Cash", gs);
        TrainUnit tr = new TrainUnit("Train SCV", gs);
        Selector<GameHandler> chooseUnit = new Selector<GameHandler>("Choose Recruit", cSCV);

        if (gs.strat.trainUnits.contains(UnitType.Terran_Siege_Tank_Tank_Mode)) {
            chooseUnit.addChild(cTan);
        }
        if (gs.strat.trainUnits.contains(UnitType.Terran_Vulture)) {
            chooseUnit.addChild(cVul);
        }
        if (gs.strat.trainUnits.contains(UnitType.Terran_Medic)) {
            chooseUnit.addChild(cMed);
        }
        if (gs.strat.trainUnits.contains(UnitType.Terran_Marine)) {
            chooseUnit.addChild(cMar);
        }
        Sequence train = new Sequence("Train", chooseUnit, cr, tr);
        trainTree = new BehavioralTree("Training Tree");
        trainTree.addChild(train);
    }

    private void initMoveBuildTree() {
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
        CheckResourcesBuilding crb = new CheckResourcesBuilding("Check Cash", gs);
        ChoosePosition cp = new ChoosePosition("Choose Position", gs);
        ChooseWorker cw = new ChooseWorker("Choose Worker", gs);
        Move m = new Move("Move to chosen building position", gs);
        Selector<GameHandler> chooseBuildingBuild = new Selector<GameHandler>("Choose Building to build", cSup);
        if (gs.strat.bunker) {
            chooseBuildingBuild.addChild(cBun);
        }
        chooseBuildingBuild.addChild(cTur);
        chooseBuildingBuild.addChild(cRef);
        if (gs.strat.buildUnits.contains(UnitType.Terran_Academy)) {
            chooseBuildingBuild.addChild(cAca);
        }
        if (gs.strat.buildUnits.contains(UnitType.Terran_Engineering_Bay)) {
            chooseBuildingBuild.addChild(cBay);
        }
        if (gs.strat.buildUnits.contains(UnitType.Terran_Factory)) {
            chooseBuildingBuild.addChild(cFar);
        }
        if (gs.strat.buildUnits.contains(UnitType.Terran_Starport)) {
            chooseBuildingBuild.addChild(cPor);
        }
        if (gs.strat.buildUnits.contains(UnitType.Terran_Science_Facility)) {
            chooseBuildingBuild.addChild(cSci);
        }
        chooseBuildingBuild.addChild(cBar);
        Sequence move = new Sequence("Move", chooseBuildingBuild, cp, cw, crb, m);
        moveBuildTree = new BehavioralTree("Building Tree");
        moveBuildTree.addChild(move);
    }

    private void initBuildTree() {
        CheckWorkerBuild cWB = new CheckWorkerBuild("Check WorkerBuild", gs);
        Build b = new Build("Build", gs);
        Sequence build = new Sequence("Build", cWB, b);
        buildTree = new BehavioralTree("Build Tree");
        buildTree.addChild(build);
    }

    private void initScoutingTree() {
        CheckScout cSc = new CheckScout("Check Scout", gs);
        ChooseScout chSc = new ChooseScout("Choose Scouter", gs);
        SendScout sSc = new SendScout("Send Scout", gs);
        CheckVisibleBase cVB = new CheckVisibleBase("Check visible Base", gs);
        CheckEnemyBaseVisible cEBV = new CheckEnemyBaseVisible("Check Enemy Base Visible", gs);
        Sequence scoutFalse = new Sequence("Scout False", cSc, chSc, sSc);
        Selector<GameHandler> EnemyFound = new Selector<GameHandler>("Enemy found in base location", cEBV, sSc);
        Sequence scoutTrue = new Sequence("Scout True", cVB, EnemyFound);
        Selector<GameHandler> Scouting = new Selector<GameHandler>("Select Scouting Plan", scoutFalse, scoutTrue);
        scoutingTree = new BehavioralTree("Movement Tree");
        scoutingTree.addChild(Scouting);
    }

    private void initAttackTree() {
        CheckArmy cA = new CheckArmy("Check Army", gs);
        ChooseAttackPosition cAP = new ChooseAttackPosition("Choose Attack Position", gs);
        Sequence Attack = new Sequence("Attack", cA, cAP);
        attackTree = new BehavioralTree("Attack Tree");
        attackTree.addChild(Attack);
    }

    private void initDefenseTree() {
        CheckPerimeter cP = new CheckPerimeter("Check Perimeter", gs);
        ChooseDefensePosition cDP = new ChooseDefensePosition("Choose Defence Position", gs);
        SendDefenders sD = new SendDefenders("Send Defenders", gs);
        Sequence Defense = new Sequence("Defence", cP, cDP, sD);
        defenseTree = new BehavioralTree("Defence Tree");
        defenseTree.addChild(Defense);
    }

    private void initUpgradeTree() {
        CheckResourcesUpgrade cRU = new CheckResourcesUpgrade("Check Resources Upgrade", gs);
        ChooseArmorInfUp cAIU = new ChooseArmorInfUp("Choose Armor inf upgrade", gs);
        ChooseWeaponInfUp cWIU = new ChooseWeaponInfUp("Choose Weapon inf upgrade", gs);
        ChooseMarineRange cMR = new ChooseMarineRange("Choose Marine Range upgrade", gs);
        ChooseStimUpgrade cSU = new ChooseStimUpgrade("Choose Stimpack upgrade", gs);
        ChooseSiegeMode cSM = new ChooseSiegeMode("Choose Siege Mode", gs);
        ResearchUpgrade rU = new ResearchUpgrade("Research Upgrade", gs);
        Selector<GameHandler> ChooseUP = new Selector<GameHandler>("Choose Upgrade");
        if (gs.strat.upgradesToResearch.contains(UpgradeType.Terran_Infantry_Weapons)) {
            ChooseUP.addChild(cWIU);
        }
        if (gs.strat.upgradesToResearch.contains(UpgradeType.Terran_Infantry_Armor)) {
            ChooseUP.addChild(cAIU);
        }
        if (gs.strat.techToResearch.contains(TechType.Stim_Packs)) {
            ChooseUP.addChild(cSU);
        }
        if (gs.strat.upgradesToResearch.contains(UpgradeType.U_238_Shells)) {
            ChooseUP.addChild(cMR);
        }
        if (gs.strat.techToResearch.contains(TechType.Tank_Siege_Mode)) {
            ChooseUP.addChild(cSM);
        }
        Sequence Upgrader = new Sequence("Upgrader", ChooseUP, cRU, rU);
        upgradeTree = new BehavioralTree("Technology");
        upgradeTree.addChild(Upgrader);
    }

    private void initRepairTree() {
        CheckBuildingFlames cBF = new CheckBuildingFlames("Check building in flames", gs);
        ChooseRepairer cR = new ChooseRepairer("Choose Repairer", gs);
        Repair R = new Repair("Repair Building", gs);
        Sequence Repair = new Sequence("Repair", cBF, cR, R);
        repairTree = new BehavioralTree("RepairTree");
        repairTree.addChild(Repair);
    }

    private void initExpandTree() {
        CheckExpansion cE = new CheckExpansion("Check Expansion", gs);
        CheckResourcesCC cRCC = new CheckResourcesCC("Check Resources CC", gs);
        ChooseBaseLocation cBL = new ChooseBaseLocation("Choose Base Location", gs);
        ChooseBuilderBL cBBL = new ChooseBuilderBL("Chose Builder Base Location", gs);
        SendBuilderBL sBBL = new SendBuilderBL("Send Builder To BL", gs);
        CheckVisibleBL cVBL = new CheckVisibleBL("Check Visible BL", gs);
        Expand E = new Expand("Expand", gs);
        Sequence Expander = new Sequence("Expander", cE, cRCC, cBL, cBBL, sBBL, cVBL, E);
        expandTree = new BehavioralTree("Expand Tree");
        expandTree.addChild(Expander);
    }

    private void initCombatStimTree() {
        CheckStimResearched cSR = new CheckStimResearched("Check if Stim Packs researched", gs);
        Stim S = new Stim("Use Stim", gs);
        Sequence Stimmer = new Sequence("Stimmer", cSR, S);
        combatStimTree = new BehavioralTree("CombatStim Tree");
        combatStimTree.addChild(Stimmer);
    }

    private void initAddonBuildTree() {
        BuildAddon bA = new BuildAddon("Build Addon", gs);
        CheckResourcesAddon cRA = new CheckResourcesAddon("Check Resources Addon", gs);
        ChooseComsatStation cCS = new ChooseComsatStation("Choose Comsat Station", gs);
        ChooseMachineShop cMS = new ChooseMachineShop("Choose Machine Shop", gs);
        Selector<GameHandler> ChooseAddon = new Selector<GameHandler>("Choose Addon");
        if (gs.strat.buildAddons.contains(UnitType.Terran_Machine_Shop)) {
            ChooseAddon.addChild(cMS);
        }
        if (gs.strat.buildAddons.contains(UnitType.Terran_Comsat_Station)) {
            ChooseAddon.addChild(cCS);
        }
        Sequence Addon = new Sequence("Addon", ChooseAddon, cRA, bA);
        addonBuildTree = new BehavioralTree("Addon Build Tree");
        addonBuildTree.addChild(Addon);
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

    private void initBunkerTree() {
        ChooseBunkerToLoad cBu = new ChooseBunkerToLoad("Choose Bunker to Load", gs);
        EnterBunker eB = new EnterBunker("Enter bunker", gs);
        ChooseMarineToEnter cMTE = new ChooseMarineToEnter("Choose Marine To Enter", gs);
        Sequence Bunker = new Sequence("Bunker", cBu, cMTE, eB);
        bunkerTree = new BehavioralTree("Bunker Tree");
        bunkerTree.addChild(Bunker);
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
        ChooseWorkerToHarass cWTH = new ChooseWorkerToHarass("Check Worker to Harass", gs);
        ChooseBuilderToHarass cWTB = new ChooseBuilderToHarass("Check Worker to Harass", gs);
        CheckHarasserAttacked cHA = new CheckHarasserAttacked("Check Harasser Attacked", gs);
        ChooseBuildingToHarass cBTH = new ChooseBuildingToHarass("Check Building to Harass", gs);
        HarassWorker hW = new HarassWorker("Bother SCV", gs);
        Selector<GameHandler> bOw = new Selector<GameHandler>("Choose Builder or Worker or Building", cWTH, cWTB, cBTH);
        Sequence harass = new Sequence("Harass", cH, cHA, bOw, hW);
        botherTree = new BehavioralTree("Harass Tree");
        botherTree.addChild(harass);
    }

    private void initCollectTree() {
        CollectGas cg = new CollectGas("Collect Gas", gs);
        CollectMineral cm = new CollectMineral("Collect Mineral", gs);
        FreeWorker fw = new FreeWorker("No Union", gs);
        Selector<GameHandler> collectResources = new Selector<GameHandler>("Collect Melted Cash", cg, cm);
        Sequence collect = new Sequence("Collect", fw, collectResources);
        collectTree = new BehavioralTree("Recollection Tree");
        collectTree.addChild(collect);
    }

    @Override
    public void onFrame() {
        try {

//			for(Unit u : bw.getUnits(self)){
//				if(u instanceof ComsatStation && ((ComsatStation) u).isCompleted()) {
//					System.out.println("Comsat with energy: " + ((ComsatStation)u).getEnergy());
//				}
//			}

            long frameStart = System.currentTimeMillis();
            gs.frameCount = ih.getFrameCount();
            if (gs.frameCount == 1000) gs.sendCustomMessage();
            gs.print(gs.naturalRegion.getTop().toTilePosition(), Color.RED);
            gs.fix();
            gs.inMapUnits = new InfluenceMap(bw, self, bw.getBWMap().mapHeight(), bw.getBWMap().mapWidth());
            gs.updateEnemyBuildingsMemory();
            gs.runAgents();
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
            if (gs.strat.name == "ProxyBBS") {
                gs.checkWorkerMilitia();
            }
            gs.siegeTanks();
            defenseTree.run();
            attackTree.run();
            gs.updateSquadOrderAndMicro();
            combatStimTree.run();
            gs.checkMainEnemyBase();
            gs.mergeSquads();
            if (ih.getFrameCount() < 24 * 150 && gs.enemyBase != null && gs.enemyRace == Race.Zerg && !gs.EI.naughty) {
                boolean found_pool = false;
                int drones = IntelligenceAgency.getNumDrones();
                for (EnemyBuilding u : gs.enemyBuildingMemory.values()) {
                    if (u.type == UnitType.Zerg_Spawning_Pool) {
                        found_pool = true;
                        break;
                    }
                }
                if (found_pool && drones <= 5) {
                    gs.EI.naughty = true;
                    ih.sendText("Bad zerg!, bad!");
                    gs.playSound("rushed.mp3");
                }
            }
            if (gs.frameCount > 0 && gs.frameCount % 5 == 0) {
                gs.mineralLocking();
            }
            gs.printer();
            long frameEnd = System.currentTimeMillis();
            long frameTotal = frameEnd - frameStart;
            gs.totalTime += frameTotal;
            bw.getMapDrawer().drawTextScreen(10, 65, "frameTime(ms): " + (String.valueOf(frameTotal)));
        } catch (Exception e) {
            System.err.println("onFrame Exception");
            e.printStackTrace();
        }

    }

    @Override
    public void onEnd(boolean arg0) {
        System.out.println("Avg. frameTime(ms): " + gs.totalTime / gs.frameCount);
        String name = ih.enemy().getName();
        gs.EI.updateStrategyOpponentHistory(gs.strat.name, gs.mapSize, arg0);
        if (arg0) {
            gs.EI.wins++;
            ih.sendText("gg wp " + name);
        } else {
            gs.EI.losses++;
            ih.sendText("gg wp! " + name + ", next game I will win!");
        }
        gs.writeOpponentInfo(name);
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

    }

    @Override
    public void onUnitCreate(Unit arg0) {
        try {
            if (arg0 instanceof MineralPatch || arg0 instanceof VespeneGeyser || arg0 instanceof SpecialBuilding || arg0 instanceof Critter)
                return;
            PlayerUnit pU = (PlayerUnit) arg0;
            UnitType type = Util.getType(pU);
            if (!type.isNeutral() && !type.isSpecialBuilding()) {
                if (arg0 instanceof Building) {
                    gs.inMap.updateMap(arg0, false);
                    if (pU.getPlayer().getId() == self.getId()) {
                        if (!(arg0 instanceof CommandCenter)) {
                            gs.map.updateMap(arg0.getTilePosition(), type, false);
                            gs.testMap = gs.map.clone();
                        }
                        for (Entry<SCV, Pair<UnitType, TilePosition>> u : gs.workerBuild.entrySet()) {
                            if (u.getKey().equals(((Building) arg0).getBuildUnit()) && u.getValue().first.equals(type)) { // TODO use Map
                                gs.workerTask.put(u.getKey(), (Building) arg0);
                                gs.deltaCash.first -= type.mineralPrice();
                                gs.deltaCash.second -= type.gasPrice();
                                gs.workerBuild.remove(u.getKey());
                                break;
                            }
                        }
                    }
                } else if (arg0 instanceof Vulture && pU.getPlayer().getId() == self.getId()) {
                    gs.vulturesTrained++;
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
            if (arg0 instanceof MineralPatch || arg0 instanceof VespeneGeyser || arg0 instanceof SpecialBuilding || arg0 instanceof Critter) {
                return;
            }
            PlayerUnit pU = (PlayerUnit) arg0;
            UnitType type = Util.getType(pU);
            if (!type.isNeutral() && pU.getPlayer().getId() == self.getId()) {

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
                                gs.workerTask.remove(u.getKey());
                                gs.workerGas.put(u.getKey(), (GasMiningFacility) arg0);
                                break;
                            }
                        }
                        gs.refineriesAssigned.put((GasMiningFacility) arg0, 1);

                        gs.builtRefinery++;
                    } else {
                        if (type == UnitType.Terran_Command_Center) {
                            gs.CCs.put(bwem.getMap().getArea(arg0.getTilePosition()).getTopLeft().toPosition(), (CommandCenter) arg0);
                            gs.addNewResources(arg0);
                            if (((CommandCenter) arg0).getAddon() != null && !gs.CSs.contains(((CommandCenter) arg0).getAddon())) {
                                gs.CSs.add((ComsatStation) ((CommandCenter) arg0).getAddon());
                            }
                            if (gs.frameCount == 0) {
                                gs.MainCC = arg0;
                            }
                            gs.builtCC++;
                        }
                        if (type == UnitType.Terran_Comsat_Station) {
                            gs.CSs.add((ComsatStation) arg0);
                        }
                        if (type == UnitType.Terran_Bunker) {
                            gs.DBs.put((Bunker) arg0, new HashSet<Unit>());
                        }
                        if (type == UnitType.Terran_Engineering_Bay || type == UnitType.Terran_Academy) {
                            gs.UBs.add((ResearchingFacility) arg0);
                        }
                        if (type == UnitType.Terran_Barracks) {
                            gs.MBs.add((Barracks) arg0);
                        }
                        if (type == UnitType.Terran_Factory) {
                            gs.Fs.add((Factory) arg0);
                        }
                        if (type == UnitType.Terran_Starport) {
                            gs.Ps.add((Starport) arg0);
                        }
                        if (type == UnitType.Terran_Science_Facility) {
                            gs.UBs.add((ResearchingFacility) arg0);
                        }
                        if (type == UnitType.Terran_Supply_Depot) {
                            gs.SBs.add((SupplyDepot) arg0);
                        }
                        if (type == UnitType.Terran_Machine_Shop) {
                            gs.UBs.add((ResearchingFacility) arg0);
                        }
                        if (type == UnitType.Terran_Missile_Turret) {
                            gs.Ts.add((MissileTurret) arg0);
                        }
                        for (Entry<SCV, Building> u : gs.workerTask.entrySet()) {
                            if (u.getValue().equals(arg0)) {
                                gs.workerTask.remove(u.getKey());
                                gs.workerIdle.add(u.getKey());
                                break;
                            }
                        }
                    }
                } else {
                    if (type.isWorker()) {
                        gs.workerIdle.add((Worker) arg0);
                        gs.trainedWorkers++;
                    } else {
                        if (type == UnitType.Terran_Siege_Tank_Tank_Mode) {
                            if (!gs.TTMs.containsKey(arg0)) {
                                String nombre = gs.addToSquad(arg0);
                                gs.TTMs.put(arg0, nombre);
                                if (!gs.DBs.isEmpty()) {
                                    ((MobileUnit) arg0).attack(gs.DBs.keySet().iterator().next().getPosition());
                                } else if (gs.closestChoke != null) {
                                    ((MobileUnit) arg0).attack(gs.closestChoke.getCenter().toPosition());
                                } else {
                                    ((MobileUnit) arg0).attack(Util.getClosestChokepoint(self.getStartLocation().toPosition()).getCenter().toPosition());
                                }
                            } else {
                                Squad tankS = gs.squads.get(gs.TTMs.get(arg0));
                                Position beforeSiege = null;
                                if (tankS != null) {
                                    beforeSiege = tankS.attack;
                                }
                                if (beforeSiege != null && beforeSiege != null) {
                                    ((MobileUnit) arg0).attack(beforeSiege);
                                }
                            }
                        } else if (type == UnitType.Terran_Vulture) {
                            gs.agents.add(new VultureAgent(arg0));
                        } else if (type == UnitType.Terran_Marine || type == UnitType.Terran_Medic) {
                            gs.addToSquad(arg0);
                            if (gs.strat.name != "ProxyBBS") {
                                if (!gs.EI.naughty || gs.enemyRace != Race.Zerg) {
                                    if (!gs.DBs.isEmpty()) {
                                        ((MobileUnit) arg0).attack(gs.DBs.keySet().iterator().next().getPosition());
                                    } else if (gs.closestChoke != null) {
                                        ((MobileUnit) arg0).attack(gs.closestChoke.getCenter().toPosition());
                                    } else {
                                        ((MobileUnit) arg0).attack(Util.getClosestChokepoint(self.getStartLocation().toPosition()).getCenter().toPosition());
                                    }
                                }
                            } else {
                                if (new TilePosition(bw.getBWMap().mapWidth() / 2, bw.getBWMap().mapHeight() / 2).getDistance(gs.enemyBase.getLocation()) < arg0.getTilePosition().getDistance(gs.enemyBase.getLocation())) {
                                    ((MobileUnit) arg0).attack(new TilePosition(bw.getBWMap().mapWidth() / 2, bw.getBWMap().mapHeight() / 2).toPosition());
                                }
                            }
                        }
                        gs.trainedCombatUnits++;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("onUnitComplete exception");
            e.printStackTrace();
        }

    }

    @Override
    public void onUnitDestroy(Unit arg0) { //TODO Fix NPEs
        try {
            UnitType type = UnitType.Unknown;
            if (arg0 instanceof MineralPatch || arg0 instanceof VespeneGeyser || arg0 instanceof SpecialBuilding || arg0 instanceof Critter) {
                type = arg0.getInitialType();
            } else {
                type = Util.getType((PlayerUnit) arg0);
            }

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
                    for (Unit u : aux) {
                        gs.workerMining.remove(u);
                    }
                    gs.mineralsAssigned.remove(arg0);
                }
            }
            if (!type.isBuilding() && !type.isRefinery() && type != UnitType.Resource_Vespene_Geyser && type != UnitType.Spell_Scanner_Sweep) {
                if (!first) {
                    gs.playSound("first.mp3");
                    first = true;
                }
            }
            if (!type.isNeutral() && (!type.isSpecialBuilding() || type.isRefinery())) {
                if (Util.isEnemy(((PlayerUnit) arg0).getPlayer())) {
                    IntelligenceAgency.onDestroy(arg0, type);
                    if (arg0.equals(gs.chosenUnitToHarass)) {
                        gs.chosenUnitToHarass = null;
                    }
                    if (type.isBuilding()) {
                        gs.inMap.updateMap(arg0, true);
                        gs.enemyBuildingMemory.remove(arg0);
                        gs.initAttackPosition = arg0.getTilePosition();
                        gs.map.updateMap(arg0.getTilePosition(), type, true);
                    } else {
                        gs.initDefensePosition = arg0.getTilePosition();
                    }
                } else if (((PlayerUnit) arg0).getPlayer().getId() == self.getId()) {
                    if (type.isWorker()) {
                        if (gs.strat.name == "ProxyBBS") {
                            gs.removeFromSquad(arg0);
                        }
                        for (SCV r : gs.repairerTask.keySet()) {
                            if (r.equals(arg0)) {
                                gs.repairerTask.remove(r);
                                break;
                            }
                        }
                        if (gs.workerIdle.contains(arg0)) {
                            gs.workerIdle.remove(arg0);
                        }
                        if (gs.chosenScout != null && arg0.equals(gs.chosenScout)) {
                            gs.chosenScout = null;
                        }
                        if (gs.chosenHarasser != null && arg0.equals(gs.chosenHarasser)) {
                            gs.chosenHarasser = null;
                            gs.chosenUnitToHarass = null;
                        }
                        if (gs.chosenWorker != null && arg0.equals(gs.chosenWorker)) {
                            gs.chosenWorker = null;
                        }
                        if (gs.chosenRepairer != null && arg0.equals(gs.chosenRepairer)) {
                            gs.chosenRepairer = null;
                        }
                        if (gs.chosenBuilderBL != null && arg0.equals(gs.chosenBuilderBL)) {
                            gs.chosenBuilderBL = null;
                            gs.expanding = false;
                            gs.chosenBaseLocation = null;
                            gs.movingToExpand = false;
                            gs.deltaCash.first -= UnitType.Terran_Command_Center.mineralPrice();
                            gs.deltaCash.second -= UnitType.Terran_Command_Center.gasPrice();
                        }
                        for (Worker u : gs.workerDefenders.keySet()) {
                            if (arg0.equals(u)) {
                                gs.workerDefenders.remove(u);
                                break;
                            }
                        }

                        if (gs.workerMining.containsKey(arg0)) {
                            Unit mineral = gs.workerMining.get(arg0);
                            gs.workerMining.remove(arg0);
                            if (gs.mineralsAssigned.containsKey(mineral)) {
                                gs.mining--;
                                gs.mineralsAssigned.put((MineralPatch) mineral, gs.mineralsAssigned.get(mineral) - 1);
                            }
                        }
                        if (gs.workerGas.containsKey(arg0)) { // TODO fix when destroyed
                            GasMiningFacility aux = gs.workerGas.get(arg0);
                            Integer auxInt = gs.refineriesAssigned.get(arg0);
                            gs.refineriesAssigned.put(aux, auxInt--);
                            gs.workerGas.remove(arg0);
                        }

                        if (gs.workerTask.containsKey(arg0)) {
                            gs.buildingLot.add(gs.workerTask.get(arg0));
                            gs.workerTask.remove(arg0);
                        }

                        if (gs.workerBuild.containsKey(arg0)) {
                            gs.deltaCash.first -= gs.workerBuild.get(arg0).first.mineralPrice();
                            gs.deltaCash.second -= gs.workerBuild.get(arg0).first.gasPrice();
                            gs.workerBuild.remove(arg0);
                        }
                    } else if (type.isBuilding()) {

                        gs.inMap.updateMap(arg0, true);
                        if (type != UnitType.Terran_Command_Center) {
                            gs.map.updateMap(arg0.getTilePosition(), type, true);
                        }
                        for (Entry<SCV, Building> r : gs.repairerTask.entrySet()) {
                            if (r.getValue().equals(arg0)) {
                                gs.workerIdle.add(r.getKey());
                                gs.repairerTask.remove(r.getKey());
                                break;
                            }
                        }
                        for (Entry<SCV, Building> w : gs.workerTask.entrySet()) {
                            if (w.getValue().equals(arg0)) {
                                gs.workerTask.remove(w.getKey());
                                gs.workerIdle.add(w.getKey());
                                break;
                            }
                        }
                        for (Unit w : gs.buildingLot) {
                            if (w.equals(arg0)) {
                                gs.buildingLot.remove(w);
                                break;
                            }
                        }
                        for (CommandCenter u : gs.CCs.values()) {
                            if (u.equals(arg0)) {
                                gs.removeResources(arg0);
                                if (u.getAddon() != null && gs.CSs.contains(u.getAddon())) {
                                    gs.CSs.remove(u.getAddon());
                                }
                                gs.CCs.remove(bwem.getMap().getArea(arg0.getTilePosition()).getTopLeft().toPosition());
                                if (arg0.equals(gs.MainCC)) {
                                    if (gs.CCs.size() > 0) {
                                        for (Unit c : gs.CCs.values()) {
                                            if (!c.equals(arg0)) {
                                                gs.MainCC = u;
                                                break;
                                            }
                                        }
                                    } else {
                                        gs.MainCC = null;
                                        break;
                                    }
                                }
                            }
                        }

                        if (gs.CSs.contains(arg0)) {
                            gs.CSs.remove(arg0);
                        }
                        if (gs.Fs.contains(arg0)) {
                            gs.Fs.remove(arg0);
                        }
                        if (gs.MBs.contains(arg0)) {
                            gs.MBs.remove(arg0);
                        }
                        if (arg0 instanceof ResearchingFacility) {
                            if (gs.UBs.contains(((ResearchingFacility) arg0))) {
                                gs.UBs.remove(((ResearchingFacility) arg0));
                            }
                        }
                        if (gs.SBs.contains(arg0)) {
                            gs.SBs.remove(arg0);
                        }
                        if (gs.Ts.contains(arg0)) {
                            gs.Ts.remove(arg0);
                        }
                        if (gs.Ps.contains(arg0)) {
                            gs.Ps.remove(arg0);
                        }
                        if (type == UnitType.Terran_Bunker) {
                            if (gs.DBs.containsKey(arg0)) {
                                for (Unit u : gs.DBs.get(arg0)) {
                                    gs.addToSquad(u);
                                }
                                gs.DBs.remove(arg0);
                            }
                        }
                        if (type.isRefinery()) { // TODO test
                            if (gs.refineriesAssigned.containsKey(arg0)) {
                                List<Unit> aux = new ArrayList<>();
                                for (Entry<Worker, GasMiningFacility> w : gs.workerGas.entrySet()) {
                                    if (arg0.equals(w.getValue())) {
                                        gs.workerIdle.add((Worker) w.getKey());
                                        aux.add(w.getKey());
                                    }
                                }
                                for (Unit u : aux) gs.workerGas.remove(u);
                                gs.refineriesAssigned.remove(arg0);
                                for (VespeneGeyser g : gs.vespeneGeysers.keySet()) {
                                    if (g.getTilePosition().equals(arg0.getTilePosition())) {
                                        gs.vespeneGeysers.put(g, false);
                                    }
                                }
                            }
                        }
                        gs.testMap = gs.map.clone();
                    } else {
                        if (type == UnitType.Terran_Siege_Tank_Siege_Mode || type == UnitType.Terran_Siege_Tank_Tank_Mode) {
                            if (gs.TTMs.containsKey(arg0)) {
                                gs.TTMs.remove(arg0);
                                gs.removeFromSquad(arg0);
                            }
                        } else if (type == UnitType.Terran_Marine || type == UnitType.Terran_Medic) {
                            gs.removeFromSquad(arg0);
                        } else if (type == UnitType.Terran_Vulture) {
                            gs.agents.remove(new VultureAgent(arg0));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("OnUnitDestroy Exception");
            e.printStackTrace();
        }

    }

    @Override
    public void onUnitMorph(Unit arg0) {
        UnitType type = UnitType.Unknown;
        if (arg0 instanceof VespeneGeyser) {
            type = arg0.getInitialType();
        } else {
            type = Util.getType((PlayerUnit) arg0);
        }
        if (Util.isEnemy(((PlayerUnit) arg0).getPlayer())) {
            if (arg0 instanceof Building && !(arg0 instanceof GasMiningFacility)) {
                if (!gs.enemyBuildingMemory.containsKey(arg0)) {
                    gs.inMap.updateMap(arg0, false);
                    gs.enemyBuildingMemory.put(arg0, new EnemyBuilding(arg0));
                }
            }
        }
        if (arg0 instanceof Refinery && ((PlayerUnit) arg0).getPlayer().getId() == self.getId()) {
            for (Entry<GasMiningFacility, Integer> r : gs.refineriesAssigned.entrySet()) {
                if (r.getKey().getTilePosition().equals(arg0.getTilePosition())) {
                    gs.map.updateMap(arg0.getTilePosition(), type, false);
                    gs.testMap = gs.map.clone();
                    break;
                }
            }
            for (Entry<SCV, Pair<UnitType, TilePosition>> u : gs.workerBuild.entrySet()) {
                if (u.getKey().equals(((Building) arg0).getBuildUnit()) && u.getValue().first == type) {
                    gs.workerBuild.remove(u.getKey());
                    gs.workerTask.put(u.getKey(), (Building) arg0);
                    gs.deltaCash.first -= type.mineralPrice();
                    gs.deltaCash.second -= type.gasPrice();
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
        if (gs.enemyCombatUnitMemory.contains(arg0)) {
            gs.enemyCombatUnitMemory.remove(arg0);
        }
    }

    @Override
    public void onUnitRenegade(Unit arg0) {

    }

    @Override
    public void onUnitShow(Unit arg0) {
        try {
            if (arg0 instanceof MineralPatch || arg0 instanceof VespeneGeyser || arg0 instanceof SpecialBuilding || arg0 instanceof Critter)
                return;
            UnitType type = Util.getType((PlayerUnit) arg0);
            if (Util.isEnemy(((PlayerUnit) arg0).getPlayer())) {
                IntelligenceAgency.onShow(arg0, type);
                if (gs.enemyRace == Race.Unknown && getGs().players.size() == 3) { // TODO Check
                    gs.enemyRace = type.getRace();
                }
                if (!type.isBuilding() || type.canAttack() || type.isSpellcaster() || type.spaceProvided() > 0) {
                    gs.enemyCombatUnitMemory.add(arg0);
                }
                if (type.isBuilding()) {

                    if (!gs.enemyBuildingMemory.containsKey(arg0)) {
                        gs.enemyBuildingMemory.put(arg0, new EnemyBuilding(arg0));
                        gs.inMap.updateMap(arg0, false);
                        gs.map.updateMap(arg0.getTilePosition(), type, false);
                    }
                }

            }
        } catch (Exception e) {
            System.err.println("OnUnitShow Exception");
            e.printStackTrace();
        }

    }
}
