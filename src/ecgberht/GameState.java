package ecgberht;

import bwem.BWEM;
import bwem.Base;
import bwem.ChokePoint;
import bwem.area.Area;
import bwem.unit.Geyser;
import bwem.unit.Mineral;
import bwta.BWTA;
import bwta.Chokepoint;
import bwta.Region;
import com.google.gson.Gson;
import ecgberht.Agents.VultureAgent;
import ecgberht.Config.ConfigManager;
import ecgberht.Squad.Status;
import ecgberht.Strategies.BioBuild;
import ecgberht.Strategies.BioBuildFE;
import ecgberht.Strategies.BioMechBuild;
import ecgberht.Strategies.BioMechBuildFE;
import jfap.JFAP;
import jfap.JFAPUnit;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.*;
import org.openbw.bwapi4j.type.*;
import org.openbw.bwapi4j.unit.*;
import org.openbw.bwapi4j.util.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

public class GameState extends GameHandler {

    public Base enemyBase = null;
    public boolean activeCount = false;
    public boolean defense = false;
    public boolean enemyIsRandom = true;
    public boolean expanding = false;
    public boolean firstAPM = false;
    public boolean firstProxyBBS = false;
    public boolean initCount = false;
    public boolean movingToExpand = false;
    public boolean scout = true;
    public boolean siegeResearched = false;
    public BuildingMap map;
    public BuildingMap testMap;
    public ChokePoint closestChoke = null;
    public EnemyInfo EI = new EnemyInfo(ih.enemy().getName());
    public Gson enemyInfoJSON = new Gson();
    public InfluenceMap inMap;
    public InfluenceMap inMapUnits;
    public int builtBuildings;
    public int builtCC;
    public int builtRefinery;
    public int deltaSupply;
    public int frameCount;
    public int lastFrameStim = Integer.MIN_VALUE;
    public int mapSize = 2;
    public int mining;
    public int startCount;
    public int trainedCombatUnits;
    public int trainedWorkers;
    public int vulturesTrained = 0;
    public int workerCountToSustain = 0;
    public JFAP simulator;
    public List<Base> blockedBLs = new ArrayList<>();
    public List<Base> BLs = new ArrayList<>();
    public List<Base> EnemyBLs = new ArrayList<>();
    public Map<VespeneGeyser, Boolean> vespeneGeysers = new TreeMap<>(new UnitComparator());
    public Map<GasMiningFacility, Integer> refineriesAssigned = new TreeMap<>(new UnitComparator());
    public Map<SCV, Pair<UnitType, TilePosition>> workerBuild = new TreeMap<>(new UnitComparator());
    public Map<Worker, Position> workerDefenders = new TreeMap<>(new UnitComparator());
    public Map<SCV, Building> repairerTask = new TreeMap<>(new UnitComparator());
    public Map<SCV, Building> workerTask = new TreeMap<>(new UnitComparator());
    public Map<Worker, GasMiningFacility> workerGas = new TreeMap<>(new UnitComparator());
    public long totalTime = 0;
    public Map<Position, MineralPatch> blockingMinerals = new HashMap<>();
    public Map<Position, CommandCenter> CCs = new HashMap<>();
    public Map<String, Squad> squads = new TreeMap<>();
    public Map<Bunker, Set<Unit>> DBs = new TreeMap<>(new UnitComparator());
    public Map<Unit, String> TTMs = new TreeMap<>(new UnitComparator());
    public Map<Unit, EnemyBuilding> enemyBuildingMemory = new TreeMap<>(new UnitComparator());
    public Map<MineralPatch, Integer> mineralsAssigned = new TreeMap<>(new UnitComparator());
    public Map<Worker, MineralPatch> workerMining = new TreeMap<>(new UnitComparator());
    public Map<Player, Integer> players = new HashMap<>();
    public Pair<Integer, Integer> deltaCash = new Pair<Integer, Integer>(0, 0);
    public Pair<String, Unit> chosenMarine = null;
    public Player neutral;
    public Position attackPosition = null;
    public Race enemyRace = Race.Unknown;
    public Area naturalRegion = null;
    public Set<Base> ScoutSLs = new HashSet<>();
    public Set<Base> SLs = new HashSet<>();
    public Set<String> teamNames = new TreeSet<>(Arrays.asList("Alpha", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot", "Golf", "Hotel", "India", "Juliet", "Kilo", "Lima", "Mike", "November", "Oscar", "Papa", "Quebec", "Romeo", "Sierra", "Tango", "Uniform", "Victor", "Whiskey", "X-Ray", "Yankee", "Zulu"));
    public Set<Building> buildingLot = new TreeSet<>(new UnitComparator());
    public Set<ComsatStation> CSs = new TreeSet<>(new UnitComparator());
    public Set<Unit> enemyCombatUnitMemory = new TreeSet<>(new UnitComparator());
    public Set<Unit> enemyInBase = new TreeSet<>(new UnitComparator());
    public Set<Factory> Fs = new TreeSet<>(new UnitComparator());
    public Set<Barracks> MBs = new TreeSet<>(new UnitComparator());
    public Set<Starport> Ps = new TreeSet<>(new UnitComparator());
    public Set<SupplyDepot> SBs = new TreeSet<>(new UnitComparator());
    public Set<MissileTurret> Ts = new TreeSet<>(new UnitComparator());
    public Set<ResearchingFacility> UBs = new TreeSet<>(new UnitComparator());
    public Set<Worker> workerIdle = new TreeSet<>(new UnitComparator());
    public Set<VultureAgent> agents = new TreeSet<>();
    public Strategy strat = new Strategy();
    public String chosenSquad = null;
    public TechType chosenResearch = null;
    public TilePosition checkScan = null;
    public TilePosition chosenBaseLocation = null;
    public TilePosition chosenPosition = null;
    public TilePosition initAttackPosition = null;
    public TilePosition initDefensePosition = null;
    public Worker chosenBuilderBL = null;
    public TrainingFacility chosenBuilding = null;
    public ExtendibleByAddon chosenBuildingAddon = null;
    public Building chosenBuildingLot = null;
    public Building chosenBuildingRepair = null;
    public Unit chosenBunker = null;
    public Worker chosenHarasser = null;
    public SCV chosenRepairer = null;
    public Worker chosenScout = null;
    public Unit chosenUnitToHarass = null;
    public ResearchingFacility chosenUnitUpgrader = null;
    public Worker chosenWorker = null;
    public Unit MainCC = null;
    public UnitType chosenAddon = null;
    public UnitType chosenToBuild = null;
    public UnitType chosenUnit = null;
    public UpgradeType chosenUpgrade = null;
    public boolean iReallyWantToExpand = false;

    public GameState(BW bw, BWTA bwta, BWEM bwem) {
        super(bw, bwta, bwem);
        ConfigManager.readConfig();
        initPlayers();
        map = new BuildingMap(bw, ih.self(), bwem); // Check old source for bwta->bwem
        map.initMap();
        testMap = map.clone();
        inMap = new InfluenceMap(bw, ih.self(), bw.getBWMap().mapHeight(), bw.getBWMap().mapWidth());
        mapSize = bwta.getStartLocations().size();
        simulator = new JFAP(bw);
    }

    public void initPlayers() {
        for (Player p : bw.getAllPlayers()) {
            //if(p.isObserver()) continue;
            if (p.isNeutral()) {
                players.put(p, 0);
                neutral = p;
            } else if (ih.allies().contains(p) || p.equals(self)) {
                players.put(p, 1);
            } else if (ih.enemies().contains(p)) {
                players.put(p, -1);
            }
        }
    }

    public Strategy initStrat() {
        try {
            BioBuild b = new BioBuild();
            // ProxyBBS bbs = new ProxyBBS(); //TODO broken, fix
            BioMechBuild bM = new BioMechBuild();
            BioBuildFE bFE = new BioBuildFE();
            BioMechBuildFE bMFE = new BioMechBuildFE();
            String map = bw.getBWMap().mapFileName();
            if (enemyRace == Race.Zerg && EI.naughty) {
                return new Strategy(b);
            }
            if (EI.history.isEmpty()) {
                if (enemyRace == Race.Protoss) {
                    double random = Math.random();
                    if (random > 0.5) {
                        return new Strategy(b);
                    } else {
                        return new Strategy(bM);
                    }
                }

                if (mapSize == 2 && !map.contains("Heartbreak Ridge")) {
                    double random = Math.random();
                    if (random > 0.5) {
                        return new Strategy(b);
                    } else {
                        return new Strategy(bM);
                    }
                }
                if (map.contains("HeartbreakRidge")) {
                    double random = Math.random();
                    if (random > 0.75) {
                        return new Strategy(bFE);
                    } else {
                        return new Strategy(b);
                    }

                } else {
                    double random = Math.random();
                    if (random > 0.5) {
                        return new Strategy(b);
                    } else {
                        return new Strategy(bM);
                    }
                }
            } else {
                Map<String, Pair<Integer, Integer>> strategies = new TreeMap<>();
                Map<String, AStrategy> nameStrat = new TreeMap<>();

//				strategies.put(bbs.name, new Pair<Integer,Integer>(0,0)); // BROKEN
//				nameStrat.put(bbs.name, bbs);

                strategies.put(bFE.name, new Pair<Integer, Integer>(0, 0));
                nameStrat.put(bFE.name, bFE);

                strategies.put(bMFE.name, new Pair<Integer, Integer>(0, 0));
                nameStrat.put(bMFE.name, bMFE);

                strategies.put(b.name, new Pair<Integer, Integer>(0, 0));
                nameStrat.put(b.name, b);

                strategies.put(bM.name, new Pair<Integer, Integer>(0, 0));
                nameStrat.put(bM.name, bM);

                for (StrategyOpponentHistory r : EI.history) {
                    if (strategies.containsKey(r.strategyName)) {
                        strategies.get(r.strategyName).first += r.wins;
                        strategies.get(r.strategyName).second += r.losses;
                    }
                }

                int totalGamesPlayed = EI.wins + EI.losses;
                int DefaultStrategyWins = strategies.get(b.name).first;
                int DefaultStrategyLosses = strategies.get(b.name).second;
                int strategyGamesPlayed = DefaultStrategyWins + DefaultStrategyLosses;
                double winRate = strategyGamesPlayed > 0 ? DefaultStrategyWins / (double) (strategyGamesPlayed) : 0;
                if (strategyGamesPlayed < 2) {
                    ih.sendText("I dont know you that well yet, lets pick the standard strategy");
                    return new Strategy(b);
                }
                if (strategyGamesPlayed > 0 && winRate > 0.74) {
                    ih.sendText("Using default Strategy with winrate " + winRate * 100 + "%");
                    return new Strategy(b);
                }
                double C = 0.5;
                String bestUCBStrategy = null;
                double bestUCBStrategyVal = Double.MIN_VALUE;
                for (String strat : strategies.keySet()) {
                    if (map.contains("HeartbreakRidge") && (strat == "BioMechFE" || strat == "BioMech" || strat == "FullMech")) {
                        continue;
                    }
                    int sGamesPlayed = strategies.get(strat).first + strategies.get(strat).second;
                    double sWinRate = sGamesPlayed > 0 ? (double) (strategies.get(strat).first / (double) (strategyGamesPlayed)) : 0;
                    double ucbVal = sGamesPlayed == 0 ? C : C * Math.sqrt(Math.log((double) (totalGamesPlayed / sGamesPlayed)));
                    double val = sWinRate + ucbVal;
                    if (val >= bestUCBStrategyVal) {
                        bestUCBStrategy = strat;
                        bestUCBStrategyVal = val;
                    }
                }
                ih.sendText("Chose: " + bestUCBStrategy + " with UCB: " + bestUCBStrategyVal);
                return new Strategy(nameStrat.get(bestUCBStrategy));
            }
        } catch (Exception e) {
            System.err.println("Error initStrat, loading default Strat");
            System.err.println(e);
            BioBuild b = new BioBuild();
            return new Strategy(b);

        }

    }

    public void initEnemyRace() {
        if (ih.enemy().getRace() != Race.Unknown) {
            enemyRace = ih.enemy().getRace();
            enemyIsRandom = false;
        }
    }

    public void initBlockingMinerals() {
        for (MineralPatch u : bw.getMineralPatches()) {
            if (u.getResources() == 0)
                blockingMinerals.put(u.getPosition(), u);
        }
    }

    public void checkBasesWithBLockingMinerals() {
        if (blockingMinerals.isEmpty()) {
            return;
        }
        for (bwem.Base b : BLs) {
            if (b.isStartingLocation()) {
                continue;
            }
            for (ChokePoint c : b.getArea().getChokePoints()) {
                for (Position m : blockingMinerals.keySet()) {
                    if (broodWarDistance(m, c.getCenter().toPosition()) < 40) {
                        blockedBLs.add(b);
                        break;
                    }
                }
            }
        }
    }

    public void playSound(String soundFile) {
        try {
            if(!ConfigManager.getConfig().sounds) return;
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

    public void initCCs() {
        List<PlayerUnit> units = bw.getUnits(self);
        for (PlayerUnit u : units) {
            if (u.getInitialType() == UnitType.Terran_Command_Center) {
                CCs.put(bwta.getRegion(u.getPosition()).getCenter(), (CommandCenter) u);
            }
        }
    }

    public void addNewResources(Unit unit) {
        List<Mineral> minerals = Util.getClosestBaseLocation(unit.getPosition()).getMinerals();
        List<Geyser> gas = Util.getClosestBaseLocation(unit.getPosition()).getGeysers();
        for (Mineral m : minerals) {
            mineralsAssigned.put((MineralPatch) m.getUnit(), 0);
        }
        for (Geyser g : gas) {
            vespeneGeysers.put((VespeneGeyser) g.getUnit(), false);
        }
        if (strat.name == "ProxyBBS") {
            workerCountToSustain = (int) mineralGatherRateNeeded(Arrays.asList(UnitType.Terran_Marine, UnitType.Terran_Marine));
        }
    }

    public void removeResources(Unit unit) {
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
                for (Unit u : aux) {
                    workerMining.remove(u);
                }
                mineralsAssigned.remove(m.getUnit());
            }

        }
        for (Geyser g : gas) {
            VespeneGeyser geyser = (VespeneGeyser) g.getUnit(); // TODO improve
            if (vespeneGeysers.containsKey(geyser)) {
                vespeneGeysers.remove(geyser);
            }
        }
        List<Unit> auxGas = new ArrayList<>();
        for (Entry<GasMiningFacility, Integer> pm : refineriesAssigned.entrySet()) { // TODO test
            for (Geyser g : gas) {
                if (pm.getKey().equals(g.getUnit())) {
                    List<Unit> aux = new ArrayList<>();
                    for (Entry<Worker, GasMiningFacility> w : workerGas.entrySet()) {
                        if (pm.getKey().equals(w.getValue())) {
                            aux.add(w.getKey());
                            workerIdle.add(w.getKey());
                        }
                    }
                    for (Unit u : aux) {
                        workerGas.remove(u);
                    }
                    auxGas.add(pm.getKey());
                }
            }
        }
        for (Unit u : auxGas) {
            refineriesAssigned.remove(u);
        }
        if (strat.name == "ProxyBBS") {
            workerCountToSustain = (int) mineralGatherRateNeeded(Arrays.asList(UnitType.Terran_Marine, UnitType.Terran_Marine));
        }
    }

    public Pair<Integer, Integer> getCash() {
        return new Pair<>(self.minerals(), self.gas());
    }

    public int getSupply() {
        return (self.supplyTotal() - self.supplyUsed());
    }

    public void printer() {
        if(!ConfigManager.getConfig().debug) return;
        Integer counter = 0;
        for (bwem.Base b : BLs) {
            bw.getMapDrawer().drawTextMap(b.getLocation().toPosition(), counter.toString());
            counter++;
        }
        bw.getMapDrawer().drawTextScreen(10, 50, "Next Building: " + chosenToBuild);

        for (VultureAgent vulture : agents) {
            bw.getMapDrawer().drawTextMap(vulture.unit.getPosition(), vulture.statusToString());
        }

        bw.getMapDrawer().drawTextScreen(10, 80, "Strategy: " + strat.name);
        if (closestChoke != null) {
            bw.getMapDrawer().drawTextMap(closestChoke.getCenter().toPosition(), "Choke");
        }

        if (chosenBuilderBL != null) {
            bw.getMapDrawer().drawTextMap(chosenBuilderBL.getPosition(), "BuilderBL");
            print(chosenBuilderBL, Color.BLUE);
        }
        if (chosenHarasser != null) {
            bw.getMapDrawer().drawTextMap(chosenHarasser.getPosition(), "Harasser");
            print(chosenHarasser, Color.BLUE);
        }
        if (chosenBaseLocation != null) {
            print(chosenBaseLocation, UnitType.Terran_Command_Center, Color.CYAN);
        }
        for (Entry<SCV, Pair<UnitType, TilePosition>> u : workerBuild.entrySet()) {
            bw.getMapDrawer().drawTextMap(u.getKey().getPosition(), "ChosenBuilder");
            print(u.getValue().second, u.getValue().first, Color.TEAL);
        }
        if (chosenUnitToHarass != null) {
            print(chosenUnitToHarass, Color.RED);
            bw.getMapDrawer().drawTextMap(chosenUnitToHarass.getPosition(), "UnitToHarass");
        }
        for (SCV r : repairerTask.keySet()) {
            print(r, Color.YELLOW);
            bw.getMapDrawer().drawTextMap(r.getPosition(), "Repairer");
        }
        bw.getMapDrawer().drawTextScreen(10, 5, self.getName() + " vs " + ih.enemy().getName());
        if (chosenScout != null) {
            bw.getMapDrawer().drawTextMap(chosenScout.getPosition(), "Scouter");
            print(chosenScout, Color.PURPLE);
            bw.getMapDrawer().drawTextScreen(10, 20, "Scouting: " + "True");
        } else {
            bw.getMapDrawer().drawTextScreen(10, 20, "Scouting: " + "False");
        }
        if (enemyBase != null) {
            bw.getMapDrawer().drawTextScreen(10, 35, "Enemy Base Found: " + "True");
        } else {
            bw.getMapDrawer().drawTextScreen(10, 35, "Enemy Base Found: " + "False");
        }
//		if (chosenWorker != null) {
//			game.drawTextMap(chosenWorker.getPosition(), "ChosenWorker");
//		}
        if (chosenRepairer != null) {
            bw.getMapDrawer().drawTextMap(chosenRepairer.getPosition(), "ChosenRepairer");
        }
//		if(enemyCombatUnitMemory.size()>0) {
//			for(Unit u : enemyCombatUnitMemory) {
//				game.drawTextMap(u.getPosition(), u.getType().toString());
//				print(u,Color.Red);
//			}
//		}
        List<Region> regions = bwta.getRegions();
        for (Region reg : regions) {
            List<Chokepoint> ch = reg.getChokepoints();
            for (Chokepoint c : ch) {
                Pair<Position, Position> lados = c.getSides();
                bw.getMapDrawer().drawLineMap(lados.first, lados.second, Color.GREEN);
            }
        }
        for (Unit u : CCs.values()) {
            print(u, Color.YELLOW);
            bw.getMapDrawer().drawCircleMap(u.getPosition(), 500, Color.ORANGE);
        }
        for (Unit u : DBs.keySet()) {
            bw.getMapDrawer().drawCircleMap(u.getPosition(), 300, Color.ORANGE);
        }
        for (Unit u : workerIdle) {
            print(u, Color.GREEN);
        }
        for (Worker u : workerDefenders.keySet()) {
            print(u, Color.PURPLE);
            bw.getMapDrawer().drawTextMap(u.getPosition(), "Spartan");
        }
        for (Entry<Worker, MineralPatch> u : workerMining.entrySet()) {
            print((Unit) u.getKey(), Color.ORANGE);
            bw.getMapDrawer().drawLineMap(u.getKey().getPosition(), u.getValue().getPosition(), Color.RED);
        }

        for (Entry<String, Squad> s : squads.entrySet()) {
            if (s.getValue().members.isEmpty()) continue;
            Position center = getSquadCenter(s.getValue());
            bw.getMapDrawer().drawCircleMap(center, 80, Color.GREEN);
            bw.getMapDrawer().drawTextMap(center, s.getKey());
        }
        if (enemyRace == Race.Zerg && EI.naughty) {
            bw.getMapDrawer().drawTextScreen(10, 95, "Naughty Zerg: " + "yes");
        }
        for (Unit m : mineralsAssigned.keySet()) {
            print(m, Color.CYAN);
            bw.getMapDrawer().drawTextMap(m.getPosition(), mineralsAssigned.get(m).toString());
        }
    }

    public void print(Unit u, Color color) {
        bw.getMapDrawer().drawBoxMap(u.getLeft(), u.getTop(), u.getRight(), u.getBottom(), color);
    }

    public void print(TilePosition u, UnitType type, Color color) {
        Position leftTop = new Position(u.getX() * TilePosition.SIZE_IN_PIXELS, u.getY() * TilePosition.SIZE_IN_PIXELS);
        Position rightBottom = new Position(leftTop.getX() + type.tileWidth() * TilePosition.SIZE_IN_PIXELS, leftTop.getY() + type.tileHeight() * TilePosition.SIZE_IN_PIXELS);
        bw.getMapDrawer().drawBoxMap(leftTop, rightBottom, color);
    }

    public void print(TilePosition u, Color color) {
        Position leftTop = new Position(u.getX() * TilePosition.SIZE_IN_PIXELS, u.getY() * TilePosition.SIZE_IN_PIXELS);
        Position rightBottom = new Position(leftTop.getX() + TilePosition.SIZE_IN_PIXELS, leftTop.getY() + TilePosition.SIZE_IN_PIXELS);
        bw.getMapDrawer().drawBoxMap(leftTop, rightBottom, color);
    }

    public String convertSeconds(int seconds) {
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        String sh = (h > 0 ? String.valueOf(h) + " " + "h" : "");
        String sm = (m < 10 && m > 0 && h > 0 ? "0" : "") + (m > 0 ? (h > 0 && s == 0 ? String.valueOf(m) : String.valueOf(m) + " " + "min") : "");
        String ss = (s == 0 && (h > 0 || m > 0) ? "" : (s < 10 && (h > 0 || m > 0) ? "0" : "") + String.valueOf(s) + " " + "sec");
        return sh + (h > 0 ? " " : "") + sm + (m > 0 ? " " : "") + ss;
    }

    public void initStartLocations() {
        Base startBot = Util.getClosestBaseLocation(self.getStartLocation().toPosition());
        for (bwem.Base b : bwem.getMap().getBases()) {
            if (b.isStartingLocation() && !b.getLocation().equals(startBot.getLocation())) {
                SLs.add(b);
                ScoutSLs.add(b);
            }
        }
    }

    public void initBaseLocations() {
        BLs.addAll(bwem.getMap().getBases());
        Collections.sort(BLs, new BaseLocationComparator(false));
    }

    public void moveUnitFromChokeWhenExpand() {
        try {
            if (!squads.isEmpty() && chosenBaseLocation != null) {
                Area chosenRegion = bwem.getMap().getArea(chosenBaseLocation);
                if (chosenRegion != null) {
                    if (chosenRegion.equals(naturalRegion)) {
                        TilePosition mapCenter = new TilePosition(bw.getBWMap().mapWidth(), bw.getBWMap().mapHeight());
                        List<ChokePoint> cs = chosenRegion.getChokePoints();
                        ChokePoint closestChoke = null;
                        for (ChokePoint c : cs) {
                            if (!c.getCenter().toTilePosition().equals(this.closestChoke.getCenter().toTilePosition())) {
                                double aux = broodWarDistance(c.getCenter().toPosition(), chosenBaseLocation.toPosition());
                                if (aux > 0.0) {
                                    if (closestChoke == null || aux < broodWarDistance(closestChoke.getCenter().toPosition(), mapCenter.toPosition())) {
                                        closestChoke = c;
                                    }
                                }
                            }
                        }
                        if (closestChoke != null) {
                            for (Squad s : squads.values()) {
                                if (s.status == Status.IDLE) {
                                    s.giveAttackOrder(closestChoke.getCenter().toPosition());
                                    s.status = Status.ATTACK;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("MoveUnitFromChokeWhenExpand");
            System.err.println(e);
        }
    }

    public void fix() {
        if (defense && enemyInBase.isEmpty()) {
            defense = false;
        }
        List<String> squadsToClean = new ArrayList<>();
        for (Squad s : squads.values()) {
            List<Unit> aux = new ArrayList<>();
            for (Unit u : s.members) {
                if (!u.exists()) {
                    aux.add(u);
                }
            }

            if (s.members.isEmpty() || aux.size() == s.members.size()) {
                squadsToClean.add(s.name);
                continue;
            } else {
                s.members.removeAll(aux);
            }
        }
        List<Unit> bunkers = new ArrayList<>();
        for (Entry<Bunker, Set<Unit>> u : DBs.entrySet()) {
            if (u.getKey().exists()) continue;
            for (Unit m : u.getValue()) {
                if (m.exists()) addToSquad(m);
            }
            bunkers.add(u.getKey());
        }
        for (Unit c : bunkers) DBs.remove(c);

        for (String name : squadsToClean) {
            squads.remove(name);
        }
        if (chosenScout != null && chosenScout.isIdle()) {
            workerIdle.add(chosenScout);
            chosenScout = null;
        }
        if (chosenBuilderBL != null && (chosenBuilderBL.isIdle() || chosenBuilderBL.isGatheringGas() || chosenBuilderBL.isGatheringMinerals())) {
            workerIdle.add(chosenBuilderBL);
            chosenBuilderBL = null;
            movingToExpand = false;
            expanding = false;
            chosenBaseLocation = null;
        }
        if (chosenBuilderBL != null && workerIdle.contains(chosenBuilderBL)) {
            workerIdle.remove(chosenBuilderBL);
        }

        List<Unit> aux3 = new ArrayList<>();
        for (Entry<SCV, Pair<UnitType, TilePosition>> u : workerBuild.entrySet()) {
            if ((u.getKey().isIdle() || u.getKey().isGatheringGas() || u.getKey().isGatheringMinerals()) &&
                    broodWarDistance(u.getKey().getPosition(), u.getValue().second.toPosition()) > 100) {
                aux3.add(u.getKey());
                deltaCash.first -= u.getValue().first.mineralPrice();
                deltaCash.second -= u.getValue().first.gasPrice();
                workerIdle.add(u.getKey());
            }
        }
        for (Unit u : aux3) workerBuild.remove(u);

        List<Unit> aux4 = new ArrayList<>();
        for (SCV r : repairerTask.keySet()) {
            if (r.equals(chosenScout)) {
                chosenScout = null;
            }
            if (!r.isRepairing() || r.isIdle()) {
                if (chosenRepairer != null) {
                    if (r.equals(chosenRepairer)) {
                        chosenRepairer = null;
                    }
                }
                workerIdle.add(r);
                aux4.add(r);
            }
        }
        for (Unit u : aux4) repairerTask.remove(u);

        List<Unit> aux5 = new ArrayList<>();
        for (Worker r : workerDefenders.keySet()) {
            if (r.isIdle() || r.isGatheringMinerals()) {
                workerIdle.add(r);
                aux5.add(r);
            }
        }
        for (Unit u : aux5) workerDefenders.remove(u);

        List<String> aux6 = new ArrayList<>();
        for (Squad u : squads.values()) {
            if (u.members.isEmpty()) {
                aux6.add(u.name);
            }
        }
        for (String s : aux6) {
            squads.remove(s);
        }
    }

    public void checkMainEnemyBase() {
        if (enemyBuildingMemory.isEmpty() && ScoutSLs.isEmpty()) {
            enemyBase = null;
            chosenScout = null;
            ScoutSLs.clear();
            for (bwem.Base b : BLs) {
                if (!CCs.containsKey(b.getArea().getTop().toPosition()) && bwta.isConnected(self.getStartLocation(), b.getLocation())) {
                    ScoutSLs.add(b);
                }
            }
        }
    }

//	public void checkEnemyAttackingWT() {
//		if(!workerTask.isEmpty()) {
//			List<Pair<Unit,Unit> > aux = new ArrayList<Pair<Unit,Unit> >();
//			for(Pair<Unit,Unit> p : workerTask) {
//				if(p.second.getInitialType().isBuilding() && !p.second.getType().isNeutral() && p.second.isBeingConstructed()) {
//					if((p.first.isUnderAttack()) && (p.second.getType() != UnitType.Terran_Bunker && p.second.getType() != UnitType.Terran_Missile_Turret)) {
//						p.first.haltConstruction();
//						workerIdle.add(p.first);
//						buildingLot.add(p.second);
//						aux.add(p);
//					}
//				}
//			}
//			workerTask.removeAll(aux);
//		}
//	}

    public void initClosestChoke() {
        List<bwem.Base> aux = BLs;
        Area naturalArea = aux.get(1).getArea();
        naturalRegion = naturalArea;
        double distBest = Double.MAX_VALUE;
        for (ChokePoint choke : naturalArea.getChokePoints()) {
            double dist = bwta.getGroundDistance(choke.getCenter().toTilePosition(), getPlayer().getStartLocation());
            if (dist < distBest && dist > 0.0)
                closestChoke = choke;
            distBest = dist;
        }
        if (closestChoke != null) {
            initAttackPosition = closestChoke.getCenter().toTilePosition();
            initDefensePosition = closestChoke.getCenter().toTilePosition();
        } else {
            initAttackPosition = self.getStartLocation();
            initDefensePosition = self.getStartLocation();
        }
    }

    public void checkUnitsBL(TilePosition BL, Unit chosen) {
        UnitType type = UnitType.Terran_Command_Center;
        Position topLeft = new Position(BL.getX() * TilePosition.SIZE_IN_PIXELS, BL.getY() * TilePosition.SIZE_IN_PIXELS);
        Position bottomRight = new Position(topLeft.getX() + type.tileWidth() * TilePosition.SIZE_IN_PIXELS, topLeft.getY() + type.tileHeight() * TilePosition.SIZE_IN_PIXELS);
        List<Unit> blockers = Util.getUnitsInRectangle(topLeft, bottomRight);
        if (!blockers.isEmpty()) {
            for (Unit u : blockers) {
                if (((PlayerUnit) u).getPlayer().getId() == self.getId() && !u.equals(chosen) && !(u instanceof Worker)) {
                    ((MobileUnit) u).move(Util.getClosestChokepoint(BL.toPosition()).getCenter().toPosition());
                }
            }
        }
    }

    public String getSquadName() {
        if (teamNames.size() == squads.size()) {
            String gg = null;
            while (gg == null || squads.containsKey(gg)) {
                gg = "RandomSquad" + new Random().toString();
            }
            return gg;
        }
        String nombre = null;
        while (nombre == null || squads.containsKey(nombre)) {
            int index = new Random().nextInt(teamNames.size());
            Iterator<String> iter = teamNames.iterator();
            for (int i = 0; i < index; i++) {
                nombre = iter.next();
            }
        }
        return nombre;
    }

    public String addToSquad(Unit unit) {
        String nombre = "";
        if (squads.size() == 0) {
            Squad aux = new Squad(getSquadName());
            aux.addToSquad(unit);
            squads.put(aux.name, aux);
            nombre = aux.name;
        } else {
            String chosen = null;
            for (Entry<String, Squad> s : squads.entrySet()) {
                if (s.getValue().members.size() < 12 && broodWarDistance(getSquadCenter(s.getValue()), unit.getPosition()) < 1000 && (chosen == null || broodWarDistance(unit.getPosition(), getSquadCenter(s.getValue())) < broodWarDistance(unit.getPosition(), getSquadCenter(squads.get(chosen))))) {
                    chosen = s.getKey();
                }
            }
            if (chosen != null) {
                squads.get(chosen).addToSquad(unit);
                nombre = chosen;
            } else {
                Squad nuevo = new Squad(getSquadName());
                nuevo.addToSquad(unit);
                squads.put(nuevo.name, nuevo);
                nombre = nuevo.name;
            }
        }
        return nombre;
    }

    public Position getSquadCenter(Squad s) {
        Position point = new Position(0, 0);
        for (Unit u : s.members) {
            if (s.members.size() == 1) {
                return u.getPosition();
            }
            point = new Position(point.getX() + u.getPosition().getX(), point.getY() + u.getPosition().getY());

        }
        return new Position(point.getX() / s.members.size(), point.getY() / s.members.size());

    }

    public void removeFromSquad(Unit unit) {
        for (Entry<String, Squad> s : squads.entrySet()) {
            if (s.getValue().members.contains(unit)) {
                if (s.getValue().members.size() == 1) {
                    squads.remove(s.getKey());
                } else {
                    s.getValue().members.remove(unit);
                }
                break;
            }
        }
    }

    public int getArmySize() {
        int count = 0;
        if (squads.isEmpty()) {
            return count;
        } else {
            for (Entry<String, Squad> s : squads.entrySet()) {
                count += s.getValue().members.size();
            }
        }
        return count + agents.size();
    }

    public void siegeTanks() {
        if (!squads.isEmpty()) {
            Set<SiegeTank> tanks = new TreeSet<>(new UnitComparator());
            for (Entry<String, Squad> s : squads.entrySet()) {
                tanks.addAll(s.getValue().getTanks());
            }
            if (!tanks.isEmpty()) {
                for (SiegeTank t : tanks) {
                    //List<Unit> unitsInRange = t.getUnitsInRadius(UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange());
                    boolean far = false;
                    boolean close = false;
                    for (Unit e : enemyCombatUnitMemory) {
                        double distance = broodWarDistance(e.getPosition(), t.getPosition());
                        if (distance > UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange()) {
                            continue;
                        }
                        if (distance <= UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().minRange()) {
                            close = true;
                            break;
                        }
                        UnitType eType = Util.getType((PlayerUnit) e);
                        if (Util.isEnemy(((PlayerUnit) e).getPlayer()) && !(e instanceof Worker) && !eType.isFlyer() && (eType.canAttack() || eType == UnitType.Terran_Bunker)) {
                            far = true;
                            break;
                        }
                    }
                    if (close && !far) {
                        if (t.isSieged() && t.getOrder() != Order.Unsieging) t.unsiege();
                        continue;
                    }
                    if (far) {
                        if (!t.isSieged() && t.getOrder() != Order.Sieging) {
                            t.siege();
                        }
                        continue;
                    }
                    if (t.isSieged() && t.getOrder() != Order.Unsieging) t.unsiege();
                }
            }
        }
    }

    public boolean checkSupply() {
        for (Pair<UnitType, TilePosition> w : workerBuild.values()) {
            if (w.first == UnitType.Terran_Supply_Depot) {
                return true;
            }
        }
        for (Building w : workerTask.values()) {
            if (w instanceof SupplyDepot) {
                return true;
            }
        }
        return false;
    }

    public int getCombatUnitsBuildings() {
        int count = 0;
        count = MBs.size() + Fs.size();
        if (count == 0) {
            return 1;
        }
        return count;
    }

    public double getMineralRate() {
        double rate = 0.0;
        if (frameCount > 0) {
            rate = ((double) self.gatheredMinerals() - 50) / frameCount;
        }
        return rate;
    }

    public Position getCenterFromBuilding(Position leftTop, UnitType type) {
        Position rightBottom = new Position(leftTop.getX() + type.tileWidth() * TilePosition.SIZE_IN_PIXELS, leftTop.getY() + type.tileHeight() * TilePosition.SIZE_IN_PIXELS);
        Position center = new Position((leftTop.getX() + rightBottom.getX()) / 2, (leftTop.getY() + rightBottom.getY()) / 2);
        return center;

    }

    //TODO Real maths
    public int getMineralsWhenReaching(Unit u, TilePosition start, TilePosition end) {
        double rate = getMineralRate();
        //Pair<Double,Double> speed = new Pair<Double,Double>(u.getVelocityX(),u.getVelocityY());
        double distance = bwta.getGroundDistance(start, end);
        //double top = u.getType().topSpeed();
        //double aceleration = u.getType().acceleration();
        double frames = distance / 2.55;
        int mineralsWhenReach = (int) (rate * frames);
//		System.out.println("--------------");
//		System.out.println("RatioMRR: " + rate);
//		System.out.println("Speed(x,y) " + speed);
//		System.out.println("Distancia: " + distance);
//		System.out.println("TopSpeed: " + top);
//		System.out.println("Aceleration: " + aceleration);
//		System.out.println("framesToReach: " + frames);
//		System.out.println("Actual frame: " + frameCount);
//		System.out.println("Minerales when reaching: " + mineralsWhenReach);
//		System.out.println("--------------");
        return mineralsWhenReach;
    }

    public void mineralLocking() {
        for (Entry<Worker, MineralPatch> u : workerMining.entrySet()) {
            if (u.getKey().getTargetUnit() != null) {
                if (!u.getKey().getTargetUnit().equals(u.getValue()) && u.getKey().getOrder() == Order.MoveToMinerals && !u.getKey().isCarryingMinerals() || u.getKey().isIdle()) {
                    u.getKey().gather(u.getValue());
                }
            }
        }
    }

    public Position getNearestCC(Position position) {
        Unit chosen = null;
        double distance = Double.MAX_VALUE;
        for (Unit u : CCs.values()) {
            double distance_aux = broodWarDistance(u.getPosition(), position);
            if (distance_aux > 0.0 && (chosen == null || distance_aux < distance)) {
                chosen = u;
                distance = distance_aux;
            }
        }
        if (chosen != null) {
            return chosen.getPosition();
        }
        return null;
    }

    public void readOpponentInfo() {
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
                return;
            }
        } catch (Exception e) {
            System.err.println("readOpponentInfo");
            System.err.println(e);
        }
    }

    public void writeOpponentInfo(String name) {
        String dir = "bwapi-data/write/";
        String path = dir + name + ".json";
        ih.sendText("Writing result to: " + path);
        Gson aux = new Gson();
        if (enemyIsRandom && EI.naughty) {
            EI.naughty = false;
        }
        String print = aux.toJson(EI);
        File directory = new File(dir);
        if (!directory.exists()) {
            directory.mkdir();
        }
        try (PrintWriter out = new PrintWriter(path)) {
            out.println(print);
        } catch (FileNotFoundException e) {
            System.err.println("writeOpponentInfo");
            System.err.println(e);
        }
    }

    public TilePosition getBunkerPositionAntiPool() {
        try {
            TilePosition rax = MBs.iterator().next().getTilePosition();
            UnitType bunker = UnitType.Terran_Bunker;
            int dist = 0;
            TilePosition chosen = null;
            while (chosen == null) {
                List<TilePosition> sides = new ArrayList<TilePosition>();
                if (rax.getY() - bunker.tileHeight() - dist >= 0) {
                    TilePosition up = new TilePosition(rax.getX(), rax.getY() - bunker.tileHeight() - dist);
                    sides.add(up);
                }
                if (rax.getY() + UnitType.Terran_Barracks.tileHeight() + dist < bw.getBWMap().mapHeight()) {
                    TilePosition down = new TilePosition(rax.getX(), rax.getY() + UnitType.Terran_Barracks.tileHeight() + dist);
                    sides.add(down);
                }
                if (rax.getX() - bunker.tileWidth() - dist >= 0) {
                    TilePosition left = new TilePosition(rax.getX() - bunker.tileWidth() - dist, rax.getY());
                    sides.add(left);
                }
                if (rax.getX() + UnitType.Terran_Barracks.tileWidth() + dist < bw.getBWMap().mapWidth()) {
                    TilePosition right = new TilePosition(rax.getX() + UnitType.Terran_Barracks.tileWidth() + dist, rax.getY());
                    sides.add(right);
                }
                for (TilePosition tile : sides) {
                    if ((chosen == null && bw.canBuildHere(tile, bunker)) || (closestChoke.getCenter().toTilePosition().getDistance(tile) < closestChoke.getCenter().toTilePosition().getDistance(chosen) && bw.canBuildHere(tile, bunker))) {
                        chosen = tile;
                    }
                }
                dist++;
            }
            return chosen;
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }

    }

    public void updateEnemyBuildingsMemory() {
        List<Unit> aux = new ArrayList<Unit>();
        for (EnemyBuilding u : enemyBuildingMemory.values()) {
            if (bw.getBWMap().isVisible(u.pos)) {
                if (!Util.getUnitsOnTile(u.pos).contains(u.unit)) { // TODO test
                    aux.add(u.unit);
                } else if (u.unit.isVisible()) {
                    u.pos = u.unit.getTilePosition();
                }
                u.type = Util.getType(u.unit);
            }

        }
        for (Unit u : aux) {
            enemyBuildingMemory.remove(u);
        }

    }

    public void mergeSquads() {
        try {
            if (squads.isEmpty()) {
                return;
            }
            if (squads.size() < 2) {
                return;
            }
            for (Squad u1 : squads.values()) {
                int u1_size = u1.members.size();
                if (u1_size < 12) {
                    for (Squad u2 : squads.values()) {
                        if (u2.name.equals(u1.name) || u2.members.size() > 11) {
                            continue;
                        }
                        if (broodWarDistance(getSquadCenter(u1), getSquadCenter(u2)) < 200) {
                            if (u1_size + u2.members.size() > 12) {
                                continue;
                            } else {
                                u1.members.addAll(u2.members);
                                u2.members.clear();

                            }
                            break;
                        }
                    }
                    break;
                }
            }
            Set<Squad> aux = new HashSet<Squad>();
            for (Squad u : squads.values()) {
                if (u.members.isEmpty()) {
                    aux.add(u);
                }
            }
            squads.values().removeAll(aux);
        } catch (Exception e) {
            System.err.println("mergeSquads");
            System.err.println(e);
        }
    }

    public void updateSquadOrderAndMicro() {
        for (Squad u : squads.values()) {
            u.microUpdateOrder();
        }
    }

    public int countUnit(UnitType type) {
        int count = 0;
        for (Pair<UnitType, TilePosition> w : workerBuild.values()) {
            if (w.first == type) {
                count++;
            }
        }
        count += Util.countUnitTypeSelf(type);
        return count;
    }

    /**
     * Thanks to Yegers for the method
     *
     * @param units List of units that are to be sustained.
     * @return Number of workers required.
     * @author Yegers
     * Number of workers needed to sustain a number of units.
     * This method assumes that the required buildings are available.
     * Example usage: to sustain building 2 marines at the same time from 2 barracks.
     */
    public double mineralGatherRateNeeded(final List<UnitType> units) {
        double mineralsRequired = 0.0;
        double m2f = (4.53 / 100.0) / 65.0;
        double SaturationX2_Slope = -1.5;
        double SaturationX1 = m2f * 65.0;
        double SaturationX2_B = m2f * 77.5;
        for (final UnitType unit : units) {
            mineralsRequired += (((double) unit.mineralPrice()) / unit.buildTime()) / 1.0;
        }
        double workersRequired = mineralsRequired / SaturationX1;
        if (workersRequired > mineralsAssigned.size()) {
            return Math.ceil((mineralsRequired - SaturationX2_B / 1.0) / SaturationX2_Slope);
        }
        return Math.ceil(workersRequired);
    }

    public void checkWorkerMilitia() {
        if (countUnit(UnitType.Terran_Barracks) == 2) {
            List<Unit> aux = new ArrayList<>();
            int count = workerMining.size();
            for (Entry<Worker, MineralPatch> scv : workerMining.entrySet()) {
                if (count <= workerCountToSustain) {
                    break;
                }
                if (!scv.getKey().isCarryingMinerals()) {
                    scv.getKey().move(new TilePosition(bw.getBWMap().mapWidth() / 2, bw.getBWMap().mapHeight() / 2).toPosition());
                    addToSquad(scv.getKey());
                    if (mineralsAssigned.containsKey(scv.getValue())) {
                        mining--;
                        mineralsAssigned.put(scv.getValue(), mineralsAssigned.get(scv.getValue()) - 1);
                    }
                    aux.add(scv.getKey());
                    count--;

                }
            }
            for (Unit u : aux) {
                workerMining.remove(u);
            }
        }

    }

    public boolean armyGroupedBBS() {
        boolean allFine = true;
        for (Squad s : squads.values()) {
            if (s.attack != null) { // TODO test
                if (s.members.size() == 1) {
                    continue;
                }
                List<Unit> circle = Util.getFriendlyUnitsInRadius(getSquadCenter(s), 190);
                Set<Unit> different = new HashSet<>();
                different.addAll(circle);
                different.addAll(s.members);
                circle.retainAll(s.members);
                different.removeAll(circle);
                if (circle.size() != s.members.size()) {
                    allFine = false;
                    for (Unit u : different) {
                        ((MobileUnit) u).attack(getSquadCenter(s));
                    }
                }
            }
        }
        return allFine;
    }

    //Credits to @PurpleWaveJadien
    public double broodWarDistance(Position a, Position b) {
        double dx = Math.abs(a.getX() - b.getX());
        double dy = Math.abs(a.getY() - b.getY());
        double d = Math.min(dx, dy);
        double D = Math.max(dx, dy);
        if (d < D / 4) {
            return D;
        }
        return D - D / 16 + d * 3 / 8 - D / 64 + d * 3 / 256;

    }

    public Pair<Boolean, Boolean> simulateDefenseBattle(Set<Unit> friends, Set<Unit> enemies, int frames, boolean bunker) {
        simulator.clear();
        Pair<Boolean, Boolean> result = new Pair<>(true, false);
        for (Unit u : friends) {
            simulator.addUnitPlayer1(new JFAPUnit(u));
        }
        for (Unit u : enemies) {
            simulator.addUnitPlayer2(new JFAPUnit(u));
        }
        jfap.Pair<Integer, Integer> presim_scores = simulator.playerScores();
//		int presim_my_unit_count = simulator.getState().first.size();
//		int presim_enemy_unit_count = simulator.getState().second.size();
        simulator.simulate(frames);
//		int postsim_my_unit_count = simulator.getState().first.size();
//		int postsim_enemy_unit_count = simulator.getState().second.size();
        jfap.Pair<Integer, Integer> postsim_scores = simulator.playerScores();
//		int my_losses = presim_my_unit_count - postsim_my_unit_count;
//		int enemy_losses = presim_enemy_unit_count - postsim_enemy_unit_count;
        int my_score_diff = presim_scores.first - postsim_scores.first;
        int enemy_score_diff = presim_scores.second - postsim_scores.second;
//		System.out.println("----- SIM RESULTS -----");
//		System.out.println("My losses : " + my_losses);
//		System.out.println("Enemy losses : " + enemy_losses);
//		System.out.println("My score diff : " + my_score_diff);
//		System.out.println("Enemy score diff : " + enemy_score_diff);
//		System.out.println("-----------------------");
        if (enemy_score_diff * 2 < my_score_diff) {
            result.first = false;
        }
        if (bunker) {
            boolean bunkerDead = true;
            for (JFAPUnit unit : simulator.getState().first) {
                if (unit.unitType == UnitType.Terran_Bunker) {
                    bunkerDead = false;
                    break;
                }
            }
            if (bunkerDead) {
                result.second = true;
            }
        }

        return result;
    }

    public boolean simulateHarass(Unit harasser, List<Unit> enemies, int frames) {
        simulator.clear();
        simulator.addUnitPlayer1(new JFAPUnit(harasser));
        for (Unit u : enemies) {
            simulator.addUnitPlayer2(new JFAPUnit(u));
        }
        int preSimFriendlyUnitCount = simulator.getState().first.size();
        simulator.simulate(frames);
        int postSimFriendlyUnitCount = simulator.getState().first.size();
        int myLosses = preSimFriendlyUnitCount - postSimFriendlyUnitCount;
        if (myLosses > 0) {
            return false;
        }
        return true;
    }

    public boolean simulateHarass(Unit harasser, Set<Unit> enemies, int frames) {
        simulator.clear();
        simulator.addUnitPlayer1(new JFAPUnit(harasser));
        for (Unit u : enemies) {
            simulator.addUnitPlayer2(new JFAPUnit(u));
        }
        int preSimFriendlyUnitCount = simulator.getState().first.size();
        simulator.simulate(frames);
        int postSimFriendlyUnitCount = simulator.getState().first.size();
        int myLosses = preSimFriendlyUnitCount - postSimFriendlyUnitCount;
        if (myLosses > 0) {
            return false;
        }
        return true;
    }

    public double getGroundDistance(TilePosition start, TilePosition end) {
        double dist = 0.0;
        if (bwta.getRegion(start) == null || bwta.getRegion(end) == null) return Integer.MAX_VALUE;

        for (TilePosition cpp : bwta.getShortestPath(start, end)) {
            Position center = cpp.toPosition();
            dist += broodWarDistance(start.toPosition(), center);
            start = center.toTilePosition();
        }
        return dist += broodWarDistance(start.toPosition(), end.toPosition());
    }

    public Unit getUnitToAttack(Unit myUnit, Set<Unit> closeSim) {
        Unit chosen = null;
        Set<Unit> workers = new HashSet<>();
        Set<Unit> combatUnits = new HashSet<>();
        Unit worker = null;
        for (Unit u : closeSim) {
            if (u.getInitialType().isWorker()) {
                workers.add(u);
            }
            if (!(u instanceof Worker) && (u instanceof Attacker)) {
                combatUnits.add(u);
            }
        }
        if (combatUnits.isEmpty() && workers.isEmpty()) {
            return null;
        }
        if (!workers.isEmpty()) {
            double distB = Double.MAX_VALUE;
            for (Unit u : workers) {
                double distA = broodWarDistance(myUnit.getPosition(), u.getPosition());
                if (worker == null || distA < distB) {
                    worker = u;
                    distB = distA;
                }
            }

        }
        if (!combatUnits.isEmpty()) {
            double distB = Double.MAX_VALUE;
            for (Unit u : combatUnits) {
                double distA = broodWarDistance(myUnit.getPosition(), u.getPosition());
                if (chosen == null || distA < distB) {
                    chosen = u;
                    distB = distA;
                }
            }
        }
        if (chosen != null) {
            return chosen;
        }
        if (worker != null) {
            return worker;
        }
        return null;
    }

    // Credits to @Yegers for a better kite method
    public Position kiteAway(final Unit unit, final Set<Unit> enemies) {
        if (enemies.isEmpty()) {
            return null;
        }
        final Position ownPosition = unit.getPosition();
        //TODO add walls
        final List<Pair<Double, Double>> vectors = new ArrayList<>();

        double minDistance = Double.MAX_VALUE;
        for (final Unit enemy : enemies) {
            final Position enemyPosition = enemy.getPosition();
            final Pair<Double, Double> unitV = new Pair<>((double) (ownPosition.getX() - enemyPosition.getX()), (double) (ownPosition.getY() - enemyPosition.getY()));
            final double distance = ownPosition.getDistance(enemyPosition);
            if (distance < minDistance) {
                minDistance = distance;
            }
            unitV.first = (1 / distance) * unitV.first;
            unitV.second = (1 / distance) * unitV.second;
            vectors.add(unitV);
        }
        minDistance = 2 * minDistance * minDistance;
        for (final Pair<Double, Double> vector : vectors) {
            vector.first *= minDistance;
            vector.second *= minDistance;
        }
        Pair<Double, Double> sumAll = Util.sumPosition(vectors);
        return Util.sumPosition(ownPosition, new Position((int) (sumAll.first / vectors.size()), (int) (sumAll.second / vectors.size())));
    }

    public void runAgents() {
        List<VultureAgent> rem = new ArrayList<>();
        for (VultureAgent vulture : agents) {
            boolean remove = vulture.runAgent();
            if (remove) {
                rem.add(vulture);
            }
        }
        for (VultureAgent vult : rem) {
            agents.remove(vult);
        }
    }

    public void sendCustomMessage() {
        String name = EI.opponent.toLowerCase();

        if (name == "krasi0".toLowerCase()) {
            ih.sendText("Please be nice to me!");
        }
        if (name == "hannes bredberg".toLowerCase()) {
            ih.sendText("Dont you dare nuke me!");
        }
        if (name == "zercgberht" || name == "protecgberht") {
            ih.sendText("Hey there!, brother");
            ih.sendText("As the oldest of the three I'm not gonna lose");
        }
    }
}