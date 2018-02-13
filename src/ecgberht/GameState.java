package ecgberht;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.iaie.btree.util.GameHandler;

import com.google.gson.Gson;

import bwapi.*;
import bwta.*;
import bwta.Region;
import ecgberht.Strategies.*;

public class GameState extends GameHandler {

	public BaseLocation enemyBase = null;
	public boolean activeCount = false;
	public boolean defense = false;
	public boolean expanding = false;
	public boolean firstAPM = false;
	public boolean firstProxyBBS = false;
	public boolean initCount = false;
	public boolean movingToExpand = false;
	public boolean enemyIsRandom = true;
	public boolean scout = true;
	public BuildingMap map;
	public BuildingMap testMap;
	public Set<Unit> enemyCombatUnitMemory = new HashSet<Unit>();
	public InfluenceMap inMap;
	public InfluenceMap inMapUnits;
	public int builtBuildings;
	public int builtCC;
	public int builtRefinery;
	public int deltaSupply;
	public int mining;
	public int startCount;
	public int trainedCombatUnits;
	public int trainedWorkers;
	public int mapSize = 2;
	public int workerCountToSustain = 0;
	public List<Unit> enemyInBase = new ArrayList<Unit>();
	public List<Pair<Pair<Unit,Integer>,Boolean> > refineriesAssigned = new ArrayList<Pair<Pair<Unit,Integer>,Boolean> >();
	public List<Pair<Unit,Integer> > mineralsAssigned = new ArrayList<Pair<Unit,Integer> >();
	public List<Pair<Unit,List<Unit>>> DBs = new ArrayList<Pair<Unit,List<Unit>>>();
	public List<Pair<Unit,Pair<UnitType,TilePosition>>> workerBuild = new ArrayList<Pair<Unit,Pair<UnitType,TilePosition>>>();
	public List<Pair<Unit,Position> > workerDefenders = new ArrayList<Pair<Unit,Position> >();
	public List<Pair<Unit,Unit> > repairerTask = new ArrayList<Pair<Unit,Unit> >();
	public List<Pair<Unit,Unit> > workerTask = new ArrayList<Pair<Unit,Unit> >();
	public List<Unit> workerIdle = new ArrayList<Unit>();
	public Map<String,Squad> squads = new HashMap<String,Squad>();
	public Map<Integer, String> TTMs = new HashMap<Integer,String>();
	public Pair<Integer,Integer> deltaCash = new Pair<Integer,Integer>(0,0);
	public Pair<String, Unit> chosenMarine = null;
	public Position attackPosition = null;
	public Set<BaseLocation> BLs = new HashSet<BaseLocation>();
	public Set<BaseLocation> ScoutSLs = new HashSet<BaseLocation>();
	public Set<BaseLocation> SLs = new HashSet<BaseLocation>();
	public Set<String> teamNames = new HashSet<String>(Arrays.asList("Alpha","Bravo","Charlie","Delta","Echo","Foxtrot","Golf","Hotel","India","Juliet","Kilo","Lima","Mike","November","Oscar","Papa","Quebec","Romeo","Sierra","Tango","Uniform","Victor","Whiskey","X-Ray","Yankee","Zulu"));
	public Set<Unit> buildingLot = new HashSet<Unit>();
	public Set<Unit> CCs = new HashSet<Unit>();
	public Set<Unit> CSs = new HashSet<Unit>();
	public Map<Unit,EnemyBuilding> enemyBuildingMemory = new HashMap<Unit,EnemyBuilding>();
	public Set<Unit> MBs = new HashSet<Unit>();
	public Set<Unit> Fs = new HashSet<Unit>();
	public Set<Unit> SBs = new HashSet<Unit>();
	public Set<Unit> Ts = new HashSet<Unit>();
	public Set<Unit> UBs = new HashSet<Unit>();
	public Strategy strat = new Strategy();
	public String chosenSquad = null;
	public TechType chosenResearch = null;
	public TilePosition checkScan = null;
	public TilePosition chosenBaseLocation = null;
	public TilePosition chosenPosition = null;
	public TilePosition closestChoke = null;
	public TilePosition initAttackPosition = null;
	public TilePosition initDefensePosition = null;
	public Unit chosenScout = null;
	public Unit chosenBuilderBL = null;
	public Unit chosenBuilding = null;
	public Unit chosenBuildingAddon = null;
	public Unit chosenBuildingLot = null;
	public Unit chosenBuildingRepair = null;
	public Unit chosenBunker = null;
	public Unit chosenRepairer = null;
	public Unit chosenUnitUpgrader = null;
	public Unit chosenWorker = null;
	public Unit MainCC = null;
	public UnitType chosenAddon = null;
	public UnitType chosenToBuild = null;
	public UnitType chosenUnit = null;
	public UpgradeType chosenUpgrade = null;
	public Unit chosenHarasser = null;
	public Race enemyRace = Race.Unknown;
	public Unit chosenUnitToHarass = null;
	public Gson enemyInfoJSON = new Gson();
	public EnemyInfo EI = new EnemyInfo(game.enemy().getName());
	public Map<Race,Double> dpsWorkerRace = new HashMap<Race,Double>();
	
	public GameState(Mirror bwapi) {
		super(bwapi);
		map = new BuildingMap(game,self);
		map.initMap();
		testMap = map.clone();
		inMap = new InfluenceMap(game,self,game.mapHeight(), game.mapWidth());
		mapSize = BWTA.getStartLocations().size();
		dpsWorkerRace.put(Race.Terran, 7.936);
		dpsWorkerRace.put(Race.Zerg, 5.411);
		dpsWorkerRace.put(Race.Protoss, 5.411);
	}
	
	public Strategy initStrat() {
		BioBuild b = new BioBuild();
		ProxyBBS bbs = new ProxyBBS();
		BioMechBuild bM = new BioMechBuild();
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
				if(random > 0.75 ) {
					return new Strategy(bbs);
				}
				else if(random > 0.4 && random <= 0.75) {
					return new Strategy(bM);
				}
				else {
					return new Strategy(b);
				}
			}
			if(map.contains("Heartbreak Ridge")) {
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
			Map<String,Pair<Integer,Integer>> strategies = new HashMap<>();
			Map<String,AStrategy> nameStrat = new HashMap<>();
			strategies.put(b.name, new Pair<Integer,Integer>(0,0));
			nameStrat.put(b.name, b);
			strategies.put(bbs.name, new Pair<Integer,Integer>(0,0));
			nameStrat.put(bbs.name, bbs);
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
		    if (strategyGamesPlayed < 5 || (strategyGamesPlayed > 0 && winRate > 0.49))
		    {
		        game.sendText("Using default Strategy as Im confident enough to do so");
		        return new Strategy(b);
		    }
		    double C = 0.5;
		    String bestUCBStrategy = null;
		    double bestUCBStrategyVal = Double.MIN_VALUE;
		    for (String strat : strategies.keySet()) {
		    	if(map.contains("Heartbreak Ridge") && strat == "BioMech") {
		    		continue;
		    	}
		        int sGamesPlayed = strategies.get(strat).first + strategies.get(strat).second;
		        double sWinRate = sGamesPlayed > 0 ? DefaultStrategyWins / (double)(strategyGamesPlayed) : 0;
		        double ucbVal = C * Math.sqrt(Math.log((double)totalGamesPlayed / sGamesPlayed));
		        double val = sWinRate + ucbVal;
		        if (val > bestUCBStrategyVal) {
		            bestUCBStrategy = strat;
		            bestUCBStrategyVal = val;
		        }
		    }
		    game.sendText("Chose: " + bestUCBStrategy + " with UCB: " + bestUCBStrategyVal);
		    return new Strategy(nameStrat.get(bestUCBStrategy));
		}
	}

	public void initEnemyRace() {
		if(game.enemy().getRace() != Race.Unknown) {
			enemyRace = game.enemy().getRace();
			enemyIsRandom = false;
		}
	}
	
	public void playSound(String soundFile) {
		try{
			String run = getClass().getResource("GameState.class").toString();
			if(run.startsWith("jar:") || run.startsWith("rsrc:")) {
				InputStream inp = getClass().getClassLoader().getResourceAsStream(soundFile);
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(inp));
				Clip clip = AudioSystem.getClip();
				clip.open(audioInputStream);
				clip.start();
			}
			else {
				soundFile = "src\\" + soundFile; 
				File f = new File(soundFile);
				AudioInputStream audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
				Clip clip = AudioSystem.getClip();
				clip.open(audioIn);
				clip.start();
			}
			
		}
		catch(Exception e) {
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
				CCs.add(u);
			}
		}
	}

	public void initWorkers(){
		List<Unit> units = self.getUnits();
		for(Unit u:units) {
			if(u.getType() == UnitType.Terran_SCV) {
				Pair<Unit,Unit> worker = new Pair<Unit,Unit>(u,null);
				workerTask.add(worker);
			}
		}
	}

	public void addNewResources(Unit unit) {
		List<Unit> minerals = BWTA.getNearestBaseLocation(unit.getTilePosition()).getMinerals();
		List<Unit> gas = BWTA.getNearestBaseLocation(unit.getTilePosition()).getGeysers();
		List<Pair<Unit,Integer> > auxMinerals = new ArrayList<Pair<Unit,Integer> >();
		List<Pair<Pair<Unit,Integer>,Boolean> > auxGas = new ArrayList<Pair<Pair<Unit,Integer>,Boolean> >();
		for(Unit m : minerals) {
			Pair<Unit,Integer> mineral = new Pair<Unit,Integer>(m,0);
			mineralsAssigned.add(mineral);
		}
		mineralsAssigned.addAll(auxMinerals);
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
		List<Unit> minerals = BWTA.getNearestBaseLocation(unit.getTilePosition()).getMinerals();
		List<Unit> gas = BWTA.getNearestBaseLocation(unit.getTilePosition()).getGeysers();
		List<Pair<Unit,Integer> > auxMinerals = new ArrayList<Pair<Unit,Integer> >();
		List<Pair<Pair<Unit,Integer>,Boolean> > auxGas = new ArrayList<Pair<Pair<Unit,Integer>,Boolean> >();
		for(Pair<Unit,Integer> pm : mineralsAssigned) {
			for(Unit m : minerals) {
				if(pm.first.equals(m)) {
					List<Pair<Unit,Unit> > aux = new ArrayList<Pair<Unit,Unit> >();
					for(Pair<Unit,Unit> w: workerTask) {
						if(pm.first.equals(w.second)) {
							aux.add(w);
							workerIdle.add(w.first);
						}
					}
					workerTask.removeAll(aux);
					auxMinerals.add(pm);
				}
			}
		}
		mineralsAssigned.removeAll(auxMinerals);
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
		int apm = game.getAPM(false);
		if(apm > 9000 && !firstAPM ) {
			game.sendText("My APM is over 9000!");
			firstAPM = true;
		}
		game.drawTextScreen(10, 65, Utils.formatText("MGPF: ",Utils.White) + Utils.formatText(String.valueOf(getMineralRate()), Utils.White));
		game.drawTextScreen(10, 80, Utils.formatText("Strategy: ",Utils.White) + Utils.formatText(strat.name, Utils.White));
		game.drawTextScreen(10, 50, Utils.formatText("APM: ",Utils.White) + Utils.formatText(String.valueOf(apm), Utils.White));
		
		if(closestChoke != null) {
			game.drawTextMap(closestChoke.toPosition(), "Choke");
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
			print(chosenBaseLocation,UnitType.Terran_Command_Center,Color.Cyan);
		}
		for(Pair<Unit,Pair<UnitType,TilePosition> > u : workerBuild) {
			game.drawTextMap(u.first.getPosition(), "ChosenBuilder");
			print(u.second.second,u.second.first,Color.Teal);
		}
		if(chosenUnitToHarass != null) {
			print(chosenUnitToHarass,Color.Red);
			game.drawTextMap(chosenUnitToHarass.getPosition(), "WorkerToBother");
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
		for(Unit u: CCs) {
			print(u,Color.Yellow);
			game.drawCircleMap(u.getPosition(), 500, Color.Orange);
		}
		for(Pair<Unit,List<Unit> > u : DBs) {
			game.drawCircleMap(u.first.getPosition(), 300, Color.Orange);
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
		for(Pair<Unit,Integer> m : mineralsAssigned) {
			print(m.first,Color.Cyan);
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
		for (BaseLocation b : BWTA.getBaseLocations()) {
			BLs.add(b);
		}
	}

	public void moveUnitFromChokeWhenExpand(){
		try {
			if(!squads.isEmpty()) {
				List<Unit> radius = game.getUnitsInRadius(closestChoke.toPosition(), 500);
				if(!radius.isEmpty()) {
					List<Chokepoint> cs = BWTA.getRegion(chosenBaseLocation).getChokepoints();
					Chokepoint closestChoke = null;
					for(Chokepoint c : cs) {
						if(!c.getCenter().toTilePosition().equals(this.closestChoke)) {
							double aux = BWTA.getGroundDistance(c.getCenter().toTilePosition().makeValid(),chosenBaseLocation);
							if(aux > 0.0) {
								if(closestChoke == null ||  aux< BWTA.getGroundDistance(closestChoke.getCenter().toTilePosition().makeValid(),chosenBaseLocation)) {
									closestChoke = c;
								}
							}
						}
					}
					if(closestChoke != null) {
						for(Unit t : radius) {
							if(t.getPlayer().getID() == self.getID() && !t.getType().isWorker()) {
								t.attack(closestChoke.getCenter().makeValid());
							}
						}
					}
				}
			}
		} catch(Exception e) {
			System.err.println(e);
		}
	}

	public void fix() {
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
		List<Pair<Unit,Unit> > aux = new ArrayList<Pair<Unit,Unit> >();
		List<Unit> aux2 = new ArrayList<Unit>();
		for(Pair<Unit,Unit> w : workerTask) {
			if(chosenScout != null && w.first.equals(chosenScout)) {
				chosenScout = null;
			}
			if(chosenRepairer != null && w.first.equals(chosenRepairer)) {
				chosenRepairer = null;
			}
			if(workerIdle.contains(w.first) && w.second.getType().isNeutral()) {
				if(w.first.isGatheringMinerals()) {
					aux2.add(w.first);
				}
				else if(w.first.isIdle()) {
					aux.add(w);
				}
			}
			if(w.first.isIdle() && w.second.getType().isNeutral()) {
				workerIdle.add(w.first);
				aux.add(w);
			}
		}
		workerTask.removeAll(aux);
		workerIdle.removeAll(aux2);
//		List<Pair<Unit,Pair<UnitType,TilePosition>>> aux3 = new ArrayList<Pair<Unit,Pair<UnitType,TilePosition>>>();
//		for(Pair<Unit,Pair<UnitType,TilePosition> > u : workerBuild) {
//			if(choosenScout != null && u.first.equals(choosenScout)) {
//				choosenScout = null;
//			}
//			if(chosenRepairer != null && u.first.equals(chosenRepairer)) {
//				chosenRepairer = null;
//			}
//			if(u.first.isIdle() || u.first.isGatheringGas() || u.first.isGatheringMinerals()) {
//				aux3.add(u);
//				deltaCash.first -= u.second.first.mineralPrice();
//				deltaCash.second -= u.second.first.gasPrice();
//				workerIdle.add(u.first);
//			}
//		}
//		workerBuild.removeAll(aux3);
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
			if(r.first.isIdle()) {
				workerIdle.add(r.first);
				aux5.add(r);
			}
		}
		workerDefenders.removeAll(aux5);
		for(Squad u : squads.values()) {
			if(u.members.isEmpty()) {
				squads.values().remove(u);
			}
		}
	}

	public void checkMainEnemyBase() {
		if(enemyBuildingMemory.isEmpty() && ScoutSLs.isEmpty()) {
			enemyBase = null;
			chosenScout = null;
			ScoutSLs.addAll(BLs);
			List<BaseLocation> aux = new ArrayList<BaseLocation>();
			for(BaseLocation b : ScoutSLs) {
				for(Unit u : CCs) {
					if(b.getTilePosition().equals(u.getTilePosition())) {
						if(!aux.contains(b)) {
							aux.add(b);
						}
						break;
					}
				}
				if(!BWTA.isConnected(self.getStartLocation(), b.getTilePosition())) {
					if(!aux.contains(b)) {
						aux.add(b);
					}
				}
				boolean found = false;
				if(game.isVisible(b.getTilePosition())) {
					for (Unit u : game.getUnitsInRadius(b.getPosition(), 500)) {
						if(u.getPlayer().getID() == game.enemy().getID() && u.getType().isBuilding()) {
							found = true;
						}
					}
					if(!found) {
						if(!aux.contains(b)) {
							aux.add(b);
						}
					}
				}
			}
			ScoutSLs.removeAll(aux);
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
		List<Chokepoint> cs = BWTA.getRegion(self.getStartLocation()).getChokepoints();
		BaseLocation closestBase = null;
		for(BaseLocation b : BLs) {
			if(BWTA.isConnected(self.getStartLocation(), b.getTilePosition())) {
				double bS = b.getGroundDistance(BWTA.getStartLocation(self));
				if(bS > 0.0) {
					if ((closestBase == null || bS < closestBase.getGroundDistance(BWTA.getStartLocation(self)))) {
						closestBase = b;
					}
				}
			}

		}
		if(closestBase != null) {
			Chokepoint closestChoke = null;
			for(Chokepoint c : cs) {
				double cS = BWTA.getGroundDistance(c.getCenter().toTilePosition(), closestBase.getTilePosition());
				if(cS > 0.0) {
					if ((closestChoke == null || cS < BWTA.getGroundDistance(closestChoke.getCenter().toTilePosition(), closestBase.getTilePosition()))) {
						closestChoke = c;
					}
				}

			}
			if(closestChoke != null) {
				this.closestChoke = closestChoke.getCenter().toTilePosition();
				initAttackPosition = this.closestChoke;
				initDefensePosition = this.closestChoke;
			}
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
				if(s.getValue().members.size() < 12 && broodWarDistance(getSquadCenter(s.getValue()), unit.getPosition()) <  800 && (chosen == null || broodWarDistance(unit.getPosition(),getSquadCenter(s.getValue())) < broodWarDistance(unit.getPosition(), getSquadCenter(squads.get(chosen))))) {
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
		return count;
	}
	
	public void siegeTanks() {
		if(!squads.isEmpty()) {
			Set<Unit> tanks = new HashSet<Unit>();
			for (Entry<String,Squad> s : squads.entrySet()) {
				tanks.addAll(s.getValue().getTanks());
			}
			if(!tanks.isEmpty()) {
				for(Unit t : tanks) {
					//List<Unit> unitsInRange = t.getUnitsInRadius(UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange());
					boolean found = false;
					for(Unit e : enemyCombatUnitMemory) {
						if(broodWarDistance(e.getPosition(), t.getPosition()) > UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange()) {
							continue;
						}
						if(e.getPlayer().getID() == game.enemy().getID() && !e.getType().isWorker() && !e.getType().isFlyer() && (e.getType().canAttack() || e.getType() == UnitType.Terran_Bunker)) {
							found = true;
							break;
						}
					}
					if(found && t.getType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
						t.siege();
						continue;
					}
					if(!found && t.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
						t.unsiege();
						continue;
					}
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
		if(game.getFrameCount() > 0) {
			rate = ((double)self.gatheredMinerals()-50)/game.getFrameCount();
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
//		System.out.println("Actual frame: " + game.getFrameCount());
//		System.out.println("Minerales when reaching: " + mineralsWhenReach);
//		System.out.println("--------------");
		return mineralsWhenReach;
	}

	public void mineralLocking() {
		for(Pair<Unit, Unit> u : workerTask) {
			if(u.second.getType().isMineralField()) {
				if(!u.first.getTarget().equals(u.second) && u.first.getOrder() == Order.MoveToMinerals && !u.first.isCarryingMinerals()){
					u.first.gather(u.second);
				}
			}
		}
	}
	
	public Position getNearestCC(Position position) {
		Unit chosen = null;
		double distance = Double.MAX_VALUE;
		for (Unit u : CCs) {
			double distance_aux = BWTA.getGroundDistance(u.getTilePosition(), position.toTilePosition());
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
			}
		} catch(Exception e) {
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
					if((chosen == null && game.canBuildHere(tile, bunker) ) || (closestChoke.getDistance(tile) < closestChoke.getDistance(chosen) && game.canBuildHere(tile, bunker))) {
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
			List<Pair<Unit, Unit>> aux = new ArrayList<Pair<Unit, Unit>>();
			int count = 0;
			for(Pair<Unit, Unit> scv : workerTask) {
				if(scv.first.getType().isWorker() && scv.second.getType().isMineralField()) {
					count++;
				}
			}
			for(Pair<Unit, Unit> scv : workerTask) {
				if(count <= workerCountToSustain) {
					break;
				}
				if(scv.first.getType().isWorker() && scv.second.getType().isMineralField() && !scv.first.isCarryingMinerals()) {
					scv.first.move(new TilePosition(game.mapWidth()/2, game.mapHeight()/2).toPosition());
					addToSquad(scv.first);
					for(Pair<Unit,Integer> m : mineralsAssigned) {
						if(m.first.equals(scv.second)) {
							mining--;
							mineralsAssigned.get(mineralsAssigned.indexOf(m)).second--;
						}
					}
					aux.add(scv);
					count--;
					
				}
			}
			workerTask.removeAll(aux);
		}
		
	}

	public boolean armyGroupedBBS() {
		boolean allFine = true;
		for(Squad s : squads.values()) {
			if(s.attack != Position.None) {
				if(s.members.size() == 1) {
					continue;
				}
				List<Unit> circle = game.getUnitsInRadius(getSquadCenter(s), 130);
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
}
