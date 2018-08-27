package ecgberht;

import bwem.BWEM;
import bwem.Base;
import bwem.ChokePoint;
import bwem.area.Area;
import bwem.unit.Geyser;
import bwem.unit.Mineral;
import bwem.unit.Neutral;
import bwem.unit.NeutralImpl;
import com.google.gson.Gson;
import ecgberht.Agents.*;
import ecgberht.Simulation.SimManager;
import ecgberht.Strategies.*;
import ecgberht.Util.BaseLocationComparator;
import ecgberht.Util.ColorUtil;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import jfap.JFAP;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.*;
import org.openbw.bwapi4j.type.*;
import org.openbw.bwapi4j.unit.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GameState extends GameHandler {

    public Area enemyMainArea = null;
    public Area enemyNaturalArea = null;
    public Area naturalArea = null;
    public Base chosenIsland = null;
    public Base enemyMainBase = null;
    public Base enemyNaturalBase = null;
    public Base enemyStartBase = null;
    public boolean defense = false;
    public boolean enemyIsRandom = true;
    public boolean expanding = false;
    public boolean firstProxyBBS = false;
    public boolean firstScout = true;
    public boolean iReallyWantToExpand = false;
    public boolean islandExpand;
    public boolean movingToExpand = false;
    public boolean siegeResearched = false;
    public Building chosenBuildingLot = null;
    public Mechanical chosenUnitRepair = null;
    public BuildingMap map;
    public BuildingMap testMap;
    public ChokePoint mainChoke = null;
    public ChokePoint naturalChoke = null;
    public DropShipAgent chosenDropShip;
    public EnemyInfo EI = new EnemyInfo(ih.enemy().getName(), ih.enemy().getRace());
    public EnemyHistory EH = new EnemyHistory();
    public ExtendibleByAddon chosenBuildingAddon = null;
    public Gson enemyInfoJSON = new Gson();
    public Gson enemyHistoryJSON = new Gson();
    public int builtBuildings;
    public int builtRefinery;
    public int directionScoutMain;
    public int frameCount;
    public int mapSize = 2;
    public int maxWraiths = 5;
    public int maxBats = 3;
    public int mining;
    public int startCount;
    public int vulturesTrained = 0;
    public int workerCountToSustain = 0;
    public JFAP simulator;
    public List<Base> blockedBLs = new ArrayList<>();
    public List<Base> BLs = new ArrayList<>();
    public List<Base> EnemyBLs = new ArrayList<>();
    public List<Base> specialBLs = new ArrayList<>();
    public Map<Base, CommandCenter> CCs = new HashMap<>();
    public Map<Base, CommandCenter> islandCCs = new HashMap<>();
    public Map<Base, Neutral> blockedBases = new HashMap<>();
    public Map<Bunker, Set<Unit>> DBs = new TreeMap<>();
    public Map<GasMiningFacility, Integer> refineriesAssigned = new TreeMap<>();
    public Map<MineralPatch, Integer> mineralsAssigned = new TreeMap<>();
    public Map<Player, Integer> players = new HashMap<>();
    public Map<Position, MineralPatch> blockingMinerals = new LinkedHashMap<>();
    public Map<SCV, Mechanical> repairerTask = new TreeMap<>();
    public Map<SCV, Building> workerTask = new TreeMap<>();
    public Map<SCV, MutablePair<UnitType, TilePosition>> workerBuild = new HashMap<>();
    public Map<Unit, Agent> agents = new TreeMap<>();
    public Map<Unit, EnemyBuilding> enemyBuildingMemory = new TreeMap<>();
    public Map<VespeneGeyser, Boolean> vespeneGeysers = new TreeMap<>();
    public Map<Worker, GasMiningFacility> workerGas = new TreeMap<>();
    public Map<Worker, MineralPatch> workerMining = new TreeMap<>();
    public Map<Worker, Position> workerDefenders = new TreeMap<>();
    public MutablePair<Base, Unit> MainCC = null;
    public MutablePair<Integer, Integer> deltaCash = new MutablePair<>(0, 0);
    public MutablePair<Integer, Unit> chosenMarine = null;
    public Player neutral = null;
    public Position attackPosition;
    public Position defendPosition = null;
    public Race enemyRace = Race.Unknown;
    public ResearchingFacility chosenUnitUpgrader = null;
    public SCV chosenRepairer = null;
    public Set<Barracks> MBs = new TreeSet<>();
    public Set<Base> islandBases = new HashSet<>();
    public Set<Base> ScoutSLs = new HashSet<>();
    public Set<Base> SLs = new HashSet<>();
    public Set<Building> buildingLot = new TreeSet<>();
    public Set<ComsatStation> CSs = new TreeSet<>();
    public Set<Factory> Fs = new TreeSet<>();
    public Set<MissileTurret> Ts = new TreeSet<>();
    public Set<ResearchingFacility> UBs = new TreeSet<>();
    public Set<Starport> Ps = new TreeSet<>();
    public Set<Unit> myArmy = new TreeSet<>();
    public Set<SupplyDepot> SBs = new TreeSet<>();
    public Set<Unit> enemyCombatUnitMemory = new TreeSet<>();
    public Set<Unit> enemyInBase = new TreeSet<>();
    public Set<Worker> workerIdle = new TreeSet<>();
    public SimManager sim;
    public SquadManager sqManager = new SquadManager();
    public Strategy strat = null;
    public SupplyMan supplyMan;
    public TechType chosenResearch = null;
    public TilePosition checkScan = null;
    public TilePosition chosenBaseLocation = null;
    public TilePosition chosenPosition = null;
    public TilePosition initAttackPosition = null;
    public TilePosition initDefensePosition = null;
    public TrainingFacility chosenBuilding = null;
    public Unit chosenBunker = null;
    public Unit chosenScout = null;
    public Unit chosenUnitToHarass = null;
    public UnitType chosenAddon = null;
    public UnitType chosenToBuild = null;
    public UnitType chosenUnit = null;
    public UpgradeType chosenUpgrade = null;
    public Worker chosenBuilderBL = null;
    public Worker chosenHarasser = null;
    public Worker chosenWorker = null;
    public Worker chosenWorkerDrop = null;
    public boolean firstExpand = true;
    Set<String> shipNames = new TreeSet<>(Arrays.asList("Adriatic", "Aegis Fate", "Agincourt", "Allegiance",
            "Apocalypso", "Athens", "Beatrice", "Bloodied Spirit", "Callisto", "Clarity of Faith", "Dawn Under Heaven",
            "Forward Unto Dawn", "Gettysburg", "Grafton", "Halcyon", "Hannibal", "Harbinger of Piety", "High Charity",
            "In Amber Clad", "Infinity", "Jericho", "Las Vegas", "Lawgiver", "Leviathan", "Long Night of Solace",
            "Matador", "Penance", "Persephone", "Pillar of Autumn", "Pitiless", "Pompadour", "Providence", "Revenant",
            "Savannah", "Shadow of Intent", "Spirit of Fire", "Tharsis", "Thermopylae"));

    public GameState(BW bw, BWEM bwem) {
        super(bw, bwem);
        initPlayers();
        map = new BuildingMap(bw, ih.self(), bwem);
        map.initMap();
        testMap = map.clone();
        mapSize = bw.getBWMap().getStartPositions().size();
        simulator = new JFAP(bw);
        supplyMan = new SupplyMan(self.getRace());
        sim = new SimManager(bw);
    }

    private void initPlayers() {
        for (Player p : bw.getAllPlayers()) {
            //if(p.isObserver()) continue; // TODO uncomment when bwapi client bug is fixed
            if (p.isNeutral()) {
                players.put(p, 0);
                neutral = p;
            } else if (ih.allies().contains(p) || p.equals(self)) players.put(p, 1);
            else if (ih.enemies().contains(p)) {
                players.put(p, -1);
                IntelligenceAgency.enemyBases.put(p.getName(), new TreeSet<>());
                IntelligenceAgency.enemyTypes.put(p.getName(), new HashSet<>());
            }
        }
    }

    Strategy initStrat() {
        try {
            FullBio b = new FullBio();
            ProxyBBS bbs = new ProxyBBS();
            BioMech bM = new BioMech();
            FullBioFE bFE = new FullBioFE();
            BioMechFE bMFE = new BioMechFE();
            FullMech FM = new FullMech();
            BioGreedyFE bGFE = new BioGreedyFE();
            MechGreedyFE mGFE = new MechGreedyFE();
            BioMechGreedyFE bMGFE = new BioMechGreedyFE();
            TwoPortWraith tPW = new TwoPortWraith();
            String map = bw.getBWMap().mapFileName();
            String forcedStrat = ConfigManager.getConfig().ecgConfig.forceStrat;
            if (enemyRace == Race.Zerg && EI.naughty) return b;
            if (bw.getBWMap().mapHash().equals("6f5295624a7e3887470f3f2e14727b1411321a67")) { // Plasma!!!
                maxWraiths = 200; // HELL
                return new PlasmaWraithHell();
            }
            String enemyName = EI.opponent.toLowerCase().replace(" ", "");
            if (enemyName.equals("arrakhammer") || enemyName.equals("pineapplecactus")) {
                return tPW; // TODO properly ponderate strats vs race/opponent
            }
            Map<String, MutablePair<Integer, Integer>> strategies = new LinkedHashMap<>();
            Map<String, Strategy> nameStrat = new LinkedHashMap<>();

            strategies.put(b.name, new MutablePair<>(0, 0));
            nameStrat.put(b.name, b);

            strategies.put(bM.name, new MutablePair<>(0, 0));
            nameStrat.put(bM.name, bM);

            strategies.put(bGFE.name, new MutablePair<>(0, 0));
            nameStrat.put(bGFE.name, bGFE);

            if (enemyRace == Race.Zerg) {
                strategies.put(tPW.name, new MutablePair<>(0, 0));
                nameStrat.put(tPW.name, tPW);
            }

            strategies.put(bMGFE.name, new MutablePair<>(0, 0));
            nameStrat.put(bMGFE.name, bGFE);

            strategies.put(FM.name, new MutablePair<>(0, 0));
            nameStrat.put(FM.name, FM);

            strategies.put(bbs.name, new MutablePair<>(0, 0));
            nameStrat.put(bbs.name, bbs);

            strategies.put(mGFE.name, new MutablePair<>(0, 0));
            nameStrat.put(mGFE.name, bGFE);

            strategies.put(bMFE.name, new MutablePair<>(0, 0));
            nameStrat.put(bMFE.name, bMFE);

            strategies.put(bFE.name, new MutablePair<>(0, 0));
            nameStrat.put(bFE.name, bFE);

            if (!forcedStrat.equals("") && nameStrat.containsKey(forcedStrat)) {
                ih.sendText("Picked forced strategy " + forcedStrat);
                return nameStrat.get(forcedStrat);
            }

            int totalGamesPlayed = EI.wins + EI.losses;
            if (totalGamesPlayed < 1) {
                ih.sendText("I dont know you that well yet, lets pick the standard strategy");
                return b;
            }
            for (StrategyOpponentHistory r : EI.history) {
                if (strategies.containsKey(r.strategyName)) {
                    strategies.get(r.strategyName).first += r.wins;
                    strategies.get(r.strategyName).second += r.losses;
                }
            }
            double maxWinRate = 0.0;
            String bestStrat = null;
            for (Entry<String, MutablePair<Integer, Integer>> s : strategies.entrySet()) {
                double winRate = (s.getValue().first + s.getValue().second) > 0 ? (double) s.getValue().first / (s.getValue().first + s.getValue().second) : 0;
                if (winRate >= 0.75 && winRate > maxWinRate) {
                    maxWinRate = winRate;
                    bestStrat = s.getKey();
                }
            }
            if (maxWinRate != 0.0 && bestStrat != null) {
                ih.sendText("Using best Strategy: " + bestStrat + " with winrate " + maxWinRate * 100 + "%");
                return nameStrat.get(bestStrat);
            }
            double C = 0.5;
            String bestUCBStrategy = null;
            double bestUCBStrategyVal = Double.MIN_VALUE;
            for (String strat : strategies.keySet()) {
                if (map.contains("HeartbreakRidge") &&
                        nameStrat.get(strat).trainUnits.contains(UnitType.Terran_Siege_Tank_Tank_Mode)) {
                    continue;
                }
                int sGamesPlayed = strategies.get(strat).first + strategies.get(strat).second;
                double sWinRate = sGamesPlayed > 0 ? (strategies.get(strat).first / (double) (sGamesPlayed)) : 0;
                double ucbVal = sGamesPlayed == 0 ? 0.5 : C * Math.sqrt(Math.log((double) (totalGamesPlayed / sGamesPlayed)));
                double val = sWinRate + ucbVal;
                if (val > bestUCBStrategyVal) {
                    bestUCBStrategy = strat;
                    bestUCBStrategyVal = val;
                }
            }
            ih.sendText("Chose: " + bestUCBStrategy + " with UCB: " + bestUCBStrategyVal);
            return nameStrat.get(bestUCBStrategy);
        } catch (Exception e) {
            System.err.println("Error initStrat, using default strategy");
            e.printStackTrace();
            return new FullBio();
        }

    }

    void initEnemyRace() {
        if (ih.enemy().getRace() != Race.Unknown) {
            enemyRace = ih.enemy().getRace();
            enemyIsRandom = false;
        }
    }

    public void initBlockingMinerals() {
        int amount = 0;
        if (bw.getBWMap().mapHash().equals("cd5d907c30d58333ce47c88719b6ddb2cba6612f")) amount = 16; // Valkyries
        for (MineralPatch u : bw.getMineralPatches()) {
            if (u.getResources() <= amount) blockingMinerals.put(u.getPosition(), u);
        }
        for (Base b : BLs) {
            if (b.isStartingLocation()) continue;
            if (skipWeirdBlocking(b)) continue;
            if (weirdBlocking(b)) blockedBLs.add(b);
            else {
                for (ChokePoint p : b.getArea().getChokePoints()) {
                    Neutral n = p.getBlockingNeutral();
                    if (n != null && n.getBlockedAreas().contains(b.getArea())) {
                        blockedBases.put(b, n);
                        blockedBLs.add(b);
                    }
                }
            }
        }
    }

    private boolean skipWeirdBlocking(Base b) {
        if (bw.getBWMap().mapHash().equals("cd5d907c30d58333ce47c88719b6ddb2cba6612f")) { // Valkyries
            return b.getLocation().equals(new TilePosition(25, 67)) || b.getLocation().equals(new TilePosition(99, 67));
        }
        return false;
    }

    private boolean weirdBlocking(Base b) {
        if (bw.getBWMap().mapHash().equals("4e24f217d2fe4dbfa6799bc57f74d8dc939d425b")) { // CIG destination / SSCAIT destination
            return b.getLocation().equals(new TilePosition(6, 119));
        }
        return false;
    }

    void checkBasesWithBLockingMinerals() {
        if (blockingMinerals.isEmpty()) return;
        for (bwem.Base b : BLs) {
            if (b.isStartingLocation() || skipWeirdBlocking(b)) continue;
            for (ChokePoint c : b.getArea().getChokePoints()) {
                for (Position m : blockingMinerals.keySet()) {
                    if (Util.broodWarDistance(m, c.getCenter().toPosition()) < 40) {
                        blockedBLs.add(b);
                        break;
                    }
                }
            }
        }
    }

    public void playSound(String soundFile) {
        try {
            if (!ConfigManager.getConfig().ecgConfig.sounds) return;
            String run = getClass().getResource("GameState.class").toString();
            if (run.startsWith("jar:") || run.startsWith("rsrc:")) {
                InputStream fis = getClass().getClassLoader().getResourceAsStream(soundFile);
                javazoom.jl.player.Player playMP3 = new javazoom.jl.player.Player(fis);
                new Thread(() -> {
                    try {
                        playMP3.play();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                soundFile = "src\\" + soundFile;
                FileInputStream fis = new FileInputStream(soundFile);
                javazoom.jl.player.Player playMP3 = new javazoom.jl.player.Player(fis);
                new Thread(() -> {
                    try {
                        playMP3.play();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }).start();
            }

        } catch (Exception e) {
            System.err.println("playSound");
            System.err.println(e);
        }
    }

    public BW getGame() {
        return bw;
    }

    public InteractionHandler getIH() {
        return ih;
    }

    public Player getPlayer() {
        return self;
    }

    void addNewResources(Unit unit) {
        List<Mineral> minerals = Util.getClosestBaseLocation(unit.getPosition()).getMinerals();
        List<Geyser> gas = Util.getClosestBaseLocation(unit.getPosition()).getGeysers();
        for (Mineral m : minerals) mineralsAssigned.put((MineralPatch) m.getUnit(), 0);
        for (Geyser g : gas) vespeneGeysers.put((VespeneGeyser) g.getUnit(), false);
        if (strat.name.equals("ProxyBBS")) {
            workerCountToSustain = (int) mineralGatherRateNeeded(Arrays.asList(UnitType.Terran_Marine, UnitType.Terran_Marine));
        }
    }

    void removeResources(Unit unit) {
        List<Mineral> minerals = Util.getClosestBaseLocation(unit.getPosition()).getMinerals();
        List<Geyser> gas = Util.getClosestBaseLocation(unit.getPosition()).getGeysers();
        for (Mineral m : minerals) {
            if (mineralsAssigned.containsKey(m.getUnit())) {
                List<Unit> aux = new ArrayList<>();
                for (Entry<Worker, MineralPatch> w : workerMining.entrySet()) {
                    if (m.getUnit().equals(w.getValue())) {
                        aux.add(w.getKey());
                        workerIdle.add(w.getKey());
                    }
                }
                for (Unit u : aux) workerMining.remove(u);
                mineralsAssigned.remove(m.getUnit());
            }

        }
        for (Geyser g : gas) {
            VespeneGeyser geyser = (VespeneGeyser) g.getUnit(); // TODO improve
            if (vespeneGeysers.containsKey(geyser)) vespeneGeysers.remove(geyser);
        }
        List<Unit> auxGas = new ArrayList<>();
        for (Entry<GasMiningFacility, Integer> pm : refineriesAssigned.entrySet()) { // TODO test
            for (Geyser g : gas) {
                if (pm.getKey().equals(g.getUnit())) {
                    List<Worker> aux = new ArrayList<>();
                    for (Entry<Worker, GasMiningFacility> w : workerGas.entrySet()) {
                        if (pm.getKey().equals(w.getValue())) {
                            aux.add(w.getKey());
                            workerIdle.add(w.getKey());
                        }
                    }
                    for (Worker u : aux) workerGas.remove(u);
                    auxGas.add(pm.getKey());
                }
            }
        }
        for (Unit u : auxGas) refineriesAssigned.remove(u);
        if (strat.name.equals("ProxyBBS")) {
            workerCountToSustain = (int) mineralGatherRateNeeded(Arrays.asList(UnitType.Terran_Marine, UnitType.Terran_Marine));
        }
    }

    public MutablePair<Integer, Integer> getCash() {
        return new MutablePair<>(self.minerals(), self.gas());
    }

    public int getSupply() {
        return (self.supplyTotal() - self.supplyUsed());
    }

    void debugText() {
        try {
            if (!ConfigManager.getConfig().ecgConfig.debugText) return;
            bw.getMapDrawer().drawTextScreen(320, 5, ColorUtil.formatText(supplyMan.getSupplyUsed() + "/" + supplyMan.getSupplyTotal(), ColorUtil.White));
            bw.getMapDrawer().drawTextScreen(320, 20, ColorUtil.formatText(getArmySize() + "/" + strat.armyForAttack, ColorUtil.White));
            String defending = defense ? ColorUtil.formatText("Defense", ColorUtil.Green) : ColorUtil.formatText("Defense", ColorUtil.Red);
            bw.getMapDrawer().drawTextScreen(320, 35, defending);
            bw.getMapDrawer().drawTextScreen(320, 50, ColorUtil.formatText(chosenUnit.toString(), ColorUtil.White));
            if (ih.allies().size() + ih.enemies().size() == 1) {
                bw.getMapDrawer().drawTextScreen(10, 5,
                        ColorUtil.formatText(ih.self().getName(), ColorUtil.getColor(ih.self().getColor())) +
                                ColorUtil.formatText(" vs ", ColorUtil.White) +
                                ColorUtil.formatText(ih.enemy().getName(), ColorUtil.getColor(ih.enemy().getColor())));
            }
            if (chosenScout != null) {
                bw.getMapDrawer().drawTextScreen(10, 20, ColorUtil.formatText("Scouting: ", ColorUtil.White) + ColorUtil.formatText("Yes", ColorUtil.Green));
            } else {
                bw.getMapDrawer().drawTextScreen(10, 20, ColorUtil.formatText("Scouting: ", ColorUtil.White) + ColorUtil.formatText("No", ColorUtil.Red));
            }
            if (enemyMainBase != null) {
                bw.getMapDrawer().drawTextScreen(10, 35, ColorUtil.formatText("Enemy Base Found: ", ColorUtil.White) + ColorUtil.formatText("Yes", ColorUtil.Green));
            } else {
                bw.getMapDrawer().drawTextScreen(10, 35, ColorUtil.formatText("Enemy Base Found: ", ColorUtil.White) + ColorUtil.formatText("No", ColorUtil.Red));
            }
            bw.getMapDrawer().drawTextScreen(10, 50, ColorUtil.formatText("FPS: ", ColorUtil.White) + ColorUtil.formatText(Integer.toString(ih.getFPS()), ColorUtil.Yellow));
            bw.getMapDrawer().drawTextScreen(65, 50, ColorUtil.formatText("APM: ", ColorUtil.White) + ColorUtil.formatText(Integer.toString(ih.getAPM()), ColorUtil.Yellow));
            bw.getMapDrawer().drawTextScreen(10, 65, ColorUtil.formatText("Strategy: ", ColorUtil.White) + ColorUtil.formatText(strat.name, ColorUtil.Yellow));
            bw.getMapDrawer().drawTextScreen(10, 80, ColorUtil.formatText("EnemyStrategy: ", ColorUtil.White) + ColorUtil.formatText(IntelligenceAgency.getEnemyStrat().toString(), ColorUtil.Yellow));
            bw.getMapDrawer().drawTextScreen(10, 95, ColorUtil.formatText("SimTime(ms): ", ColorUtil.White) + ColorUtil.formatText(String.valueOf(sim.time), ColorUtil.Teal));
            if (enemyRace == Race.Zerg && EI.naughty) {
                bw.getMapDrawer().drawTextScreen(10, 110, ColorUtil.formatText("Naughty Zerg: ", ColorUtil.White) + ColorUtil.formatText("yes", ColorUtil.Green));
            }
        } catch (Exception e) {
            System.err.println("debugText Exception");
            e.printStackTrace();
        }
    }

    void debugScreen() {
        if (!ConfigManager.getConfig().ecgConfig.debugScreen) return;
        if (naturalArea != null) {
            print(naturalArea.getTop().toTilePosition(), Color.RED);
            for (ChokePoint c : naturalArea.getChokePoints()) {
                if (c.getGeometry().size() > 2)
                    bw.getMapDrawer().drawLineMap(c.getGeometry().get(0).toPosition(), c.getGeometry().get(c.getGeometry().size() - 1).toPosition(), Color.GREEN);
            }
        }
       /* for(ChokePoint c : bwem.getMap().getChokePoints()){
            if(c.getGeometry().size() > 2) bw.getMapDrawer().drawLineMap(c.getGeometry().get(0).toPosition(), c.getGeometry().get(c.getGeometry().size()-1).toPosition(), Color.GREEN);
        }*/
        for (MineralPatch d : blockingMinerals.values()) print(d, Color.RED);
        int counter = 0;
        for (Base b : BLs) {
            bw.getMapDrawer().drawTextMap(getCenterFromBuilding(b.getLocation().toPosition(), UnitType.Terran_Command_Center), ColorUtil.formatText(Integer.toString(counter), ColorUtil.White));
            counter++;
        }
        for (Base b : islandBases)
            bw.getMapDrawer().drawTextMap(b.getLocation().toPosition(), ColorUtil.formatText("Island", ColorUtil.White));
        for (Agent ag : agents.values()) {
            if (ag instanceof VultureAgent) {
                VultureAgent vulture = (VultureAgent) ag;
                bw.getMapDrawer().drawTextMap(vulture.unit.getPosition(), ColorUtil.formatText(ag.statusToString(), ColorUtil.White));
            } else if (ag instanceof VesselAgent) {
                VesselAgent vessel = (VesselAgent) ag;
                bw.getMapDrawer().drawTextMap(vessel.unit.getPosition(), ColorUtil.formatText(ag.statusToString(), ColorUtil.White));
                if (vessel.follow != null)
                    bw.getMapDrawer().drawLineMap(vessel.unit.getPosition(), vessel.follow.getSquadCenter(), Color.YELLOW);
            } else if (ag instanceof WraithAgent) {
                WraithAgent wraith = (WraithAgent) ag;
                bw.getMapDrawer().drawTextMap(wraith.unit.getPosition(), ColorUtil.formatText(ag.statusToString(), ColorUtil.White));
                bw.getMapDrawer().drawTextMap(wraith.unit.getPosition().add(new Position(0,
                        UnitType.Terran_Wraith.dimensionUp())), ColorUtil.formatText(wraith.name, ColorUtil.White));
            } else if (ag instanceof DropShipAgent) {
                DropShipAgent dropShip = (DropShipAgent) ag;
                bw.getMapDrawer().drawTextMap(dropShip.unit.getPosition(), ColorUtil.formatText(ag.statusToString(), ColorUtil.White));
            }
        }
        if (enemyStartBase != null)
            bw.getMapDrawer().drawTextMap(enemyStartBase.getLocation().toPosition(), ColorUtil.formatText("EnemyStartBase", ColorUtil.White));
        if (enemyNaturalBase != null)
            bw.getMapDrawer().drawTextMap(enemyNaturalBase.getLocation().toPosition(), ColorUtil.formatText("EnemyNaturalBase", ColorUtil.White));
        if (mainChoke != null)
            bw.getMapDrawer().drawTextMap(mainChoke.getCenter().toPosition(), ColorUtil.formatText("MainChoke", ColorUtil.White));
        if (naturalChoke != null)
            bw.getMapDrawer().drawTextMap(naturalChoke.getCenter().toPosition(), ColorUtil.formatText("NatChoke", ColorUtil.White));
        if (chosenBuilderBL != null) {
            bw.getMapDrawer().drawTextMap(chosenBuilderBL.getPosition(), ColorUtil.formatText("BuilderBL", ColorUtil.White));
            print(chosenBuilderBL, Color.BLUE);
        }
        if (chosenHarasser != null) {
            bw.getMapDrawer().drawTextMap(chosenHarasser.getPosition(), ColorUtil.formatText("Harasser", ColorUtil.White));
            print(chosenHarasser, Color.BLUE);
        }
        if (chosenBaseLocation != null) {
            print(chosenBaseLocation, UnitType.Terran_Command_Center, Color.CYAN);
        }
        for (Entry<SCV, MutablePair<UnitType, TilePosition>> u : workerBuild.entrySet()) {
            print(u.getKey(), Color.TEAL);
            bw.getMapDrawer().drawTextMap(u.getKey().getPosition(), ColorUtil.formatText("Building " + u.getValue().first.toString(), ColorUtil.White));
            print(u.getValue().second, u.getValue().first, Color.TEAL);
            bw.getMapDrawer().drawLineMap(u.getKey().getPosition(), getCenterFromBuilding(u.getValue().second.toPosition(), u.getValue().first), Color.RED);
        }
        if (chosenUnitToHarass != null) {
            print(chosenUnitToHarass, Color.RED);
            bw.getMapDrawer().drawTextMap(chosenUnitToHarass.getPosition(), ColorUtil.formatText("UnitToHarass", ColorUtil.White));
        }
        for (SCV r : repairerTask.keySet()) {
            print(r, Color.YELLOW);
            bw.getMapDrawer().drawTextMap(r.getPosition(), ColorUtil.formatText("Repairer", ColorUtil.White));
        }
        for (EnemyBuilding b : enemyBuildingMemory.values()) print(b.pos, b.type, Color.RED);
        if (chosenScout != null) {
            bw.getMapDrawer().drawTextMap(chosenScout.getPosition(), ColorUtil.formatText("Scouter", ColorUtil.White));
            print(chosenScout, Color.PURPLE);
        }
        if (chosenRepairer != null)
            bw.getMapDrawer().drawTextMap(chosenRepairer.getPosition(), ColorUtil.formatText("ChosenRepairer", ColorUtil.White));
        for (ChokePoint c : bwem.getMap().getChokePoints()) {
            List<WalkPosition> sides = c.getGeometry();
            if (sides.size() == 3) {
                bw.getMapDrawer().drawLineMap(sides.get(1).toPosition(), sides.get(2).toPosition(), Color.GREEN);
            }
        }
        for (Unit u : CCs.values()) {
            print(u, Color.YELLOW);
            bw.getMapDrawer().drawCircleMap(u.getPosition(), 500, Color.ORANGE);
        }
        for (Unit u : DBs.keySet()) {
            bw.getMapDrawer().drawCircleMap(u.getPosition(), 300, Color.ORANGE);
        }
        for (Unit u : workerIdle) print(u, Color.ORANGE);
        for (Entry<SCV, Building> u : workerTask.entrySet()) {
            print(u.getKey(), Color.TEAL);
            bw.getMapDrawer().drawTextMap(u.getKey().getPosition(), ColorUtil.formatText("Tasked: " + u.getValue().getType().toString(), ColorUtil.White));
            print(u.getValue(), Color.TEAL);
            bw.getMapDrawer().drawLineMap(u.getKey().getPosition(), u.getValue().getPosition(), Color.RED);
        }
        for (Worker u : workerDefenders.keySet()) {
            print(u, Color.PURPLE);
            bw.getMapDrawer().drawTextMap(u.getPosition(), ColorUtil.formatText("SpartanSCV", ColorUtil.White));
        }
        for (Entry<Worker, MineralPatch> u : workerMining.entrySet()) {
            print(u.getKey(), Color.CYAN);
            bw.getMapDrawer().drawLineMap(u.getKey().getPosition(), u.getValue().getPosition(), Color.CYAN);
        }
        for (Entry<Worker, GasMiningFacility> u : workerGas.entrySet()) {
            if (u.getKey().getOrder() == Order.HarvestGas) continue;
            print(u.getKey(), Color.GREEN);
            bw.getMapDrawer().drawLineMap(u.getKey().getPosition(), u.getValue().getPosition(), Color.GREEN);
        }
        for (Entry<VespeneGeyser, Boolean> u : vespeneGeysers.entrySet()) {
            print(u.getKey(), Color.GREEN);
            if (refineriesAssigned.containsKey(u.getKey())) {
                int gas = refineriesAssigned.get(u.getKey());
                bw.getMapDrawer().drawTextMap(u.getKey().getPosition(), ColorUtil.formatText(Integer.toString(gas), ColorUtil.White));
            }
        }
        sim.drawClusters();
        for (Squad s : sqManager.squads.values()) {
            if (s.status == Squad.Status.ATTACK && s.attack != null)
                bw.getMapDrawer().drawLineMap(s.getSquadCenter(), s.attack, Color.ORANGE);
        }
        /*for (Squad s : sqManager.squads.values()) {
            if (s.members.isEmpty()) continue;
            Position center = s.getSquadCenter();
            bw.getMapDrawer().drawCircleMap(center, 90, Color.GREEN);
            bw.getMapDrawer().drawTextMap(center.add(new Position(0, UnitType.Terran_Marine.dimensionUp())), ColorUtil.formatText(s.status.toString(), ColorUtil.White));
        }*/
        for (Entry<MineralPatch, Integer> m : mineralsAssigned.entrySet()) {
            print(m.getKey(), Color.CYAN);
            if (m.getValue() == 0) continue;
            bw.getMapDrawer().drawTextMap(m.getKey().getPosition(), ColorUtil.formatText(m.getValue().toString(), ColorUtil.White));
        }
    }

    private void print(Unit u, Color color) {
        bw.getMapDrawer().drawBoxMap(u.getLeft(), u.getTop(), u.getRight(), u.getBottom(), color);
    }

    private void print(TilePosition u, UnitType type, Color color) {
        Position leftTop = new Position(u.getX() * TilePosition.SIZE_IN_PIXELS, u.getY() * TilePosition.SIZE_IN_PIXELS);
        Position rightBottom = new Position(leftTop.getX() + type.tileWidth() * TilePosition.SIZE_IN_PIXELS, leftTop.getY() + type.tileHeight() * TilePosition.SIZE_IN_PIXELS);
        bw.getMapDrawer().drawBoxMap(leftTop, rightBottom, color);
    }

    private void print(TilePosition u, Color color) {
        Position leftTop = new Position(u.getX() * TilePosition.SIZE_IN_PIXELS, u.getY() * TilePosition.SIZE_IN_PIXELS);
        Position rightBottom = new Position(leftTop.getX() + TilePosition.SIZE_IN_PIXELS, leftTop.getY() + TilePosition.SIZE_IN_PIXELS);
        bw.getMapDrawer().drawBoxMap(leftTop, rightBottom, color);
    }

    void initStartLocations() {
        Base startBot = Util.getClosestBaseLocation(self.getStartLocation().toPosition());
        for (bwem.Base b : bwem.getMap().getBases()) {
            if (b.isStartingLocation() && !b.getLocation().equals(startBot.getLocation())) {
                SLs.add(b);
                ScoutSLs.add(b);
            }
        }
    }

    void initBaseLocations() {
        BLs.sort(new BaseLocationComparator(Util.getClosestBaseLocation(self.getStartLocation().toPosition())));
        if (strat.name.equals("PlasmaWraithHell")) { // Special logic for Plasma
            specialBLs.add(BLs.get(0));
            if (BLs.get(0).getLocation().equals(new TilePosition(77, 63))) { // Start 1
                for (Base b : BLs) {
                    TilePosition pos = b.getLocation();
                    if (pos.equals(new TilePosition(85, 42)) || pos.equals(new TilePosition(85, 83))) {
                        specialBLs.add(b);
                    }
                }
                return;
            }
            if (BLs.get(0).getLocation().equals(new TilePosition(14, 110))) { // Start 2
                for (Base b : BLs) {
                    TilePosition pos = b.getLocation();
                    if (pos.equals(new TilePosition(39, 118)) || pos.equals(new TilePosition(7, 90))) {
                        specialBLs.add(b);
                    }
                }
                return;
            }
            if (BLs.get(0).getLocation().equals(new TilePosition(14, 14))) { // Start 3
                for (Base b : BLs) {
                    TilePosition pos = b.getLocation();
                    if (pos.equals(new TilePosition(36, 6)) || pos.equals(new TilePosition(7, 37))) {
                        specialBLs.add(b);
                    }
                }
            }
        }
    }

    void fix() {
        if (defense && enemyInBase.isEmpty()) defense = false;
        /*List<Bunker> bunkers = new ArrayList<>();
        for (Entry<Bunker, Set<Unit>> u : DBs.entrySet()) {
            if (u.getKey().exists()) continue;
            for (Unit m : u.getValue()) {
                if (m.exists()) addToSquad(m);
            }
            bunkers.add(u.getKey());
        }
        for (Bunker c : bunkers) DBs.remove(c);*/

        List<Worker> removeGas = new ArrayList<>();
        for (Entry<Worker, GasMiningFacility> w : workerGas.entrySet()) {
            if (!w.getKey().isGatheringGas()) {
                removeGas.add(w.getKey());
                refineriesAssigned.put(w.getValue(), refineriesAssigned.get(w.getValue()) - 1);
                w.getKey().stop(false);
                workerIdle.add(w.getKey());
            }
        }
        for (Worker u : removeGas) workerGas.remove(u);

        if (frameCount % 500 == 0) {
            Map<MineralPatch, Long> mineralCount = workerMining.values().stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            for (Entry<MineralPatch, Long> p : mineralCount.entrySet())
                mineralsAssigned.put(p.getKey(), Math.toIntExact(p.getValue()));
        }

        List<Worker> removeTask = new ArrayList<>();
        for (Entry<SCV, Building> w : workerTask.entrySet()) {
            if (!w.getKey().isConstructing() || w.getValue().isCompleted()) removeTask.add(w.getKey());
        }
        for (Worker u : removeTask) {
            workerTask.remove(u);
            u.stop(false);
            workerIdle.add(u);
        }

        if (!strat.name.equals("PlasmaWraithHell")) {
            if (chosenScout != null && ((Worker) chosenScout).isIdle()) {
                workerIdle.add((Worker) chosenScout);
                chosenScout = null;
            }
        }
        if (chosenBuilderBL != null && (chosenBuilderBL.isIdle() || chosenBuilderBL.isGatheringGas() || chosenBuilderBL.isGatheringMinerals())) {
            workerIdle.add(chosenBuilderBL);
            chosenBuilderBL = null;
            movingToExpand = false;
            expanding = false;
            chosenBaseLocation = null;
        }
        if (chosenBuilderBL != null && workerIdle.contains(chosenBuilderBL)) workerIdle.remove(chosenBuilderBL);

        List<Unit> aux3 = new ArrayList<>();
        for (Entry<SCV, MutablePair<UnitType, TilePosition>> u : workerBuild.entrySet()) {
            if ((u.getKey().isIdle() || u.getKey().isGatheringGas() || u.getKey().isGatheringMinerals()) &&
                    Util.broodWarDistance(u.getKey().getPosition(), u.getValue().second.toPosition()) > 100) {
                aux3.add(u.getKey());
                deltaCash.first -= u.getValue().first.mineralPrice();
                deltaCash.second -= u.getValue().first.gasPrice();
                workerIdle.add(u.getKey());
            }
        }
        for (Unit u : aux3) workerBuild.remove(u);

        List<Unit> aux5 = new ArrayList<>();
        for (Worker r : workerDefenders.keySet()) {
            if (!r.exists()) aux5.add(r);
            else if (r.isIdle() || r.isGatheringMinerals()) {
                workerIdle.add(r);
                aux5.add(r);
            }
        }
        for (Unit u : aux5) workerDefenders.remove(u);
    }

    void checkMainEnemyBase() {
        if (enemyBuildingMemory.isEmpty() && ScoutSLs.isEmpty()) {
            enemyMainBase = null;
            chosenScout = null;
            for (Base b : BLs) {
                if (CCs.containsKey(b)) continue;
                if (!strat.name.equals("PlasmaWraithHell") && b.getArea().getAccessibleNeighbors().isEmpty()) {
                    continue;
                }
                ScoutSLs.add(b);
            }
        }
    }

    // Based on BWEB, thanks @Fawx, https://github.com/Cmccrave/BWEB
    void initChokes() {
        try {
            // Main choke
            naturalArea = BLs.get(1).getArea();
            Area mainRegion = BLs.get(0).getArea();
            double distBest = Double.MAX_VALUE;
            for (ChokePoint choke : naturalArea.getChokePoints()) {
                double dist = Util.getGroundDistance(choke.getCenter().toPosition(), getPlayer().getStartLocation().toPosition());
                if (dist < distBest && dist > 0.0) {
                    mainChoke = choke;
                    distBest = dist;
                }
            }
            if (mainChoke != null) {
                initAttackPosition = mainChoke.getCenter().toTilePosition();
                initDefensePosition = mainChoke.getCenter().toTilePosition();
            } else {
                initAttackPosition = self.getStartLocation();
                initDefensePosition = self.getStartLocation();
            }
            // Natural choke
            // Exception for maps with a natural behind the main such as Crossing Fields
            if (Util.getGroundDistance(self.getStartLocation().toPosition(), bwem.getMap().getData().getMapData().getCenter()) < Util.getGroundDistance(BLs.get(1).getLocation().toPosition(), bwem.getMap().getData().getMapData().getCenter())) {
                naturalChoke = mainChoke;
                return;
            }
            // Find area that shares the choke we need to defend
            if (bw.getBWMap().mapHash().compareTo("33527b4ce7662f83485575c4b1fcad5d737dfcf1") == 0 &&
                    BLs.get(0).getLocation().equals(new TilePosition(8, 9))) { // Luna special start location
                naturalChoke = mainChoke;
                mainChoke = BLs.get(0).getArea().getChokePoints().get(0);
            } else if (bw.getBWMap().mapHash().compareTo("8000dc6116e405ab878c14bb0f0cde8efa4d640c") == 0 &&
                    (BLs.get(0).getLocation().equals(new TilePosition(117, 51)) ||
                            BLs.get(0).getLocation().equals(new TilePosition(43, 118)))) { // Alchemist special start location
                naturalChoke = mainChoke;
                double distMax = Double.MAX_VALUE;
                for (ChokePoint p : BLs.get(0).getArea().getChokePoints()) {
                    double dist = p.getCenter().toPosition().getDistance(naturalChoke.getCenter().toPosition());
                    if (dist < distMax) {
                        mainChoke = p;
                        distMax = dist;
                    }
                }
                if (BLs.get(0).getLocation().equals(new TilePosition(117, 51))) {
                    distMax = Double.MIN_VALUE;
                    for (ChokePoint p : BLs.get(1).getArea().getChokePoints()) {
                        double dist = p.getCenter().toPosition().getDistance(mainChoke.getCenter().toPosition());
                        if (dist > distMax) {
                            naturalChoke = p;
                            distMax = dist;
                        }
                    }
                }
            } else if (bw.getBWMap().mapHash().compareTo("aab66dbf9c85f85c47c219277e1e36181fe5f9fc") != 0) {
                distBest = Double.MAX_VALUE;
                Area second = null;
                for (Area a : naturalArea.getAccessibleNeighbors()) {
                    if (a.getTop().equals(mainRegion.getTop())) continue;
                    WalkPosition center = a.getTop();
                    double dist = center.toPosition().getDistance(bwem.getMap().getData().getMapData().getCenter());
                    if (dist < distBest) {
                        second = a;
                        distBest = dist;
                    }
                }
                // Find second choke based on the connected area
                distBest = Double.MAX_VALUE;
                for (ChokePoint choke : naturalArea.getChokePoints()) {
                    if (choke.getCenter() == mainChoke.getCenter()) continue;
                    if (choke.isBlocked() || choke.getGeometry().size() <= 3) continue;
                    if (choke.getAreas().getFirst() != second && choke.getAreas().getSecond() != second) continue;
                    double dist = choke.getCenter().toPosition().getDistance(self.getStartLocation().toPosition());
                    if (dist < distBest) {
                        naturalChoke = choke;
                        distBest = dist;
                    }
                }
            } else {
                distBest = Double.MAX_VALUE;
                for (ChokePoint choke : naturalArea.getChokePoints()) {
                    if (choke.getCenter().equals(mainChoke.getCenter())) continue;
                    if (choke.isBlocked() || choke.getGeometry().size() <= 3) continue;
                    double dist = choke.getCenter().toPosition().getDistance(self.getStartLocation().toPosition());
                    if (dist < distBest) {
                        naturalChoke = choke;
                        distBest = dist;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("initChokes Exception");
            e.printStackTrace();
        }
    }

    public int getArmySize() {
        int count = 0;
        if (sqManager.squads.isEmpty()) return count;
        else for (Squad s : sqManager.squads.values()) count += s.getArmyCount();
        return count + agents.size() * 2;
    }

    public int getArmySize(Set<Unit> units) {
        int count = 0;
        if (units.isEmpty()) return count;
        else {
            for (Unit u : units) {
                count++;
                if (u instanceof SiegeTank || u instanceof Vulture || u instanceof Wraith || u instanceof ScienceVessel)
                    count++;
            }
        }
        return count;
    }

    public boolean checkSupply() {
        for (MutablePair<UnitType, TilePosition> w : workerBuild.values()) {
            if (w.first == UnitType.Terran_Supply_Depot) return true;
        }
        for (Building w : workerTask.values()) {
            if (w instanceof SupplyDepot) return true;
        }
        return false;
    }

    public int getCombatUnitsBuildings() {
        int count = MBs.size() + Fs.size();
        return count == 0 ? 1 : count;
    }

    private double getMineralRate() {
        double rate = 0.0;
        if (frameCount > 0) rate = ((double) self.gatheredMinerals() - 50) / frameCount;
        return rate;
    }

    public Position getCenterFromBuilding(Position leftTop, UnitType type) {
        Position rightBottom = new Position(leftTop.getX() + type.tileWidth() * TilePosition.SIZE_IN_PIXELS, leftTop.getY() + type.tileHeight() * TilePosition.SIZE_IN_PIXELS);
        return new Position((leftTop.getX() + rightBottom.getX()) / 2, (leftTop.getY() + rightBottom.getY()) / 2);

    }

    //TODO Real maths
    public int getMineralsWhenReaching(TilePosition start, TilePosition end) {
        double rate = getMineralRate();
        double distance = Util.getGroundDistance(start.toPosition(), end.toPosition());
        double frames = distance / 2.55;
        return (int) (rate * frames);
    }

    void mineralLocking() {
        for (Entry<Worker, MineralPatch> u : workerMining.entrySet()) {
            if (u.getKey().isIdle() || (u.getKey().getTargetUnit() == null && !Order.MoveToMinerals.equals(u.getKey().getOrder())))
                u.getKey().gather(u.getValue());
            else if (u.getKey().getTargetUnit() != null && !u.getKey().getTargetUnit().equals(u.getValue())
                    && u.getKey().getOrder() == Order.MoveToMinerals && !u.getKey().isCarryingMinerals()) {
                u.getKey().gather(u.getValue());
            }
        }
    }

    public Position getNearestCC(Position position) {
        Unit chosen = null;
        double distance = Double.MAX_VALUE;
        for (Unit u : CCs.values()) {
            double distance_aux = Util.broodWarDistance(u.getPosition(), position);
            if (distance_aux > 0.0 && (chosen == null || distance_aux < distance)) {
                chosen = u;
                distance = distance_aux;
            }
        }
        if (chosen != null) return chosen.getPosition();
        return null;
    }

    void readOpponentInfo() {
        String name = ih.enemy().getName();
        String path = "bwapi-data/read/" + name + ".json";
        try {
            if (Files.exists(Paths.get(path))) {
                EI = enemyInfoJSON.fromJson(new FileReader(path), EnemyInfo.class);
                return;
            }
            path = "bwapi-data/write/" + name + ".json";
            if (Files.exists(Paths.get(path))) {
                EI = enemyInfoJSON.fromJson(new FileReader(path), EnemyInfo.class);
                return;
            }
            path = "bwapi-data/AI/" + name + ".json";
            if (Files.exists(Paths.get(path))) {
                EI = enemyInfoJSON.fromJson(new FileReader(path), EnemyInfo.class);
            }

        } catch (Exception e) {
            System.err.println("readOpponentInfo");
            e.printStackTrace();
        }
    }

    void readOpponentHistory() {
        String name = ih.enemy().getName();
        String path = "bwapi-data/read/" + name + "-History.json";
        try {
            if (Files.exists(Paths.get(path))) {
                EH = enemyHistoryJSON.fromJson(new FileReader(path), EnemyHistory.class);
                return;
            }
            path = "bwapi-data/write/" + name + "-History.json";
            if (Files.exists(Paths.get(path))) {
                EH = enemyHistoryJSON.fromJson(new FileReader(path), EnemyHistory.class);
                return;
            }
            path = "bwapi-data/AI/" + name + "-History.json";
            if (Files.exists(Paths.get(path))) EH = enemyHistoryJSON.fromJson(new FileReader(path), EnemyHistory.class);
        } catch (Exception e) {
            System.err.println("readOpponentHistory");
            e.printStackTrace();
        }
    }

    void writeOpponentInfo(String name) {
        String dir = "bwapi-data/write/";
        String path = dir + name + ".json";
        ih.sendText("Writing result to: " + path);
        Gson aux = new Gson();
        if (enemyIsRandom && EI.naughty) EI.naughty = false;
        String print = aux.toJson(EI);
        File directory = new File(dir);
        if (!directory.exists()) directory.mkdir();
        try (PrintWriter out = new PrintWriter(path)) {
            out.println(print);
        } catch (FileNotFoundException e) {
            System.err.println("writeOpponentInfo");
            e.printStackTrace();
        }
    }

    void writeOpponentHistory(String name) {
        String dir = "bwapi-data/write/";
        String path = dir + name + "-History.json";
        ih.sendText("Writing history to: " + path);
        Gson aux = new Gson();
        String print = aux.toJson(EH);
        File directory = new File(dir);
        if (!directory.exists()) directory.mkdir();
        try (PrintWriter out = new PrintWriter(path)) {
            out.println(print);
        } catch (FileNotFoundException e) {
            System.err.println("writeOpponentHistory");
            e.printStackTrace();
        }
    }

    public TilePosition getBunkerPositionAntiPool() {
        try {
            if (MBs.isEmpty() || CCs.isEmpty()) return null;
            TilePosition startTile = MBs.iterator().next().getTilePosition();
            TilePosition searchTile = CCs.values().iterator().next().getTilePosition();
            UnitType type = UnitType.Terran_Bunker;
            int dist = 0;
            TilePosition chosen = null;
            while (chosen == null || dist < 5) {
                List<TilePosition> sides = new ArrayList<>();
                if (startTile.getY() - type.tileHeight() - dist >= 0) {
                    TilePosition up = new TilePosition(startTile.getX(), startTile.getY() - type.tileHeight() - dist);
                    sides.add(up);
                }
                if (startTile.getY() + UnitType.Terran_Barracks.tileHeight() + dist < bw.getBWMap().mapHeight()) {
                    TilePosition down = new TilePosition(startTile.getX(), startTile.getY() + UnitType.Terran_Barracks.tileHeight() + dist);
                    sides.add(down);
                }
                if (startTile.getX() - type.tileWidth() - dist >= 0) {
                    TilePosition left = new TilePosition(startTile.getX() - type.tileWidth() - dist, startTile.getY());
                    sides.add(left);
                }
                if (startTile.getX() + UnitType.Terran_Barracks.tileWidth() + dist < bw.getBWMap().mapWidth()) {
                    TilePosition right = new TilePosition(startTile.getX() + UnitType.Terran_Barracks.tileWidth() + dist, startTile.getY());
                    sides.add(right);
                }
                for (TilePosition tile : sides) {
                    if (tile == null) continue;
                    if (((chosen == null) || (searchTile.getDistance(tile) < searchTile.getDistance(chosen)))
                            && bw.canBuildHere(tile, type)) {
                        chosen = tile;
                    }
                }
                dist++;
            }
            startTile = CCs.values().iterator().next().getTilePosition();
            UnitType ccType = UnitType.Terran_Command_Center;
            searchTile = mainChoke.getCenter().toTilePosition();
            dist = 0;
            while (dist < 2) {
                List<TilePosition> sides = new ArrayList<>();
                if (startTile.getY() - type.tileHeight() - dist >= 0) {
                    TilePosition up = new TilePosition(startTile.getX(), startTile.getY() - type.tileHeight() - dist);
                    sides.add(up);
                }
                if (startTile.getY() + ccType.tileHeight() + dist < bw.getBWMap().mapHeight()) {
                    TilePosition down = new TilePosition(startTile.getX(), startTile.getY() + ccType.tileHeight() + dist);
                    sides.add(down);
                }
                if (startTile.getX() - type.tileWidth() - dist >= 0) {
                    TilePosition left = new TilePosition(startTile.getX() - type.tileWidth() - dist, startTile.getY());
                    sides.add(left);
                }
                if (startTile.getX() + ccType.tileWidth() + dist < bw.getBWMap().mapWidth()) {
                    TilePosition right = new TilePosition(startTile.getX() + ccType.tileWidth() + dist, startTile.getY());
                    sides.add(right);
                }
                for (TilePosition tile : sides) {
                    if (tile == null) continue;
                    if ((chosen == null) || (searchTile.getDistance(tile) < searchTile.getDistance(chosen))) {
                        if (bw.canBuildHere(tile, type)) chosen = tile;
                    }
                }
                dist++;
            }
            return chosen;
        } catch (Exception e) {
            System.err.println("getBunkerPositionAntiPool Exception");
            e.printStackTrace();
            return null;
        }

    }

    void updateEnemyBuildingsMemory() {
        List<Unit> aux = new ArrayList<>();
        for (EnemyBuilding u : enemyBuildingMemory.values()) {
            if (bw.getBWMap().isVisible(u.pos)) {
                if (!Util.getUnitsOnTile(u.pos).contains(u.unit)) aux.add(u.unit);
                else if (u.unit.isVisible()) u.pos = u.unit.getTilePosition();
                u.type = u.unit.getType();
            }
        }
        for (Unit u : aux) enemyBuildingMemory.remove(u);
    }

    void updateSquadOrderAndMicro() {
        for (Squad u : sqManager.squads.values()) {
            if (u.members.isEmpty()) continue;
            u.microUpdateOrder();
        }
    }

    public int countUnit(UnitType type) {
        int count = 0;
        for (MutablePair<UnitType, TilePosition> w : workerBuild.values()) {
            if (w.first == type) count++;
        }
        count += Util.countUnitTypeSelf(type);
        return count;
    }

    /**
     * Credits and thanks to Yegers for the method
     * Number of workers needed to sustain a number of units.
     * This method assumes that the required buildings are available.
     * Example usage: to sustain building 2 marines at the same time from 2 barracks.
     *
     * @param units List of units that are to be sustained.
     * @return Number of workers required.
     * @author Yegers
     */
    private double mineralGatherRateNeeded(final List<UnitType> units) {
        double mineralsRequired = 0.0;
        double m2f = (4.53 / 100.0) / 65.0;
        double SaturationX2_Slope = -1.5;
        double SaturationX1 = m2f * 65.0;
        double SaturationX2_B = m2f * 77.5;
        for (UnitType unit : units) mineralsRequired += (((double) unit.mineralPrice()) / unit.buildTime()) / 1.0;
        double workersRequired = mineralsRequired / SaturationX1;
        if (workersRequired > mineralsAssigned.size())
            return Math.ceil((mineralsRequired - SaturationX2_B / 1.0) / SaturationX2_Slope);
        return Math.ceil(workersRequired);
    }

    void checkWorkerMilitia() {
        if (countUnit(UnitType.Terran_Barracks) == 2) {
            List<Unit> aux = new ArrayList<>();
            int count = workerMining.size();
            for (Entry<Worker, MineralPatch> scv : workerMining.entrySet()) {
                if (count <= workerCountToSustain) break;
                if (!scv.getKey().isCarryingMinerals()) {
                    scv.getKey().move(new TilePosition(bw.getBWMap().mapWidth() / 2, bw.getBWMap().mapHeight() / 2).toPosition());
                    //addToSquad(scv.getKey());
                    myArmy.add(scv.getKey());
                    if (mineralsAssigned.containsKey(scv.getValue())) {
                        mining--;
                        mineralsAssigned.put(scv.getValue(), mineralsAssigned.get(scv.getValue()) - 1);
                    }
                    aux.add(scv.getKey());
                    count--;
                }
            }
            for (Unit u : aux) workerMining.remove(u);
        }
    }

    public Unit getUnitToAttack(Unit myUnit, Set<Unit> closeSim) {
        Unit chosen = null;
        Set<Unit> workers = new TreeSet<>();
        Set<Unit> combatUnits = new TreeSet<>();
        Unit worker = null;
        for (Unit u : closeSim) {
            if (u instanceof Worker) workers.add(u);
            else combatUnits.add(u);
        }
        if (combatUnits.isEmpty() && workers.isEmpty()) return null;
        if (!workers.isEmpty()) {
            double distB = Double.MAX_VALUE;
            for (Unit u : workers) {
                double distA = Util.broodWarDistance(myUnit.getPosition(), u.getPosition());
                if (worker == null || distA < distB) {
                    worker = u;
                    distB = distA;
                }
            }

        }
        if (!combatUnits.isEmpty()) {
            double distB = Double.MAX_VALUE;
            for (Unit u : combatUnits) {
                double distA = Util.broodWarDistance(myUnit.getPosition(), u.getPosition());
                if (chosen == null || distA < distB) {
                    chosen = u;
                    distB = distA;
                }
            }
        }
        if (chosen != null) return chosen;
        return worker;
    }

    // Credits to @Yegers for a better kite method
    public Position kiteAway(final Unit unit, final Set<Unit> enemies) {
        try {
            if (enemies.isEmpty()) return null;
            Position ownPosition = unit.getPosition();
            List<MutablePair<Double, Double>> vectors = new ArrayList<>();
            //double minDistance = Double.MAX_VALUE;
            for (Unit enemy : enemies) {
                if (!enemy.exists() || !enemy.isVisible()) continue;
                Position enemyPosition = enemy.getPosition();
                MutablePair<Double, Double> unitV = new MutablePair<>((double) (ownPosition.getX() - enemyPosition.getX()), (double) (ownPosition.getY() - enemyPosition.getY()));
                unitV = Util.cropPosition(unitV);
                //double distance = Util.broodWarDistance(ownPosition, enemyPosition);
                //if (distance < minDistance) minDistance = distance;
                //unitV.first = (1 / distance) * unitV.first;
                //unitV.second = (1 / distance) * unitV.second;
                vectors.add(new MutablePair<>(unitV.first, unitV.second));
            }
           /* minDistance = 2 * (minDistance * minDistance);
            for (Pair<Double, Double> vector : vectors) {
                vector.first *= minDistance;
                vector.second *= minDistance;
            }*/
            MutablePair<Double, Double> sumAll = Util.sumPosition(vectors);
            return Util.cropPosition(Util.sumPosition(ownPosition, new Position((int) (sumAll.first / vectors.size()), (int) (sumAll.second / vectors.size()))));
        } catch (Exception e) {
            System.err.println("KiteAway Exception");
            e.printStackTrace();
            return new Position(-1, -1);
        }
    }

    void runAgents() {
        List<Agent> rem = new ArrayList<>();
        for (Agent ag : agents.values()) {
            boolean remove = ag.runAgent();
            if (remove) rem.add(ag);
        }
        for (Agent ag : rem) {
            if (ag instanceof WraithAgent) {
                String wraith = ((WraithAgent) ag).name;
                shipNames.add(wraith);
            } else if (ag instanceof VesselAgent) ((VesselAgent) ag).follow = null;
            agents.remove(ag.myUnit);
        }
    }

    void sendCustomMessage() {
        String name = EI.opponent.toLowerCase();
        if (name.equals("krasi0".toLowerCase())) ih.sendText("Please don't bully me too much!");
        else if (name.equals("hannes bredberg".toLowerCase()) || name.equals("hannesbredberg".toLowerCase())) {
            ih.sendText("Don't you dare nuke me!");
        } else if (name.equals("zercgberht")) {
            ih.sendText("Hello there!, brother");
        } else ih.sendText("BEEEEP BOOOOP!, This king salutes you, " + EI.opponent);
    }

    String pickShipName() {
        if (shipNames.isEmpty()) return "Pepe";
        String name;
        int index = new Random().nextInt(shipNames.size());
        Iterator<String> iter = shipNames.iterator();
        do {
            name = iter.next();
            index--;
        }
        while (index >= 0);
        if (name == null) return "Pepe";
        shipNames.remove(name);
        return name;
    }

    public boolean canAfford(UnitType type) {
        return self.minerals() >= type.mineralPrice() && self.gas() >= type.gasPrice();
    }

    void sendRandomMessage() {
        if (Math.random() < 0.80) return;
        if (Math.random() < 0.50) {
            ih.sendText("What do you call a Zealot smoking weed?");
            ih.sendText("A High Templar");
        } else {
            ih.sendText("Why shouldn't you ask a Protoss for advice?");
            ih.sendText("Because the ones who give the feedback are always high!");
        }
    }

    void alwaysPools() {
        List<String> poolers = new ArrayList<>(Arrays.asList("neoedmundzerg", "peregrinebot", "dawidloranc", "chriscoxe", "zzzkbot", "middleschoolstrats", "zercgberht", "killalll"));
        if (enemyRace == Race.Zerg) {
            if (poolers.contains(EI.opponent.toLowerCase().replace(" ", ""))) {
                EI.naughty = true;
                return;
            }
        }
        EI.naughty = false;
    }

    public boolean requiredUnitsForAttack() {
        return strat.requiredUnitsForAttack();
    }

    void workerTransfer() {
        int numWorkersToTransfer = (workerIdle.size() + workerMining.size()) / 2;
        List<Unit> minerals = BLs.get(1).getMinerals().stream().map(NeutralImpl::getUnit).collect(Collectors.toList());
        boolean hardStuck = false;
        while (numWorkersToTransfer != 0 && !hardStuck) {
            MineralPatch chosenMineral = Collections.min(mineralsAssigned.entrySet().stream().filter(m -> minerals.contains(m.getKey())).collect(Collectors.toSet()), Entry.comparingByValue()).getKey();
            if (chosenMineral == null) break;
            Worker chosen = null;
            if (!workerIdle.isEmpty()) {
                chosen = workerIdle.iterator().next();
                mineralsAssigned.put(chosenMineral, mineralsAssigned.get(chosenMineral) + 1);
                workerMining.put(chosen, chosenMineral);
                workerIdle.remove(chosen);
                numWorkersToTransfer--;
                continue;
            }
            MineralPatch oldPatch = null;
            for (Entry<Worker, MineralPatch> w : workerMining.entrySet()) {
                if (minerals.contains(w.getValue())) continue;
                chosen = w.getKey();
                oldPatch = w.getValue();
                break;
            }
            if (chosen != null && oldPatch != null) {
                mineralsAssigned.put(oldPatch, mineralsAssigned.get(oldPatch) - 1);
                mineralsAssigned.put(chosenMineral, mineralsAssigned.get(chosenMineral) + 1);
                workerMining.put(chosen, chosenMineral);
                numWorkersToTransfer--;
                continue;
            }
            hardStuck = true;
        }
    }

    public boolean needToAttack() {
        if (strat.name.equals("ProxyBBS") && getArmySize() >= strat.armyForAttack && requiredUnitsForAttack())
            return true;
        return getArmySize() >= strat.armyForAttack && requiredUnitsForAttack();
    }

    void updateAttack() {
        try {
            if (sqManager.squads.isEmpty() || defense) return;
            boolean needToAttack = needToAttack();
            for (Squad u : sqManager.squads.values()) {
                if (u.members.isEmpty()) continue;
                if (!needToAttack && u.status != Squad.Status.ATTACK) continue;
                Position attackPos = Util.chooseAttackPosition(u.getSquadCenter(), false);
                if (attackPos != null) {
                    if (!firstProxyBBS && strat.name.equals("ProxyBBS")) {
                        firstProxyBBS = true;
                        getIH().sendText("Get ready for a party in your house!");
                    }
                    if (getGame().getBWMap().isValidPosition(attackPos)) {
                        u.giveAttackOrder(attackPos);
                        u.status = Squad.Status.ATTACK;
                    }
                } else if (enemyMainBase != null) {
                    if (!firstProxyBBS && strat.name.equals("ProxyBBS")) {
                        firstProxyBBS = true;
                        getIH().sendText("Get ready for a party in your house!");
                    }
                    u.giveAttackOrder(enemyMainBase.getLocation().toPosition());
                    u.status = Squad.Status.ATTACK;
                } else u.status = Squad.Status.IDLE;
            }

        } catch (Exception e) {
            System.err.println("Update Attack Exception");
            e.printStackTrace();
        }
    }
}