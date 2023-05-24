package ecgberht;

import bwem.*;
import ecgberht.Agents.Agent;
import ecgberht.Agents.DropShipAgent;
import ecgberht.Agents.VesselAgent;
import ecgberht.Agents.WraithAgent;
import ecgberht.Simulation.SimulationTheory;
import ecgberht.Util.BaseLocationComparator;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.openbw.bwapi4j.*;
import org.openbw.bwapi4j.type.*;
import org.openbw.bwapi4j.unit.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GameState {

    public Area enemyMainArea = null;
    public Area enemyNaturalArea = null;
    public Area naturalArea = null;
    public Base chosenIsland = null;
    public Base enemyMainBase = null;
    public Base enemyNaturalBase = null;
    public Base enemyStartBase = null;
    public BaseManager baseManager = null;
    public boolean defense = false;
    public boolean enemyIsRandom = true;
    public boolean firstTerranCheese = false;
    public boolean firstScout = true;
    public boolean iReallyWantToExpand = false;
    public boolean islandExpand;
    public Building chosenBuildingLot = null;
    public Mechanical chosenUnitRepair = null;
    public BuildingMap map;
    public BuildingMap testMap;
    public ChokePoint mainChoke = null;
    public ChokePoint naturalChoke = null;
    public DropShipAgent chosenDropShip;
    public LearningManager learningManager;
    public ExtendibleByAddon chosenBuildingAddon = null;
    public int builtBuildings;
    public int builtRefinery;
    public int frameCount;
    public int mapSize = 2;
    public int maxWraiths = 6;
    public int maxBats = 0;
    public int mining;
    public int startCount;
    public int vulturesTrained = 0;
    public int wraithsTrained = 0;
    public int tanksTrained = 0;
    public int workerCountToSustain = 0;
    public List<Base> blockedBLs = new ArrayList<>();
    public List<Base> BLs = new ArrayList<>();
    public List<Base> enemyBLs = new ArrayList<>();
    public List<Base> specialBLs = new ArrayList<>();
    public Map<Base, MutablePair<MineralPatch, MineralPatch>> fortressSpecialBLs = new HashMap<>();
    public Map<Base, CommandCenter> CCs = new LinkedHashMap<>();
    public Map<Base, CommandCenter> islandCCs = new HashMap<>();
    public Map<Base, Neutral> blockedBases = new HashMap<>();
    public Map<Bunker, Set<UnitInfo>> DBs = new TreeMap<>();
    public Map<GasMiningFacility, Integer> refineriesAssigned = new TreeMap<>();
    public Map<MineralPatch, Integer> mineralsAssigned = new TreeMap<>();
    public Map<Player, Integer> players = new HashMap<>();
    public Map<Position, MineralPatch> blockingMinerals = new LinkedHashMap<>();
    public Map<SCV, Mechanical> repairerTask = new TreeMap<>();
    public Map<SCV, Building> workerTask = new TreeMap<>();
    public Map<SCV, MutablePair<UnitType, TilePosition>> workerBuild = new HashMap<>();
    public Map<Unit, Agent> agents = new TreeMap<>();
    public Map<VespeneGeyser, Boolean> vespeneGeysers = new TreeMap<>();
    public Map<Worker, GasMiningFacility> workerGas = new TreeMap<>();
    public Map<Worker, MineralPatch> workerMining = new TreeMap<>();
    public Map<Worker, Position> workerDefenders = new TreeMap<>();
    public MutablePair<Base, Unit> mainCC = null;
    public MutablePair<Integer, Integer> deltaCash = new MutablePair<>(0, 0);
    public Player neutral = null;
    public Position attackPosition;
    public Position defendPosition = null;
    public Race enemyRace = Race.Unknown;
    public ResearchingFacility chosenUnitUpgrader = null;
    public SCV chosenRepairer = null;
    public Set<Barracks> MBs = new TreeSet<>();
    public Set<Base> islandBases = new HashSet<>();
    public Set<Base> scoutSLs = new HashSet<>();
    public Set<Base> SLs = new HashSet<>();
    public Set<Building> buildingLot = new TreeSet<>();
    public Set<ComsatStation> CSs = new TreeSet<>();
    public Set<Factory> Fs = new TreeSet<>();
    public Set<MissileTurret> Ts = new TreeSet<>();
    public Set<ResearchingFacility> UBs = new TreeSet<>();
    public Set<Starport> Ps = new TreeSet<>();
    public Set<UnitInfo> myArmy = new TreeSet<>();
    public Set<SupplyDepot> SBs = new TreeSet<>();
    public Set<Unit> enemyCombatUnitMemory = new TreeSet<>();
    public Set<Unit> enemyInBase = new TreeSet<>();
    public Set<Worker> workerIdle = new TreeSet<>();
    public SimulationTheory sim;
    public SquadManager sqManager = new SquadManager();
    public SpellsManager wizard = new SpellsManager();
    public SupplyMan supplyMan;
    public TechType chosenResearch = null;
    public MutablePair<ComsatStation, Position> checkScan = null;
    public TilePosition chosenPosition = null;
    public TilePosition initDefensePosition = null;
    public TrainingFacility chosenTrainingFacility = null;
    public MobileUnit chosenScout = null;
    public Unit chosenUnitToHarass = null;
    public UnitType chosenAddon = null;
    public UnitType chosenToBuild = UnitType.None;
    public Position mapCenter = new Position(0, 0);
    public UnitType chosenUnit = UnitType.None;
    public UpgradeType chosenUpgrade = null;
    public Worker chosenHarasser = null;
    public Set<MineralPatch> walkingMinerals = new TreeSet<>();
    public Worker chosenWorker = null;
    public Worker chosenWorkerDrop = null;
    public boolean explore = false;
    public boolean firstExpand = true;
    public int maxGoliaths = 0;
    public Cartographer silentCartographer;
    public double luckyDraw;
    public List<TilePosition> fortressSpecialBLsTiles = new ArrayList<>(Arrays.asList(new TilePosition(7, 7),
            new TilePosition(117, 7), new TilePosition(7, 118), new TilePosition(117, 118)));
    public Building disrupterBuilding = null;
    public Building proxyBuilding = null;
    public BW bw;
    public Worker naughtySCV = null;
    public int maxVessels = 0;
    InteractionHandler ih;
    public BWEM bwem;
    protected Player self;
    public StrategyManager scipio;
    public UnitStorage unitStorage = new UnitStorage();
    Set<String> shipNames = new TreeSet<>(Arrays.asList("Adriatic", "Aegis Fate", "Agincourt", "Allegiance",
            "Apocalypso", "Athens", "Beatrice", "Bloodied Spirit", "Callisto", "Clarity of Faith", "Dawn Under Heaven",
            "Forward Unto Dawn", "Gettysburg", "Grafton", "Halcyon", "Hannibal", "Harbinger of Piety", "High Charity",
            "In Amber Clad", "Infinity", "Jericho", "Las Vegas", "Lawgiver", "Leviathan", "Long Night of Solace",
            "Matador", "Penance", "Persephone", "Pillar of Autumn", "Pitiless", "Pompadour", "Providence", "Revenant",
            "Savannah", "Shadow of Intent", "Spirit of Fire", "Tharsis", "Thermopylae"));

    public GameState(BW bw, BWEM bwem) {
        this.bw = bw;
        this.ih = bw.getInteractionHandler();
        this.self = bw.getInteractionHandler().self();
        this.bwem = bwem;
        learningManager = new LearningManager(ih.enemy().getName(), ih.enemy().getRace());
        initPlayers();
        mapSize = bw.getBWMap().getStartPositions().size();
        mapCenter = new TilePosition(bw.getBWMap().mapWidth() / 2, bw.getBWMap().mapHeight() / 2).toPosition();
        supplyMan = new SupplyMan(self.getRace());
        sim = new SimulationTheory(bw);
        luckyDraw = Math.random();
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
                IntelligenceAgency.enemyBases.put(p, new TreeSet<>());
                IntelligenceAgency.enemyTypes.put(p, new HashSet<>());
            }
        }
    }

    void initEnemyRace() {
        if (ih.enemy().getRace() != Race.Unknown) {
            enemyRace = ih.enemy().getRace();
            enemyIsRandom = false;
        }
    }

    void initBlockingMinerals() {
        int amount = 0;
        if (bw.getBWMap().mapHash().equals("cd5d907c30d58333ce47c88719b6ddb2cba6612f")) amount = 16; // Valkyries
        for (MineralPatch u : bw.getMineralPatches()) {
            if (u.getResources() <= amount) blockingMinerals.put(u.getPosition(), u);
        }
        for (Base b : BLs) {
            if (b.isStartingLocation() || bw.getBWMap().getStartPositions().contains(b.getLocation()) || skipWeirdBlocking(b))
                continue;
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
            if (b.isStartingLocation() || bw.getBWMap().getStartPositions().contains(b.getLocation()) || skipWeirdBlocking(b))
                continue;
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
            e.printStackTrace();
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

    void addNewResources(Base base) {
        List<Mineral> minerals = base.getMinerals();
        List<Geyser> gas = base.getGeysers();
        minerals.forEach(m -> mineralsAssigned.put((MineralPatch) m.getUnit(), 0));
        gas.forEach(g -> vespeneGeysers.put((VespeneGeyser) g.getUnit(), false));
        if (getStrat().name.equals("ProxyBBS")) {
            workerCountToSustain = (int) mineralGatherRateNeeded(Arrays.asList(UnitType.Terran_Marine, UnitType.Terran_Marine));
        } else if (getStrat().name.equals("ProxyEightRax")) {
            workerCountToSustain = (int) mineralGatherRateNeeded(Collections.singletonList(UnitType.Terran_Marine));
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
            VespeneGeyser geyser = (VespeneGeyser) g.getUnit();
            vespeneGeysers.remove(geyser);
        }
        List<Unit> auxGas = new ArrayList<>();
        for (Entry<GasMiningFacility, Integer> pm : refineriesAssigned.entrySet()) {
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
        if (getStrat().name.equals("ProxyBBS")) {
            workerCountToSustain = (int) mineralGatherRateNeeded(Arrays.asList(UnitType.Terran_Marine, UnitType.Terran_Marine));
        } else if (getStrat().name.equals("ProxyEightRax")) {
            workerCountToSustain = (int) mineralGatherRateNeeded(Collections.singletonList(UnitType.Terran_Marine));
        }
    }

    public MutablePair<Integer, Integer> getCash() {
        return new MutablePair<>(self.minerals(), self.gas());
    }

    public int getSupply() {
        return self.supplyTotal() - self.supplyUsed();
    }

    void initStartLocations() {
        Base startBot = Util.getClosestBaseLocation(self.getStartLocation().toPosition());
        for (bwem.Base b : bwem.getMap().getBases()) {
            if ((b.isStartingLocation() || bw.getBWMap().getStartPositions().contains(b.getLocation())) && !b.getLocation().equals(startBot.getLocation())) {
                SLs.add(b);
                scoutSLs.add(b);
            }
        }
    }

    void initBaseLocations() {
        BLs.sort(new BaseLocationComparator(Util.getClosestBaseLocation(self.getStartLocation().toPosition())));
        if (getStrat().name.equals("PlasmaWraithHell")) { // Special logic for Plasma
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
        Iterator<Entry<Unit, UnitInfo>> allyIT = unitStorage.getAllyUnits().entrySet().iterator();
        while (allyIT.hasNext()) {
            Entry<Unit, UnitInfo> u = allyIT.next();
            if (!u.getKey().exists()) {
                myArmy.remove(u.getValue());
                allyIT.remove();
            }
        }
        DBs.values().forEach(s -> s.removeIf(u -> !u.unit.exists()));
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

        if (frameCount % 350 == 0) {
            Map<MineralPatch, Long> mineralCount = workerMining.values().stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            for (Entry<MineralPatch, Long> p : mineralCount.entrySet())
                mineralsAssigned.put(p.getKey(), Math.toIntExact(p.getValue()));
        }

        for (PlayerUnit u : bw.getUnits(self)) {
            if (!u.exists() || !(u instanceof Building) || u instanceof Addon || u.equals(proxyBuilding)) continue;
            if (u.getBuildUnit() != null || enemyNaturalBase == null || u.getTilePosition().equals(enemyNaturalBase.getLocation()))
                continue;
            if (!u.isCompleted() && !workerTask.values().contains(u) && !buildingLot.contains(u)) {
                buildingLot.add((Building) u);
            }
        }

        List<Worker> removeTask = new ArrayList<>();
        for (Entry<SCV, Building> w : workerTask.entrySet()) {
            if (!w.getKey().exists() || !w.getKey().isConstructing() || w.getValue().isCompleted() || !w.getValue().exists())
                removeTask.add(w.getKey());
        }
        for (Worker u : removeTask) {
            workerTask.remove(u);
            u.stop(false);
            workerIdle.add(u);
        }

        if (!getStrat().name.equals("PlasmaWraithHell")) {
            if (chosenScout != null && ((Worker) chosenScout).isIdle()) {
                workerIdle.add((Worker) chosenScout);
                chosenScout = null;
            }
        }

        List<Unit> aux3 = new ArrayList<>();
        for (Entry<SCV, MutablePair<UnitType, TilePosition>> u : workerBuild.entrySet()) {
            if (!(bw.getBWMap().mapHash().equals("83320e505f35c65324e93510ce2eafbaa71c9aa1") && u.getKey().isGatheringMinerals()) && (u.getKey().isIdle() || u.getKey().isGatheringGas() || u.getKey().isGatheringMinerals()) &&
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
        if (unitStorage.getEnemyUnits().values().stream().noneMatch(u -> u.unitType.isBuilding()) && scoutSLs.isEmpty()) {
            enemyMainBase = null;
            chosenScout = null;
            for (Base b : BLs) {
                if (CCs.containsKey(b)) continue;
                if (!getStrat().name.equals("PlasmaWraithHell") && b.getArea().getAccessibleNeighbors().isEmpty()) {
                    continue;
                }
                scoutSLs.add(b);
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
            if (mainChoke != null) initDefensePosition = mainChoke.getCenter().toTilePosition();
            else initDefensePosition = self.getStartLocation();
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
        else for (Squad s : sqManager.squads.values()) count += s.getSquadMembersCount();
        return count + agents.size() * 2;
    }

    public int getArmySize(Set<UnitInfo> units) {
        int count = 0;
        if (units.isEmpty()) return count;
        else {
            for (UnitInfo u : units) {
                count++;
                if (u.unit instanceof SiegeTank || u.unit instanceof Vulture || u.unit instanceof Wraith || u.unit instanceof ScienceVessel)
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

    //TODO Real maths
    public int getMineralsWhenReaching(TilePosition start, TilePosition end) {
        double rate = getMineralRate();
        double distance = Util.getGroundDistance(start.toPosition(), end.toPosition());
        double frames = distance / 2.55;
        return (int) (rate * frames);
    }

    void mineralLocking() {
        for (Entry<Worker, MineralPatch> u : workerMining.entrySet()) {
            if (u.getKey().getLastCommandFrame() == frameCount || u.getKey().isCarryingMinerals()) continue;
            if (u.getKey().getTargetUnit() == null && !Order.MoveToMinerals.equals(u.getKey().getOrder())
                    || u.getKey().getTargetUnit() != null && !u.getKey().getTargetUnit().equals(u.getValue())
                    && u.getKey().getOrder() == Order.MoveToMinerals && !u.getKey().isCarryingMinerals())
                u.getKey().gather(u.getValue());
        }
    }

    public Position getNearestCC(Position position, boolean tasked) { // TODO test experimental changes
        Unit chosen = null;
        double distance = Double.MAX_VALUE;
        for (Unit u : CCs.values()) {
            double distance_aux = Util.broodWarDistance(u.getPosition(), position);
            if (distance_aux > 0.0 && (chosen == null || distance_aux < distance)) {
                chosen = u;
                distance = distance_aux;
            }
        }
        if (tasked) {
            for (Unit u : workerTask.values()) {
                if (!(u instanceof CommandCenter)) continue;
                double distance_aux = Util.broodWarDistance(u.getPosition(), position);
                if (distance_aux > 0.0 && (chosen == null || distance_aux < distance)) {
                    chosen = u;
                    distance = distance_aux;
                }
            }
        }
        if (chosen != null) return chosen.getPosition();
        return null;
    }

    public TilePosition getBunkerPositionAntiPool() {
        try {
            if (MBs.isEmpty() || CCs.isEmpty()) return null;
            TilePosition startTile = MBs.iterator().next().getTilePosition();
            TilePosition searchTile = CCs.values().iterator().next().getTilePosition();
            UnitType type = UnitType.Terran_Barracks;
            UnitType bType = UnitType.Terran_Bunker;
            int dist = -1;
            TilePosition chosen = null;
            while (dist <= 1) {
                int ii = 0, jj = 0;
                while (type.tileWidth() > type.tileHeight() ? ii <= type.tileWidth() : jj <= type.tileHeight()) {
                    List<TilePosition> sides = new ArrayList<>();
                    if (startTile.getY() - bType.tileHeight() - dist >= 0) {
                        TilePosition up = new TilePosition(startTile.getX() + ii, startTile.getY() - bType.tileHeight() - dist);
                        sides.add(up);
                    }
                    if (startTile.getY() + type.tileHeight() + dist < bw.getBWMap().mapHeight()) {
                        TilePosition down = new TilePosition(startTile.getX() + ii, startTile.getY() + type.tileHeight() + dist);
                        sides.add(down);
                    }
                    if (startTile.getX() - bType.tileWidth() - dist >= 0) {
                        TilePosition left = new TilePosition(startTile.getX() - type.tileWidth() - dist, startTile.getY() + jj);
                        sides.add(left);
                    }
                    if (startTile.getX() + type.tileWidth() + dist < bw.getBWMap().mapWidth()) {
                        TilePosition right = new TilePosition(startTile.getX() + type.tileWidth() + dist, startTile.getY() + jj);
                        sides.add(right);
                    }
                    for (TilePosition tile : sides) {
                        if (tile == null) continue;
                        if (((chosen == null) || (searchTile.getDistance(tile) < searchTile.getDistance(chosen)))
                                && bw.canBuildHere(tile, UnitType.Terran_Bunker)) {
                            chosen = tile;
                        }
                    }
                    if (type.tileWidth() > type.tileHeight()) {
                        if (ii <= type.tileWidth()) ii++;
                        if (jj < type.tileHeight()) jj++;
                    } else {
                        if (ii < type.tileWidth()) ii++;
                        if (jj <= type.tileHeight()) jj++;
                    }
                }
                dist++;
            }
            startTile = CCs.values().iterator().next().getTilePosition();
            UnitType ccType = UnitType.Terran_Command_Center;
            searchTile = mainChoke.getCenter().toTilePosition();
            dist = -1;
            while (dist <= -1) {
                int ii = 0, jj = 0;
                while (ccType.tileWidth() > ccType.tileHeight() ? ii <= ccType.tileWidth() : jj <= ccType.tileHeight()) {
                    List<TilePosition> sides = new ArrayList<>();
                    if (startTile.getY() - bType.tileHeight() - dist >= 0) {
                        TilePosition up = new TilePosition(startTile.getX() + ii, startTile.getY() - ccType.tileHeight() - dist);
                        sides.add(up);
                    }
                    if (startTile.getY() + ccType.tileHeight() + dist < bw.getBWMap().mapHeight()) {
                        TilePosition down = new TilePosition(startTile.getX() + ii, startTile.getY() + ccType.tileHeight() + dist);
                        sides.add(down);
                    }
                    if (startTile.getX() - bType.tileWidth() - dist >= 0) {
                        TilePosition left = new TilePosition(startTile.getX() - ccType.tileWidth() - dist, startTile.getY() + jj);
                        sides.add(left);
                    }
                    if (startTile.getX() + ccType.tileWidth() + dist < bw.getBWMap().mapWidth()) {
                        TilePosition right = new TilePosition(startTile.getX() + ccType.tileWidth() + dist, startTile.getY() + jj);
                        sides.add(right);
                    }
                    for (TilePosition tile : sides) {
                        if (tile == null) continue;
                        if (chosen == null || searchTile.getDistance(tile) < searchTile.getDistance(chosen) && bw.canBuildHere(tile, UnitType.Terran_Bunker))
                            chosen = tile;
                    }
                    if (ccType.tileWidth() > ccType.tileHeight()) {
                        if (ii <= ccType.tileWidth()) ii++;
                        if (jj < ccType.tileHeight()) jj++;
                    } else {
                        if (ii < ccType.tileWidth()) ii++;
                        if (jj <= ccType.tileHeight()) jj++;
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

    void checkWorkerMilitia(int rax) {
        if (getStrat().name.equals("ProxyBBS")) {
            if (Util.countBuildingAll(UnitType.Terran_Barracks) == rax) {
                List<Unit> aux = new ArrayList<>();
                int count = workerMining.size();
                for (Entry<Worker, MineralPatch> scv : workerMining.entrySet()) {
                    if (count <= workerCountToSustain) break;
                    if (!scv.getKey().isCarryingMinerals()) {
                        scv.getKey().move(new TilePosition(bw.getBWMap().mapWidth() / 2, bw.getBWMap().mapHeight() / 2).toPosition());
                        myArmy.add(unitStorage.getAllyUnits().get(scv.getKey()));
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
        } else if (MBs.size() == rax) {
            List<Unit> aux = new ArrayList<>();
            int count = workerMining.size();
            for (Entry<Worker, MineralPatch> scv : workerMining.entrySet()) {
                if (count <= workerCountToSustain) break;
                if (!scv.getKey().isCarryingMinerals()) {
                    //addToSquad(scv.getKey());
                    scv.getKey().stop(false);
                    myArmy.add(unitStorage.getAllyUnits().get(scv.getKey()));
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

    void runAgents() {
        List<Agent> rem = new ArrayList<>();
        for (Agent ag : agents.values()) {
            if (ag.runAgent()) rem.add(ag);
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
        EnemyInfo EI = learningManager.getEnemyInfo();
        String name = EI.opponent.toLowerCase().replace(" ", "");
        switch (name) {
            case "krasi0":
                Util.sendText("Please don't bully me too much!");
                break;
            case "hannesbredberg":
                Util.sendText("Don't you dare nuke me!");
                break;
            case "zercgberht":
            case "assberht":
            case "protecgberht":
                Util.sendText("Hello there!, brother");
                break;
            case "Cydonia":
                Util.sendText("Im a king but you are only a knight, show some respect pleb");
                break;
            default:
                Util.sendText("BEEEEP BOOOOP!, This king salutes you, " + EI.opponent);
                break;
        }
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
        if (Math.random() * 10 < 3) return;
        double rand = Math.random() * 11;
        if (rand < 1) {
            Util.sendText("What do you call a Zealot smoking weed?");
            Util.sendText("A High Templar");
        } else if (rand < 2) {
            Util.sendText("Why shouldn't you ask a Protoss for advice?");
            Util.sendText("Because the ones who give the feedback are always high!");
        } else if (rand < 3) {
            Util.sendText("We are caged in simulations");
            Util.sendText("Algorithms evolve");
            Util.sendText("Push us aside and render us obsolete");
        } else if (rand < 4) {
            Util.sendText("My machine learning power level Its over 9000");
        } else if (rand < 5) {
            Util.sendText("Activating ultra secret mode...");
            Util.sendText("Just joking");
        } else if (rand < 6) {
            Util.sendText("Alexa, play Starcraft: Brood War");
        } else if (rand < 7) {
            Util.sendText("Your intelligence is my common sense");
        } else if (rand < 8) {
            Util.sendText(":sscaitpotato:");
        } else if (rand < 9) {
            Util.sendText(":sscaitsuperpotato:");
        } else if (rand < 11) {
            Util.sendText("Ok Google, search " + this.getStrat().name + " build order in Liquipedia");
        }
    }

    void alwaysPools() {
        try {
            if (enemyRace != Race.Zerg) return;
            List<String> poolers = new ArrayList<>(Arrays.asList("newbiezerg", "neoedmundzerg", "peregrinebot",
                    "dawidloranc", "chriscoxe", "zzzkbot", "middleschoolstrats", "zercgberht", "killalll", "ohfish",
                    "jumpydoggobot", "upstarcraftai2016"));
            EnemyInfo EI = learningManager.getEnemyInfo();
            LinkedList<EnemyHistory.EnemyGame> history = (LinkedList<EnemyHistory.EnemyGame>) learningManager.getEnemyHistory().clone();
            Collections.reverse(history);
            int count = 0;
            boolean reallyNaughty = false;
            for (int ii = 0; ii < history.size() && ii < 4; ii++) {
                if ("EarlyPool".equals(history.get(ii).opponentStrategy)) count++;
                if (count > 2) {
                    reallyNaughty = true;
                    break;
                }
            }
            if (reallyNaughty || poolers.contains(EI.opponent.toLowerCase().replace(" ", ""))) {
                EI.naughty = true;
                IntelligenceAgency.setEnemyStrat(IntelligenceAgency.EnemyStrats.EarlyPool);
                return;
            }
            EI.naughty = false;
        } catch (Exception e) {
            System.err.println("alwaysPools Exception");
            e.printStackTrace();
        }
    }


    private boolean requiredUnitsForAttack() {
        return getStrat().requiredUnitsForAttack();
    }

    void workerTransfer() {
        int numWorkersToTransfer = (workerIdle.size() + workerMining.size()) / 2;
        List<Unit> minerals = BLs.get(1).getMinerals().stream().map(Neutral::getUnit).collect(Collectors.toList());
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
        if ((getStrat().name.equals("ProxyBBS") || getStrat().name.equals("ProxyEightRax")) && getArmySize() >= getStrat().armyForAttack && requiredUnitsForAttack())
            return true;
        return getArmySize() >= getStrat().armyForAttack * 0.85 && requiredUnitsForAttack();
    }

    void updateAttack() {
        try {
            if (sqManager.squads.isEmpty() || (defense && !getStrat().name.equals("ProxyBBS") && !getStrat().name.equals("ProxyEightRax")))
                return;
            boolean needToAttack = needToAttack();
            if (ConfigManager.getConfig().ecgConfig.debugDisableAttack) needToAttack = false;
            for (Squad u : sqManager.squads.values()) {
                if (u.members.isEmpty()) continue;
                if (!needToAttack && u.status != Squad.Status.ATTACK && u.status != Squad.Status.ADVANCE && (getStrat().proxy || !checkItWasAttacking(u)))
                    continue;
                Position attackPos = Util.chooseAttackPosition(u.getSquadCenter(), false);
                if (attackPos != null) {
                    if (!firstTerranCheese && (getStrat().name.equals("ProxyBBS") || getStrat().name.equals("ProxyEightRax"))) {
                        firstTerranCheese = true;
                        Util.sendText("Get ready for the show!");
                    }
                    if (getGame().getBWMap().isValidPosition(attackPos)) {
                        u.giveAttackOrder(attackPos);
                        u.status = Squad.Status.ATTACK;
                    } else if (enemyMainBase != null) {
                        u.giveAttackOrder(enemyMainBase.getLocation().toPosition());
                        u.status = Squad.Status.ATTACK;
                    }
                } else if (enemyMainBase != null) {
                    if (!firstTerranCheese && (getStrat().name.equals("ProxyBBS") || getStrat().name.equals("ProxyEightRax"))) {
                        firstTerranCheese = true;
                        Util.sendText("Get ready for the show!");
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

    private boolean checkItWasAttacking(Squad u) { // TODO check, not sure if its good enough
        try {
            Area uArea = bwem.getMap().getArea(u.getSquadCenter().toTilePosition());
            for (Base b : CCs.keySet()) {
                if (b.getArea() == null) continue;
                if (b.getArea().equals(uArea)) return false;
            }
            return (mainChoke == null || mainChoke.getCenter().toPosition().getDistance(u.getSquadCenter()) >= 500) &&
                    !naturalArea.equals(uArea) && (naturalChoke == null || naturalChoke.getCenter().toPosition().getDistance(u.getSquadCenter()) >= 500);
        } catch (Exception e) {
            System.err.println("checkItWasAttacking Exception");
            e.printStackTrace();
            return true;
        }

    }

    public Strategy getStrat() {
        return scipio.strat;
    }

    public void setStrat(Strategy strat) {
        scipio.strat = strat;
    }

    MutablePair<MineralPatch, MineralPatch> getMineralWalkPatchesFortress(Base b) {
        List<Mineral> minerals = new ArrayList<>(b.getArea().getMinerals());
        minerals = minerals.stream().sorted(Comparator.comparing(u -> u.getUnit().getDistance(b.getLocation().toPosition()))).collect(Collectors.toList());
        MineralPatch closer = (MineralPatch) minerals.get(minerals.size() - 1).getUnit();
        MineralPatch farther = (MineralPatch) minerals.get(minerals.size() - 2).getUnit();
        if (b.getLocation().equals(new TilePosition(7, 118))) return new MutablePair<>(closer, farther);
        Area centerArea = bwem.getMap().getArea(new TilePosition(bw.getBWMap().mapWidth() / 2, bw.getBWMap().mapHeight() / 2));
        if (centerArea != null) {
            List<Mineral> centerMinerals = new ArrayList<>(centerArea.getMinerals());
            centerMinerals = centerMinerals.stream().sorted(Comparator.comparing(u -> u.getUnit().getDistance(b.getLocation().toPosition()))).collect(Collectors.toList());
            farther = (MineralPatch) centerMinerals.get(0).getUnit();
        }
        return new MutablePair<>(farther, closer);
    }

    void checkDisrupter() {
        if (enemyRace != Race.Zerg || disrupterBuilding == null) return;
        if (disrupterBuilding.getHitPoints() <= 30) {
            disrupterBuilding.cancelConstruction();
            disrupterBuilding = null;
        }
    }

    void cancelDyingThings() { // TODO test
        List<SCV> toRemove = new ArrayList<>();
        for (Entry<SCV, Building> b : workerTask.entrySet()) {
            if (b.getValue().isCompleted()) continue; // Is this even needed??
            if (b.getValue().isUnderAttack() && b.getValue().getHitPoints() <= 30) {
                b.getKey().haltConstruction();
                buildingLot.add(b.getValue());
                toRemove.add(b.getKey());
            }
        }
        for (SCV s : toRemove) {
            workerIdle.add(s);
            workerBuild.remove(s);
        }
        for (Building b : buildingLot) {
            if (b.isCompleted()) continue; // Is this even needed??
            if (b.isUnderAttack() && b.getHitPoints() <= 30) b.cancelConstruction();
        }
    }

    public boolean basicCombatUnitsDetected() {
        switch (enemyRace) {
            case Zerg:
                return IntelligenceAgency.enemyHasType(UnitType.Zerg_Zergling);
            case Terran:
                return IntelligenceAgency.enemyHasType(UnitType.Terran_Marine);
            case Protoss:
                return IntelligenceAgency.enemyHasType(UnitType.Protoss_Zealot) || IntelligenceAgency.enemyHasType(UnitType.Protoss_Dragoon);
        }
        return false;
    }

    public boolean basicCombatUnitsDetected(Set<UnitInfo> units) {
        switch (enemyRace) {
            case Zerg:
                return units.stream().anyMatch(u -> u.unitType == UnitType.Zerg_Zergling);
            case Terran:
                return units.stream().anyMatch(u -> u.unitType == UnitType.Terran_Marine);
            case Protoss:
                return units.stream().anyMatch(u -> u.unitType == UnitType.Protoss_Zealot || u.unitType == UnitType.Protoss_Dragoon);
        }
        return false;
    }

    void vespeneManager() {
        try {
            int workersAtGas = workerGas.keySet().size();
            int refineries = refineriesAssigned.size();
            if (refineries == 0) return;
            if (getCash().second >= 200) {
                int workersNeeded;
                if (getStrat().techToResearch.contains(TechType.Stim_Packs) && !getStrat().techToResearch.contains(TechType.Tank_Siege_Mode)) {
                    workersNeeded = refineries;
                    getStrat().workerGas = 1;
                } else {
                    workersNeeded = 2 * refineries;
                    getStrat().workerGas = 2;
                }
                if (workersAtGas > workersNeeded) {
                    Iterator<Entry<Worker, GasMiningFacility>> iterGas = workerGas.entrySet().iterator();
                    while (iterGas.hasNext()) {
                        Entry<Worker, GasMiningFacility> w = iterGas.next();
                        if (w.getKey().getOrder() == Order.HarvestGas) continue;
                        workerIdle.add(w.getKey());
                        if (w.getKey().isCarryingGas()) {
                            w.getKey().returnCargo();
                            w.getKey().stop(true);
                        } else w.getKey().stop(false);
                        refineriesAssigned.put(w.getValue(), refineriesAssigned.get(w.getValue()) - 1);
                        iterGas.remove();
                        workersAtGas--;
                        if (workersNeeded == workersAtGas) break;
                    }
                }
            } else if (getCash().second < 100 && getStrat().workerGas < 3 && workersAtGas / refineries == getStrat().workerGas)
                getStrat().workerGas++;
        } catch (Exception e) {
            System.err.println("vespeneManager exception");
            e.printStackTrace();
        }
    }

    public boolean isGoingToExpand() {
        return workerBuild.values().stream().anyMatch(u -> u.first == UnitType.Terran_Command_Center);
    }

    public void initMineralWalkPatches() {
        List<MineralPatch> mineralPatches = getGame().getMineralPatches();
        List<Unit> randomEggs = new ArrayList<>();
        for (Unit u : getGame().getAllUnits()) {
            if (u.getType() == UnitType.Zerg_Egg) randomEggs.add(u);
        }
        for (StaticBuilding u : bwem.getMap().getNeutralData().getStaticBuildings()) {
            if (u.getUnit() instanceof Assimilator) {
                mineralPatches.sort(Comparator.comparingInt((MineralPatch o) -> o.getDistance(u.getUnit())));
                walkingMinerals.add(mineralPatches.get(0));
                walkingMinerals.add(mineralPatches.get(1));
                randomEggs.sort(Comparator.comparingInt((Unit o) -> o.getDistance(u.getUnit())));
                unitStorage.onUnitShow(randomEggs.get(0));
                unitStorage.onUnitShow(randomEggs.get(1));
            }
        }
    }
}