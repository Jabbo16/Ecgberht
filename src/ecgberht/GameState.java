package ecgberht;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.iaie.btree.util.GameHandler;

import com.google.gson.Gson;

import bwapi.*;
import bwta.*;
import bwta.Region;
import ecgberht.Strategies.*;
import jfap.JFAP;
import jfap.JFAPUnit;
//import jppap.JPPAP;
//import jweb.JBWEB;
import ecgberht.BaseLocationComparator;
import ecgberht.Squad.Status;
import ecgberht.Agents.Vulture;

public class GameState extends GameHandler {

	public BaseLocation enemyBase = null;
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
	public Chokepoint closestChoke = null;
	public EnemyInfo EI = new EnemyInfo(game.enemy().getName());
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
	public List<BaseLocation> blockedBLs = new ArrayList<>();
	public List<BaseLocation> BLs = new ArrayList<BaseLocation>();
	public List<BaseLocation> EnemyBLs = new ArrayList<BaseLocation>();
	public List<Pair<Pair<Unit,Integer>,Boolean> > refineriesAssigned = new ArrayList<Pair<Pair<Unit,Integer>,Boolean> >();
	public List<Pair<Unit,Pair<UnitType,TilePosition>>> workerBuild = new ArrayList<Pair<Unit,Pair<UnitType,TilePosition>>>();
	public List<Pair<Unit,Position> > workerDefenders = new ArrayList<Pair<Unit,Position> >();
	public List<Pair<Unit,Unit> > repairerTask = new ArrayList<Pair<Unit,Unit> >();
	public List<Pair<Unit,Unit> > workerTask = new ArrayList<Pair<Unit,Unit>>();
	public long totalTime = 0;
	public Map<Position, Unit> blockingMinerals = new HashMap<>();
	public Map<Position,Unit> CCs = new HashMap<>();
	public Map<String,Squad> squads = new TreeMap<>();
	public Map<Unit, Set<Unit>> DBs = new TreeMap<>(new UnitComparator());
	public Map<Unit, String> TTMs = new TreeMap<>(new UnitComparator());
	public Map<Unit,EnemyBuilding> enemyBuildingMemory = new TreeMap<>(new UnitComparator());
	public Map<Unit,Integer> mineralsAssigned = new TreeMap<>(new UnitComparator());
	public Map<Unit,Unit> workerMining = new TreeMap<>(new UnitComparator());
	public Pair<Integer,Integer> deltaCash = new Pair<Integer,Integer>(0,0);
	public Pair<String, Unit> chosenMarine = null;
	public Position attackPosition = null;
	public Race enemyRace = Race.Unknown;
	public Region naturalRegion = null;
	public Set<BaseLocation> ScoutSLs = new HashSet<>();
	public Set<BaseLocation> SLs = new HashSet<>();
	public Set<String> teamNames = new TreeSet<>(Arrays.asList("Alpha","Bravo","Charlie","Delta","Echo","Foxtrot","Golf","Hotel","India","Juliet","Kilo","Lima","Mike","November","Oscar","Papa","Quebec","Romeo","Sierra","Tango","Uniform","Victor","Whiskey","X-Ray","Yankee","Zulu"));
	public Set<Unit> buildingLot = new TreeSet<>(new UnitComparator());
	public Set<Unit> CSs = new TreeSet<>(new UnitComparator());
	public Set<Unit> enemyCombatUnitMemory = new TreeSet<>(new UnitComparator());
	public Set<Unit> enemyInBase = new TreeSet<>(new UnitComparator());
	public Set<Unit> Fs = new TreeSet<>(new UnitComparator());
	public Set<Unit> MBs = new TreeSet<>(new UnitComparator());
	public Set<Unit> Ps = new TreeSet<>(new UnitComparator());
	public Set<Unit> SBs = new TreeSet<>(new UnitComparator());
	public Set<Unit> Ts = new TreeSet<>(new UnitComparator());
	public Set<Unit> UBs = new TreeSet<>(new UnitComparator());
	public Set<Unit> workerIdle = new TreeSet<>(new UnitComparator());
	public Set<Vulture> agents = new HashSet<>();
	public Strategy strat = new Strategy();
	public String chosenSquad = null;
	public TechType chosenResearch = null;
	public TilePosition checkScan = null;
	public TilePosition chosenBaseLocation = null;
	public TilePosition chosenPosition = null;
	public TilePosition initAttackPosition = null;
	public TilePosition initDefensePosition = null;
	public Unit chosenBuilderBL = null;
	public Unit chosenBuilding = null;
	public Unit chosenBuildingAddon = null;
	public Unit chosenBuildingLot = null;
	public Unit chosenBuildingRepair = null;
	public Unit chosenBunker = null;
	public Unit chosenHarasser = null;
	public Unit chosenRepairer = null;
	public Unit chosenScout = null;
	public Unit chosenUnitToHarass = null;
	public Unit chosenUnitUpgrader = null;
	public Unit chosenWorker = null;
	public Unit MainCC = null;
	public UnitType chosenAddon = null;
	public UnitType chosenToBuild = null;
	public UnitType chosenUnit = null;
	public UpgradeType chosenUpgrade = null;
	public boolean iReallyWantToExpand = false;

	public GameState(Mirror bwapi) {
		super(bwapi);
		map = new BuildingMap(game,self);
		map.initMap();
		testMap = map.clone();
		inMap = new InfluenceMap(game,self,game.mapHeight(), game.mapWidth());
		mapSize = BWTA.getStartLocations().size();
		simulator = new JFAP(bwapi.getGame());
	}

	public Strategy initStrat() {
		try {
			BioBuild b = new BioBuild();
			ProxyBBS bbs = new ProxyBBS();
			BioMechBuild bM = new BioMechBuild();
			BioBuildFE bFE = new BioBuildFE();
			BioMechBuildFE bMFE = new BioMechBuildFE();
			String map = game.mapFileName();
			if(enemyRace == Race.Zerg && EI.naughty) {
				return new Strategy(b);
			}
			if(EI.history.isEmpty()) {
				if(enemyRace == Race.Protoss) {
					double random = Math.random();
					if(random > 0.5 ) {
						return new Strategy(b);
					}
					else {
						return new Strategy(bM);
					}
				}

				if(mapSize == 2 && !map.contains("Heartbreak Ridge")) {
					double random = Math.random();
					if(random > 0.5 ) {
						return new Strategy(b);
					}
					else{
						return new Strategy(bM);
					}
				}
				if(map.contains("HeartbreakRidge")) {
					double random = Math.random();
					if(random > 0.75 ) {
						return new Strategy(bbs);
					}
					else {
						return new Strategy(b);
					}

				}
				else {
					double random = Math.random();
					if(random > 0.5 ) {
						return new Strategy(b);
					}
					else {
						return new Strategy(bM);
					}
				}
			} else {
				Map<String,Pair<Integer,Integer>> strategies = new TreeMap<>();
				Map<String,AStrategy> nameStrat = new TreeMap<>();

				strategies.put(bbs.name, new Pair<Integer,Integer>(0,0));
				nameStrat.put(bbs.name, bbs);

				strategies.put(bFE.name, new Pair<Integer,Integer>(0,0));
				nameStrat.put(bFE.name, bFE);

				strategies.put(bMFE.name, new Pair<Integer,Integer>(0,0));
				nameStrat.put(bMFE.name, bMFE);

				strategies.put(b.name, new Pair<Integer,Integer>(0,0));
				nameStrat.put(b.name, b);

				strategies.put(bM.name, new Pair<Integer,Integer>(0,0));
				nameStrat.put(bM.name, bM);

				for(StrategyOpponentHistory r: EI.history) {
					if(strategies.containsKey(r.strategyName)) {
						strategies.get(r.strategyName).first += r.wins;
						strategies.get(r.strategyName).second += r.losses;
					}
				}

				int totalGamesPlayed = EI.wins + EI.losses;
				int DefaultStrategyWins = strategies.get(b.name).first;
				int DefaultStrategyLosses = strategies.get(b.name).second;
			    int strategyGamesPlayed = DefaultStrategyWins + DefaultStrategyLosses;
			    double winRate = strategyGamesPlayed > 0 ? DefaultStrategyWins / (double)(strategyGamesPlayed) : 0;
			    if (strategyGamesPlayed < 2)
			    {
			    	game.sendText("I dont know you that well yet, lets pick the standard strategy");
			        return new Strategy(b);
			    }
			    if(strategyGamesPlayed > 0 && winRate > 0.74) {
			    	game.sendText("Using default Strategy with winrate " + winRate*100 + "%");
			        return new Strategy(b);
			    }
			    double C = 0.5;
			    String bestUCBStrategy = null;
			    double bestUCBStrategyVal = Double.MIN_VALUE;
			    for (String strat : strategies.keySet()) {
			    	if(map.contains("HeartbreakRidge") && (strat == "BioMechFE" || strat == "BioMech" || strat == "FullMech")) {
			    		continue;
			    	}
			        int sGamesPlayed = strategies.get(strat).first + strategies.get(strat).second;
			        double sWinRate = sGamesPlayed > 0 ? (double)(strategies.get(strat).first / (double)(strategyGamesPlayed)) : 0;
			        double ucbVal = sGamesPlayed == 0 ?  C : C * Math.sqrt(Math.log((double)(totalGamesPlayed / sGamesPlayed)));
			        double val = sWinRate + ucbVal;
			        if (val >= bestUCBStrategyVal) {
			            bestUCBStrategy = strat;
			            bestUCBStrategyVal = val;
			        }
			    }
			    game.sendText("Chose: " + bestUCBStrategy + " with UCB: " + bestUCBStrategyVal);
			    return new Strategy(nameStrat.get(bestUCBStrategy));
			}
		} catch(Exception e) {
			System.err.println("Error initStrat, loading default Strat");
			System.err.println(e);
			BioBuild b = new BioBuild();
			return new Strategy(b);

		}

	}

	public void initEnemyRace() {
		if(game.enemy().getRace() != Race.Unknown) {
			enemyRace = game.enemy().getRace();
			enemyIsRandom = false;
		}
	}

	public void initBlockingMinerals() {
		for(Unit u : game.getStaticMinerals()) {
			if(u.getResources() == 0)
				blockingMinerals.put(u.getPosition(), u);
		}
	}

	public void checkBasesWithBLockingMinerals() {
		if(blockingMinerals.isEmpty()) {
			return;
		}
		for(BaseLocation b : BLs) {
			if(b.isStartLocation()) {
				continue;
			}
			for(Chokepoint c : b.getRegion().getChokepoints()) {
				for(Position m : blockingMinerals.keySet()) {
					if(broodWarDistance(m,c.getCenter()) < 40){
						blockedBLs.add(b);
						break;
					}
				}
			}
		}
	}

	public void playSound(String soundFile) {
		try{
			String run = getClass().getResource("GameState.class").toString();
			if(run.startsWith("jar:") || run.startsWith("rsrc:")) {
				InputStream fis = getClass().getClassLoader().getResourceAsStream(soundFile);
				javazoom.jl.player.Player playMP3 = new javazoom.jl.player.Player(fis);
				new Thread(() -> {
		        	 try {
						playMP3.play();
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}).start();
			}
			else {
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

		}
		catch(Exception e) {
			System.err.println("playSound");
			System.err.println(e);
		}
	}

	public Game getGame(){
		return game;
	}

	public Player getPlayer(){
		return self;
	}

	public void initCCs(){
		List<Unit> units = self.getUnits();
		for(Unit u:units) {
			if(u.getType() == UnitType.Terran_Command_Center) {
				CCs.put(BWTA.getRegion(u.getPosition()).getCenter(),u);
			}
		}
	}

	public void addNewResources(Unit unit) {
		List<Unit> minerals = BWTA.getNearestBaseLocation(unit.getTilePosition()).getStaticMinerals();
		List<Unit> gas = BWTA.getNearestBaseLocation(unit.getTilePosition()).getGeysers();
		List<Pair<Pair<Unit,Integer>,Boolean> > auxGas = new ArrayList<Pair<Pair<Unit,Integer>,Boolean> >();
		for(Unit m : minerals) {
			mineralsAssigned.put(m, 0);
		}
		for(Unit m:gas) {
			Pair<Pair<Unit,Integer>,Boolean> geyser = new Pair<Pair<Unit,Integer>,Boolean>(new Pair<Unit,Integer>(m,0),false);
			refineriesAssigned.add(geyser);
		}
		refineriesAssigned.addAll(auxGas);
		if(strat.name == "ProxyBBS") {
			workerCountToSustain = (int) mineralGatherRateNeeded(Arrays.asList(UnitType.Terran_Marine, UnitType.Terran_Marine));
		}
	}

	public void removeResources(Unit unit) {
		List<Unit> minerals = BWTA.getNearestBaseLocation(unit.getTilePosition()).getStaticMinerals();
		List<Unit> gas = BWTA.getNearestBaseLocation(unit.getTilePosition()).getGeysers();
		List<Pair<Pair<Unit,Integer>,Boolean> > auxGas = new ArrayList<Pair<Pair<Unit,Integer>,Boolean> >();
		for(Unit m : minerals) {
			if(mineralsAssigned.containsKey(m)) {
				List<Pair<Unit,Unit> > aux = new ArrayList<Pair<Unit,Unit> >();
				for(Pair<Unit,Unit> w: workerTask) {
					if(m.equals(w.second)) {
						aux.add(w);
						workerIdle.add(w.first);
					}
				}
				workerTask.removeAll(aux);
				mineralsAssigned.remove(m);
			}

		}
		for(Unit m : gas) {
			Pair<Pair<Unit,Integer>,Boolean> geyser = new Pair<Pair<Unit,Integer>,Boolean>(new Pair<Unit,Integer>(m,0),false);
			refineriesAssigned.add(geyser);
		}
		for(Pair<Pair<Unit,Integer>,Boolean> pm : refineriesAssigned) {
			for(Unit m : gas) {
				if(pm.first.first.equals(m)) {
					List<Pair<Unit,Unit> > aux = new ArrayList<Pair<Unit,Unit> >();
					for(Pair<Unit,Unit> w: workerTask) {
						if(pm.first.first.equals(w.second)) {
							aux.add(w);
							workerIdle.add(w.first);
						}
					}
					workerTask.removeAll(aux);
					auxGas.add(pm);
				}
			}
		}
		refineriesAssigned.removeAll(auxGas);
		if(strat.name == "ProxyBBS") {
			workerCountToSustain = (int) mineralGatherRateNeeded(Arrays.asList(UnitType.Terran_Marine, UnitType.Terran_Marine));
		}
	}

	public Pair<Integer,Integer> getCash(){
		return new Pair<Integer,Integer>(self.minerals(),self.gas());
	}

	public int getSupply(){
		return (self.supplyTotal()-self.supplyUsed());
	}

	public void printer() {
		Integer counter = 0;
		for(BaseLocation b : BLs) {
			game.drawTextMap(b.getPosition(), counter.toString());
			counter++;
		}
		int apm = game.getAPM(false);
		if(apm > 9000 && !firstAPM ) {
			game.sendText("My APM is over 9000!");
			firstAPM = true;
		}
		for(Vulture vulture : agents) {
			game.drawTextMap(vulture.unit.getPosition(), vulture.statusToString());
		}

		game.drawTextScreen(10, 80, Utils.formatText("Strategy: ",Utils.White) + Utils.formatText(strat.name, Utils.White));
		game.drawTextScreen(10, 50, Utils.formatText("APM: ",Utils.White) + Utils.formatText(String.valueOf(apm), Utils.White));
		if(closestChoke != null) {
			game.drawTextMap(closestChoke.getCenter(), "Choke");
		}

		if(chosenBuilderBL != null) {
			game.drawTextMap(chosenBuilderBL.getPosition(), "BuilderBL");
			print(chosenBuilderBL,Color.Blue);
		}
		if(chosenHarasser != null) {
			game.drawTextMap(chosenHarasser.getPosition(), "Harasser");
			print(chosenHarasser,Color.Blue);
		}
		if (chosenBaseLocation != null) {
			print(chosenBaseLocation, UnitType.Terran_Command_Center,Color.Cyan);
		}
		for(Pair<Unit,Pair<UnitType,TilePosition> > u : workerBuild) {
			game.drawTextMap(u.first.getPosition(), "ChosenBuilder");
			print(u.second.second,u.second.first,Color.Teal);
		}
		if(chosenUnitToHarass != null) {
			print(chosenUnitToHarass,Color.Red);
			game.drawTextMap(chosenUnitToHarass.getPosition(), "UnitToHarass");
		}
		for(Pair<Unit,Unit> r : repairerTask) {
			print(r.first,Color.Yellow);
			game.drawTextMap(r.first.getPosition(), "Repairer");
		}
		game.drawTextScreen(10, 5, Utils.formatText(self.getName(), Utils.Green) + Utils.formatText(" vs ", Utils.Yellow) + Utils.formatText(game.enemy().getName(), Utils.Red));
		if (chosenScout != null) {
			game.drawTextMap(chosenScout.getPosition(), "Scouter");
			print(chosenScout,Color.Purple);
			game.drawTextScreen(10, 20, Utils.formatText("Scouting: ",Utils.White) + Utils.formatText("True", Utils.Green));
		}
		else {
			game.drawTextScreen(10, 20, Utils.formatText("Scouting: ",Utils.White) + Utils.formatText("False", Utils.Red));
		}
		if(enemyBase != null) {
			game.drawTextScreen(10, 35, Utils.formatText("Enemy Base Found: ",Utils.White) + Utils.formatText("True", Utils.Green));
		}
		else {
			game.drawTextScreen(10, 35, Utils.formatText("Enemy Base Found: ",Utils.White) + Utils.formatText("False", Utils.Red));
		}
//		if (chosenWorker != null) {
//			game.drawTextMap(chosenWorker.getPosition(), "ChosenWorker");
//		}
		if (chosenRepairer != null) {
			game.drawTextMap(chosenRepairer.getPosition(), "ChosenRepairer");
		}
//		if(enemyCombatUnitMemory.size()>0) {
//			for(Unit u : enemyCombatUnitMemory) {
//				game.drawTextMap(u.getPosition(), u.getType().toString());
//				print(u,Color.Red);
//			}
//		}
		List <Region> regions = BWTA.getRegions();
		for(Region reg: regions) {
			List <Chokepoint> ch = reg.getChokepoints();
			for(Chokepoint c : ch) {
				Pair <Position,Position> lados = c.getSides();
				game.drawLineMap(lados.first, lados.second, Color.Green);
			}
		}
		for(Unit u: CCs.values()) {
			print(u,Color.Yellow);
			game.drawCircleMap(u.getPosition(), 500, Color.Orange);
		}
		for(Unit u : DBs.keySet()) {
			game.drawCircleMap(u.getPosition(), 300, Color.Orange);
		}
		for(Unit u: workerIdle) {
			print(u,Color.Green);
		}
		for(Pair<Unit, Position> u: workerDefenders) {
			print(u.first,Color.Purple);
			game.drawTextMap(u.first.getPosition(), "Spartan");
		}
		for(Entry<String, Squad> s : squads.entrySet()) {
			Position centro = getSquadCenter(s.getValue());
			game.drawCircleMap(centro, 80, Color.Green);
			game.drawTextMap(centro,s.getKey());
		}
		if(enemyRace == Race.Zerg && EI.naughty) {
			game.drawTextScreen(10, 95, Utils.formatText("Naughty Zerg: ",Utils.White) + Utils.formatText("yes", Utils.Green));
		}
		for(Unit m : mineralsAssigned.keySet()) {
			print(m, Color.Cyan);
			game.drawTextMap(m.getPosition(), mineralsAssigned.get(m).toString());
		}
	}

	public void print(Unit u,Color color) {
		game.drawBoxMap(u.getLeft(),u.getTop(),u.getRight(),u.getBottom(),color);
	}

	public void print(TilePosition u,UnitType type, Color color) {
		Position leftTop = new Position(u.getX() * TilePosition.SIZE_IN_PIXELS, u.getY() * TilePosition.SIZE_IN_PIXELS);
		Position rightBottom = new Position(leftTop.getX() + type.tileWidth() * TilePosition.SIZE_IN_PIXELS, leftTop.getY() + type.tileHeight() * TilePosition.SIZE_IN_PIXELS);
		game.drawBoxMap(leftTop,rightBottom,color);
	}

	public void print(TilePosition u, Color color) {
		Position leftTop = new Position(u.getX() * TilePosition.SIZE_IN_PIXELS, u.getY() * TilePosition.SIZE_IN_PIXELS);
		Position rightBottom = new Position(leftTop.getX() + TilePosition.SIZE_IN_PIXELS, leftTop.getY() + TilePosition.SIZE_IN_PIXELS);
		game.drawBoxMap(leftTop,rightBottom,color);
	}

	public String convertSeconds(int seconds){
		int h = seconds/ 3600;
		int m = (seconds % 3600) / 60;
		int s = seconds % 60;
		String sh = (h > 0 ? String.valueOf(h) + " " + "h" : "");
		String sm = (m < 10 && m > 0 && h > 0 ? "0" : "") + (m > 0 ? (h > 0 && s == 0 ? String.valueOf(m) : String.valueOf(m) + " " + "min") : "");
		String ss = (s == 0 && (h > 0 || m > 0) ? "" : (s < 10 && (h > 0 || m > 0) ? "0" : "") + String.valueOf(s) + " " + "sec");
		return sh + (h > 0 ? " " : "") + sm + (m > 0 ? " " : "") + ss;
	}

	public void initStartLocations() {
		BaseLocation startBot = BWTA.getStartLocation(getPlayer());
		for (BaseLocation b : BWTA.getBaseLocations()) {
			if (b.isStartLocation() && !b.getTilePosition().equals(startBot.getTilePosition())) {
				SLs.add(b);
				ScoutSLs.add(b);
			}
		}
	}

	public void initBaseLocations() {
		BLs.addAll(BWTA.getBaseLocations());
		Collections.sort(BLs, new BaseLocationComparator(false));

	}

	public void moveUnitFromChokeWhenExpand(){
		try {
			if(!squads.isEmpty() && chosenBaseLocation != null) {
				Region chosenRegion = BWTA.getRegion(chosenBaseLocation);
				if(chosenRegion != null) {
					if(chosenRegion.getCenter().equals(naturalRegion.getCenter())) {
						TilePosition mapCenter = new TilePosition(game.mapWidth(), game.mapHeight());
						List<Chokepoint> cs = chosenRegion.getChokepoints();
						Chokepoint closestChoke = null;
						for(Chokepoint c : cs) {
							if(!c.getCenter().toTilePosition().equals(this.closestChoke.getCenter().toTilePosition())) {
								double aux = broodWarDistance(c.getCenter(),chosenBaseLocation.toPosition());
								if(aux > 0.0) {
									if(closestChoke == null ||  aux < broodWarDistance(closestChoke.getCenter(),mapCenter.toPosition())) {
										closestChoke = c;
									}
								}
							}
						}
						if(closestChoke != null) {
							for(Squad s : squads.values()) {
								if(s.status == Status.IDLE) {
									s.giveAttackOrder(closestChoke.getCenter());
									s.status = Status.ATTACK;
								}
							}
						}
					}
				}
			}
		} catch(Exception e) {
			System.err.println("MoveUnitFromChokeWhenExpand");
			System.err.println(e);
		}
	}

	public void fix() {
		if(defense && enemyInBase.isEmpty()) {
			defense = false;
		}
		List<String> squadsToClean = new ArrayList<>();
		for(Squad s : squads.values()) {
			List<Unit> aux = new ArrayList<>();
			for(Unit u : s.members) {
				if(!u.exists()) {
					aux.add(u);
				}
			}

			if(s.members.isEmpty() || aux.size() == s.members.size()) {
				squadsToClean.add(s.name);
				continue;
			}
			else {
				s.members.removeAll(aux);
			}
		}
		List<Unit> bunkers =  new ArrayList<>();
		for(Entry<Unit, Set<Unit>> u : DBs.entrySet()) {
			if(u.getKey().exists()) continue;
			for(Unit m : u.getValue()) {
				if(m.exists()) addToSquad(m);
			}
			bunkers.add(u.getKey());
		}
		for(Unit c : bunkers) DBs.remove(c);

		for(String name : squadsToClean) {
			squads.remove(name);
		}
		if(chosenScout != null && chosenScout.isIdle()) {
			workerIdle.add(chosenScout);
			chosenScout = null;
		}
		if(chosenBuilderBL!= null && (chosenBuilderBL.isIdle() || chosenBuilderBL.isGatheringGas() || chosenBuilderBL.isGatheringMinerals())) {
			workerIdle.add(chosenBuilderBL);
			chosenBuilderBL = null;
			movingToExpand = false;
			expanding = false;
			chosenBaseLocation = null;
		}
		if(chosenBuilderBL!= null && workerIdle.contains(chosenBuilderBL)) {
			workerIdle.remove(chosenBuilderBL);
		}

		List<Pair<Unit,Pair<UnitType,TilePosition>>> aux3 = new ArrayList<Pair<Unit,Pair<UnitType,TilePosition>>>();
		for(Pair<Unit,Pair<UnitType,TilePosition> > u : workerBuild) {
			if((u.first.isIdle() || u.first.isGatheringGas() || u.first.isGatheringMinerals()) &&
					broodWarDistance(u.first.getPosition(), u.second.second.toPosition()) > 100) {
				aux3.add(u);
				deltaCash.first -= u.second.first.mineralPrice();
				deltaCash.second -= u.second.first.gasPrice();
				workerIdle.add(u.first);
			}
		}
		workerBuild.removeAll(aux3);

		List<Pair<Unit,Unit> > aux4 = new ArrayList<Pair<Unit,Unit> >();
		for(Pair<Unit,Unit> r : repairerTask) {
			if(r.first.equals(chosenScout)) {
				chosenScout = null;
			}
			if(!r.first.isRepairing() || r.first.isIdle()) {
				if(chosenRepairer != null) {
					if(r.first.equals(chosenRepairer)) {
						chosenRepairer = null;
					}
				}
				workerIdle.add(r.first);
				aux4.add(r);
			}
		}
		repairerTask.removeAll(aux4);

		List<Pair<Unit,Position> > aux5 = new ArrayList<Pair<Unit,Position> >();
		for(Pair<Unit,Position> r : workerDefenders) {
			if(r.first.isIdle() || r.first.isGatheringMinerals()) {
				workerIdle.add(r.first);
				aux5.add(r);
			}
		}
		workerDefenders.removeAll(aux5);

		List<String> aux6 = new ArrayList<>();
		for(Squad u : squads.values()) {
			if(u.members.isEmpty()) {
				aux6.add(u.name);
			}
		}
		for(String s : aux6) {
			squads.remove(s);
		}
	}

	public void checkMainEnemyBase() {
		if(enemyBuildingMemory.isEmpty() && ScoutSLs.isEmpty()) {
			enemyBase = null;
			chosenScout = null;
			ScoutSLs.clear();
			for(BaseLocation b : BLs) {
				if(!CCs.containsKey(b.getRegion().getCenter()) && BWTA.isConnected(self.getStartLocation(), b.getTilePosition())) {
					ScoutSLs.add(b);
				}
			}
		}
	}

	public void checkEnemyAttackingWT() {
		if(!workerTask.isEmpty()) {
			List<Pair<Unit,Unit> > aux = new ArrayList<Pair<Unit,Unit> >();
			for(Pair<Unit,Unit> p : workerTask) {
				if(p.second.getType().isBuilding() && !p.second.getType().isNeutral() && p.second.isBeingConstructed()) {
					if((p.first.isUnderAttack()) && (p.second.getType() != UnitType.Terran_Bunker && p.second.getType() != UnitType.Terran_Missile_Turret)) {
						p.first.haltConstruction();
						workerIdle.add(p.first);
						buildingLot.add(p.second);
						aux.add(p);
					}
				}
			}
			workerTask.removeAll(aux);
		}
	}

	public void initClosestChoke() {
		List<BaseLocation> aux = BLs;
//		aux.removeAll(blockedBLs);
		Region naturalArea = aux.get(1).getRegion();
		naturalRegion = naturalArea;
		double distBest = Double.MAX_VALUE;
		for (Chokepoint choke : naturalArea.getChokepoints())
		{
			double dist = BWTA.getGroundDistance(choke.getCenter().toTilePosition(), game.self().getStartLocation());
			if (dist < distBest && dist > 0.0)
				closestChoke = choke; distBest = dist;
		}
		if(closestChoke != null) {
			initAttackPosition = closestChoke.getCenter().toTilePosition();
			initDefensePosition = closestChoke.getCenter().toTilePosition();
		}
		else {
			initAttackPosition = self.getStartLocation();
			initDefensePosition = self.getStartLocation();
		}
	}

	public void checkUnitsBL(TilePosition BL, Unit chosen) {
		UnitType type = UnitType.Terran_Command_Center;
		Position topLeft = new Position(BL.getX() * TilePosition.SIZE_IN_PIXELS, BL.getY() * TilePosition.SIZE_IN_PIXELS);
		Position bottomRight = new Position(topLeft.getX() + type.tileWidth() * TilePosition.SIZE_IN_PIXELS, topLeft.getY() + type.tileHeight() * TilePosition.SIZE_IN_PIXELS);
		List<Unit> blockers = game.getUnitsInRectangle(topLeft, bottomRight);
		if(!blockers.isEmpty()) {
			for(Unit u : blockers) {
				if(u.getPlayer().getID() == self.getID() && !u.equals(chosen) && !u.getType().isWorker()) {
					u.move(BWTA.getNearestChokepoint(BL).getCenter());
				}
			}
		}
	}

	public String getSquadName() {
		if(teamNames.size() == squads.size()) {
			String gg = null;
			while(gg == null || squads.containsKey(gg)) {
				gg = "RandomSquad" + new Random().toString();
			}
			return gg;
		}
		String nombre = null;
		while(nombre == null || squads.containsKey(nombre)) {
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
		if(squads.size() == 0) {
			Squad aux = new Squad(getSquadName());
			aux.addToSquad(unit);
			squads.put(aux.name, aux);
			nombre = aux.name;
		}
		else {
			String chosen = null;
			for(Entry<String, Squad> s : squads.entrySet()) {
				if(s.getValue().members.size() < 12 && broodWarDistance(getSquadCenter(s.getValue()), unit.getPosition()) <  1000 && (chosen == null || broodWarDistance(unit.getPosition(),getSquadCenter(s.getValue())) < broodWarDistance(unit.getPosition(), getSquadCenter(squads.get(chosen))))) {
					chosen = s.getKey();
				}
			}
			if(chosen != null) {
				squads.get(chosen).addToSquad(unit);
				nombre = chosen;
			}
			else {
				Squad nuevo = new Squad(getSquadName());
				nuevo.addToSquad(unit);
				squads.put(nuevo.name, nuevo);
				nombre = nuevo.name;
			}
		}
		return nombre;
	}

	public Position getSquadCenter(Squad s) {
		Position point = new Position(0,0);
		for(Unit u : s.members) {
			if(s.members.size() == 1) {
				return u.getPosition();
			}
			point = new Position(point.getX() + u.getPosition().getX(), point.getY() + u.getPosition().getY());

		}
		return new Position(point.getX()/s.members.size(), point.getY()/s.members.size());

	}

	public void removeFromSquad(Unit unit) {
		for(Entry<String, Squad> s : squads.entrySet()) {
			if(s.getValue().members.contains(unit)) {
				if(s.getValue().members.size() == 1) {
					squads.remove(s.getKey());
				}
				else {
					s.getValue().members.remove(unit);
				}
				break;
			}
		}
	}

	public int getArmySize() {
		int count = 0;
		if(squads.isEmpty()) {
			return count;
		}
		else {
			for(Entry<String,Squad> s : squads.entrySet()) {
				count += s.getValue().members.size();
			}
		}
		return count + agents.size();
	}

	public void siegeTanks() {
		if(!squads.isEmpty()) {
			Set<Unit> tanks = new TreeSet<Unit>(new UnitComparator());
			for (Entry<String,Squad> s : squads.entrySet()) {
				tanks.addAll(s.getValue().getTanks());
			}
			if(!tanks.isEmpty()) {
				for(Unit t : tanks) {
					//List<Unit> unitsInRange = t.getUnitsInRadius(UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange());
					boolean far = false;
					boolean close = false;
					for(Unit e : enemyCombatUnitMemory) {
						double distance = broodWarDistance(e.getPosition(), t.getPosition());
						if(distance > UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange()) {
							continue;
						}
						if(distance <= UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().minRange()) {
							close = true;
							break;
						}
						if(e.getPlayer().isEnemy(self) && !e.getType().isWorker() && !e.getType().isFlyer() && (e.getType().canAttack() || e.getType() == UnitType.Terran_Bunker)) {
							far = true;
							break;
						}
					}
					if(close && !far) {
						if(t.getType() == UnitType.Terran_Siege_Tank_Siege_Mode && t.getOrder() != Order.Unsieging) t.unsiege();
						continue;
					}
					if(far) {
						if(t.getType() == UnitType.Terran_Siege_Tank_Tank_Mode && t.getOrder() != Order.Sieging) {
							t.siege();
						}
						continue;
					}
					if(t.getType() == UnitType.Terran_Siege_Tank_Siege_Mode && t.getOrder() != Order.Unsieging) t.unsiege();
				}
			}
		}
	}

	public boolean checkSupply() {
		for(Pair<Unit,Pair<UnitType,TilePosition> > w : workerBuild) {
			if(w.second.first == UnitType.Terran_Supply_Depot) {
				return true;
			}
		}
		for(Pair<Unit,Unit> w : workerTask) {
			if(w.second.getType() == UnitType.Terran_Supply_Depot) {
				return true;
			}
		}
		return false;
	}

	public int getCombatUnitsBuildings() {
		int count = 0;
		count = MBs.size() + Fs.size();
		if(count == 0) {
			return 1;
		}
		return count;
	}

	public double getMineralRate() {
		double rate = 0.0;
		if(frameCount > 0) {
			rate = ((double)self.gatheredMinerals()-50)/frameCount;
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
		double distance = BWTA.getGroundDistance(start, end);
		//double top = u.getType().topSpeed();
		//double aceleration = u.getType().acceleration();
		double frames = distance/2.55;
		int mineralsWhenReach = (int) (rate*frames);
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
		for(Entry<Unit, Unit> u : workerMining.entrySet()) {
			if(u.getKey().getTarget() != null) {
				if(!u.getKey().getTarget().equals(u.getValue()) && u.getKey().getOrder() == Order.MoveToMinerals && !u.getKey().isCarryingMinerals()){
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
			if( distance_aux > 0.0 && (chosen == null ||  distance_aux <  distance)) {
				chosen = u;
				distance = distance_aux;
			}
		}
		if(chosen != null) {
			return chosen.getPosition();
		}
		return null;
	}

	public void readOpponentInfo(){
		String name = game.enemy().getName();
		String path = "bwapi-data/read/" + name + ".json";
		try {
			if(Files.exists(Paths.get(path))) {
				EI = enemyInfoJSON.fromJson(new FileReader(path), EnemyInfo.class);
				return;
			}
			path = "bwapi-data/write/" + name + ".json";
			if(Files.exists(Paths.get(path))) {
				EI = enemyInfoJSON.fromJson(new FileReader(path), EnemyInfo.class);
				return;
			}
			path = "bwapi-data/AI/" + name + ".json";
			if(Files.exists(Paths.get(path))) {
				EI = enemyInfoJSON.fromJson(new FileReader(path), EnemyInfo.class);
				return;
			}
		} catch(Exception e) {
			System.err.println("readOpponentInfo");
			System.err.println(e);
		}
	}

	public void writeOpponentInfo(String name) {
		String dir = "bwapi-data/write/";
		String path = dir + name + ".json";
		game.sendText("Writing result to: " + path);
		Gson aux = new Gson();
		if(enemyIsRandom && EI.naughty) {
			EI.naughty = false;
		}
		String print = aux.toJson(EI);
		File directory = new File(dir);
	    if (! directory.exists()){
	        directory.mkdir();
	    }
		try(PrintWriter out = new PrintWriter(path)){
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
			while(chosen == null) {
				List<TilePosition> sides = new ArrayList<TilePosition>();
				if(rax.getY() - bunker.tileHeight() - dist >= 0) {
					TilePosition up = new TilePosition(rax.getX() , rax.getY() - bunker.tileHeight() - dist);
					sides.add(up);
				}
				if(rax.getY() + UnitType.Terran_Barracks.tileHeight() + dist < game.mapHeight()) {
					TilePosition down = new TilePosition(rax.getX() , rax.getY() + UnitType.Terran_Barracks.tileHeight() + dist);
					sides.add(down);
				}
				if(rax.getX() - bunker.tileWidth() - dist >= 0) {
					TilePosition left = new TilePosition(rax.getX() - bunker.tileWidth() - dist, rax.getY());
					sides.add(left);
				}
				if(rax.getX() + UnitType.Terran_Barracks.tileWidth() + dist < game.mapWidth()) {
					TilePosition right = new TilePosition(rax.getX() + UnitType.Terran_Barracks.tileWidth() + dist, rax.getY());
					sides.add(right);
				}
				for(TilePosition tile : sides) {
					if((chosen == null && game.canBuildHere(tile, bunker) ) || (closestChoke.getCenter().toTilePosition().getDistance(tile) < closestChoke.getCenter().toTilePosition().getDistance(chosen) && game.canBuildHere(tile, bunker))) {
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
		for(EnemyBuilding u : enemyBuildingMemory.values()) {
			if(game.isVisible(u.pos)) {
				if(!game.getUnitsOnTile(u.pos).contains(u.unit)){
					aux.add(u.unit);
				}
				else if(u.unit.isVisible()) {
					u.pos = u.unit.getTilePosition();
				}
				u.type = u.unit.getType();
			}

		}
		for(Unit u : aux) {
			enemyBuildingMemory.remove(u);
		}

	}

	public void mergeSquads() {
		try {
			if(squads.isEmpty()) {
				return;
			}
			if(squads.size() < 2) {
				return;
			}
			for(Squad u1 : squads.values()) {
				int u1_size = u1.members.size();
				if(u1_size < 12) {
					for(Squad u2 : squads.values()) {
						if(u2.name.equals(u1.name) || u2.members.size() > 11){
							continue;
						}
						if(broodWarDistance(getSquadCenter(u1), getSquadCenter(u2)) < 200) {
							if(u1_size + u2.members.size() > 12) {
								continue;
							}
							else {
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
			for(Squad u : squads.values()) {
				if(u.members.isEmpty()) {
					aux.add(u);
				}
			}
			squads.values().removeAll(aux);
		} catch(Exception e) {
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
		for(Pair<Unit,Pair<UnitType,TilePosition> > w: workerBuild) {
			if(w.second.first == type) {
				count++;
			}
		}

		count += self.allUnitCount(type);
		return count;
	}

	/** Thanks to Yegers for the method
	 * @author Yegers
	 * Number of workers needed to sustain a number of units.
	 * This method assumes that the required buildings are available.
	 * Example usage: to sustain building 2 marines at the same time from 2 barracks.
	 * @param units List of units that are to be sustained.
	 * @return Number of workers required.
	 */
	public double mineralGatherRateNeeded(final List<UnitType> units) {
		double mineralsRequired = 0.0;
		double m2f = (4.53/100.0)/65.0;
		double SaturationX2_Slope = -1.5;
		double SaturationX1 = m2f * 65.0;
		double SaturationX2_B = m2f * 77.5 ;
		for (final UnitType unit : units) {
			mineralsRequired += (((double) unit.mineralPrice()) / unit.buildTime()) / 1.0;
		}
		double workersRequired = mineralsRequired / SaturationX1;
		if (workersRequired > mineralsAssigned.size()) {
			return  Math.ceil((mineralsRequired - SaturationX2_B / 1.0) / SaturationX2_Slope);
		}
		return Math.ceil(workersRequired);
	}

	public void checkWorkerMilitia() {
		if(countUnit(UnitType.Terran_Barracks) == 2) {
			List<Unit> aux = new ArrayList<>();
			int count = workerMining.size();
			for(Entry<Unit, Unit> scv : workerMining.entrySet()) {
				if(count <= workerCountToSustain) {
					break;
				}
				if(!scv.getKey().isCarryingMinerals()) {
					scv.getKey().move(new TilePosition(game.mapWidth()/2, game.mapHeight()/2).toPosition());
					addToSquad(scv.getKey());
						if(mineralsAssigned.containsKey(scv.getValue())) {
							mining--;
							mineralsAssigned.put(scv.getValue(), mineralsAssigned.get(scv.getValue()) - 1);
						}
					aux.add(scv.getKey());
					count--;

				}
			}
			for(Unit u : aux) {
				workerMining.remove(u);
			}
		}

	}

	public boolean armyGroupedBBS() {
		boolean allFine = true;
		for(Squad s : squads.values()) {
			if(s.attack != Position.None) {
				if(s.members.size() == 1) {
					continue;
				}
				List<Unit> circle = game.getUnitsInRadius(getSquadCenter(s), 190);
				Set<Unit> different = new HashSet<>();
				different.addAll(circle);
				different.addAll(s.members);
				circle.retainAll(s.members);
				different.removeAll(circle);
				if(circle.size() != s.members.size()) {
					allFine = false;
					for(Unit u : different) {
						u.attack(getSquadCenter(s));
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
		double d   = Math.min(dx, dy);
		double D   = Math.max(dx, dy);
		if (d < D / 4) {
			return D;
		}
		return D - D / 16 + d * 3 / 8 - D / 64 + d * 3 / 256;

	}

	public Pair<Boolean, Boolean> simulateDefenseBattle(Set<Unit> friends, Set<Unit> enemies, int frames, boolean bunker) {
		simulator.clear();
		Pair<Boolean,Boolean> result = new Pair<>(true,false);
		for(Unit u : friends) {
			simulator.addUnitPlayer1(new JFAPUnit(u));
		}
		for(Unit u : enemies) {
			simulator.addUnitPlayer2(new JFAPUnit(u));
		}
		Pair<Integer, Integer> presim_scores = simulator.playerScores();
//		int presim_my_unit_count = simulator.getState().first.size();
//		int presim_enemy_unit_count = simulator.getState().second.size();
		simulator.simulate(frames);
//		int postsim_my_unit_count = simulator.getState().first.size();
//		int postsim_enemy_unit_count = simulator.getState().second.size();
		Pair<Integer, Integer> postsim_scores = simulator.playerScores();
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
		if(enemy_score_diff * 2 < my_score_diff) {
			result.first = false;
		}
		if(bunker){
			boolean bunkerDead = true;
			for (JFAPUnit unit : simulator.getState().first){
				if (unit.unitType == UnitType.Terran_Bunker){
					bunkerDead = false;
					break;
				}
			}
			if(bunkerDead) {
				result.second = true;
			}
		}

		return result;
	}

	public boolean simulateHarass(Unit harasser, List<Unit> enemies, int frames) {
		simulator.clear();
		simulator.addUnitPlayer1(new JFAPUnit(harasser));
		for(Unit u : enemies) {
			simulator.addUnitPlayer2(new JFAPUnit(u));
		}
		int preSimFriendlyUnitCount = simulator.getState().first.size();
		simulator.simulate(frames);
		int postSimFriendlyUnitCount = simulator.getState().first.size();
		int myLosses = preSimFriendlyUnitCount - postSimFriendlyUnitCount;
		if(myLosses > 0) {
			return false;
		}
		return true;
	}

	public boolean simulateHarass(Unit harasser, Set<Unit> enemies, int frames) {
		simulator.clear();
		simulator.addUnitPlayer1(new JFAPUnit(harasser));
		for(Unit u : enemies) {
			simulator.addUnitPlayer2(new JFAPUnit(u));
		}
		int preSimFriendlyUnitCount = simulator.getState().first.size();
		simulator.simulate(frames);
		int postSimFriendlyUnitCount = simulator.getState().first.size();
		int myLosses = preSimFriendlyUnitCount - postSimFriendlyUnitCount;
		if(myLosses > 0) {
			return false;
		}
		return true;
	}

	public double getGroundDistance(TilePosition start, TilePosition end) {
		double dist = 0.0;
		if (!start.isValid() || !end.isValid()) return Integer.MAX_VALUE;
		if (BWTA.getRegion(start) == null || BWTA.getRegion(end) == null) return Integer.MAX_VALUE;

		for (TilePosition cpp : BWTA.getShortestPath(start, end))
		{
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
		for(Unit u : closeSim) {
			if(u.getType().isWorker()) {
				workers.add(u);
			}
			if(!u.getType().isWorker() && u.getType().canAttack()) {
				combatUnits.add(u);
			}
		}
		if(combatUnits.isEmpty() && workers.isEmpty()) {
			return null;
		}
		if(!workers.isEmpty()) {
			double distB = Double.MAX_VALUE;
			for(Unit u : workers) {
				double distA = broodWarDistance(myUnit.getPosition(), u.getPosition());
				if(worker == null || distA < distB) {
					worker = u;
					distB = distA;
				}
			}

		}
		if(!combatUnits.isEmpty()) {
			double distB = Double.MAX_VALUE;
			for(Unit u : combatUnits) {
				double distA = broodWarDistance(myUnit.getPosition(), u.getPosition());
				if(chosen == null || distA < distB) {
					chosen = u;
					distB = distA;
				}
			}
		}
		if(chosen != null) {
			return chosen;
		}
		if(worker != null){
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
		        final Pair<Double, Double> unitV = new Pair<>((double)(ownPosition.getX() - enemyPosition.getX()),(double) (ownPosition.getY() - enemyPosition.getY()));
		        final double distance = ownPosition.getDistance(enemyPosition);
		        if (distance < minDistance) {
		            minDistance = distance;
		        }
		        unitV.first = (1/distance) * unitV.first;
		        unitV.second = (1/distance) * unitV.second;
		        vectors.add(unitV);
		    }
		    minDistance = 2 * minDistance * minDistance;
		    for (final Pair<Double, Double> vector : vectors){
		        vector.first *= minDistance;
		        vector.second *= minDistance;
		    }
		    Pair<Double,Double> sumAll = Util.sumPosition(vectors);
		    return Util.sumPosition(ownPosition, new Position((int)(sumAll.first / vectors.size()),(int) (sumAll.second / vectors.size())));
		}

		public void runAgents() {
			List<Vulture> rem = new ArrayList<>();
			for(Vulture vulture : agents) {
				boolean remove = vulture.runAgent();
				if(remove) {
					rem.add(vulture);
				}
			}
			for(Vulture vult : rem) {
				agents.remove(vult);
			}
		}

		public void sendCustomMessage() {
			String name = EI.opponent.toLowerCase();
			if(name == "purplewave".toLowerCase()) {
				game.sendText("Dude, stop with the DT opener :P");
			}
			if(name == "krasi0".toLowerCase()) {
				game.sendText("Please be nice to me!");
			}
			if(name == "hannes bredberg".toLowerCase()) {
				game.sendText("Dont you dare nuke me!");
			}
			if(name == "zercgberht" || name == "protecgberht") {
				game.sendText("Hey there!, brother");
				game.sendText("As the oldest of the three I'm not gonna lose");
			}
		}
}