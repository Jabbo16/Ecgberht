package ecgberht;

import ecgberht.Strategies.*;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;

import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;

import java.util.*;
import java.util.function.Consumer;

import static ecgberht.Ecgberht.getGs;

public class StrategyManager {
	private static StrategyManager managerInstance;
	  private StrategyManager() {
	        initBaseStrategies();
	        this.setStrategy(initStrat());
	        AddSpecialUnitsIfParticularMap();
	    }
	  public static StrategyManager getInstance() {
		  if(!managerUsed) {
			  managerInstance = new StrategyManager();
			  managerUsed = true;
		  }
			return managerInstance;
		}
	
	private static boolean managerUsed = false;
    private Strategy strategy;
    private Map<String, MutablePair<Integer, Integer>> strategies = new LinkedHashMap<>();
    private Map<String, Strategy> nameOfStrategies = new LinkedHashMap<>();

    private Strategy b = new FullBio();
    private Strategy bbs = new ProxyBBS();
    private Strategy bM = new BioMech();
    private Strategy bFE = new FullBioFE();
    private Strategy bMFE = new BioMechFE();
    private Strategy FM = new FullMech();
    private Strategy bGFE = new BioGreedyFE();
    private Strategy mGFE = new MechGreedyFE();
    private Strategy bMGFE = new BioMechGreedyFE();
    private Strategy tPW = new TwoPortWraith();
    private Strategy pER = new ProxyEightRax();
    private Strategy vR = new VultureRush();
    private Strategy tNK = new TheNitekat();
    private Strategy jOR = new JoyORush();
    private Strategy fastCC = new FastCC();
    
    private void AddSpecialUnitsIfParticularMap() {
        if (checkMapHash()) { // GoldRush
            this.getStrategy().trainUnits.add(UnitType.Terran_Wraith);
        }
    }

	public boolean checkMapHash() {
		return getGs().getGame().getBWMap().mapHash().equals("666dd28cd3c85223ebc749a481fc281e58221e4a");
	}

    private void initBaseStrategies() {
        Consumer<Strategy> addStrategy = (strategy) -> {
            strategies.put(strategy.name, new MutablePair<>(0, 0));
            nameOfStrategies.put(strategy.name, strategy);
        };
        switch (getGs().enemyRace) {
            case Zerg:
			addZergStrategy(addStrategy);
                break;

            case Terran:
			addTerranStrategy(addStrategy);
                break;

            case Protoss:
			addProtossStrategy(addStrategy);
                break;

            case Unknown:
			addUnknownStrategy(addStrategy);
                break;
        }
    }

	public void addUnknownStrategy(Consumer<Strategy> addStrat) {
		addStrat.accept(b);
		addStrat.accept(FM);
		addStrat.accept(bM);
		addStrat.accept(bGFE);
		addStrat.accept(bMGFE);
		addStrat.accept(bbs);
		addStrat.accept(mGFE);
		addStrat.accept(jOR);
		addStrat.accept(bMFE);
		addStrat.accept(bFE);
	}

	public void addProtossStrategy(Consumer<Strategy> addStrat) {
		addStrat.accept(FM);
		addStrat.accept(jOR);
		addStrat.accept(pER);
		addStrat.accept(fastCC);
		addStrat.accept(mGFE);
		addStrat.accept(bM);
		addStrat.accept(bMGFE);
		addStrat.accept(b);
		addStrat.accept(bGFE);
		addStrat.accept(bMFE);
		addStrat.accept(bFE);
		addStrat.accept(vR);
		addStrat.accept(tNK);
	}

	public void addTerranStrategy(Consumer<Strategy> addStrat) {
		addStrat.accept(FM);
		addStrat.accept(fastCC);
		addStrat.accept(pER);
		addStrat.accept(bMGFE);
		addStrat.accept(tPW);
		addStrat.accept(bM);
		addStrat.accept(mGFE);
		addStrat.accept(bbs);
		addStrat.accept(bFE);
		addStrat.accept(bGFE);
		addStrat.accept(b);
		addStrat.accept(bMFE);
		addStrat.accept(vR);
		addStrat.accept(tNK);
	}

	public void addZergStrategy(Consumer<Strategy> addStrat) {
		addStrat.accept(bGFE);
		addStrat.accept(tPW);
		addStrat.accept(pER);
		addStrat.accept(bFE);
		addStrat.accept(fastCC);
		addStrat.accept(bMGFE);
		addStrat.accept(bbs);
		addStrat.accept(bM);
		addStrat.accept(FM);
		addStrat.accept(b);
		addStrat.accept(bMFE);
		addStrat.accept(vR);
		addStrat.accept(tNK);
	}
	public boolean forTerranAndProtossTransition(List<String> validTransitions) {
		return validTransitions.addAll(Arrays.asList(FM.name, bMGFE.name, bGFE.name));
	}
	public boolean forZergTransition(List<String> validTransitions) {
		return validTransitions.addAll(Arrays.asList(bGFE.name, bMGFE.name, tPW.name));
	}
    void chooseTransitionForEnemy() {
        double C = 0.75;
        int totalGamesPlayed = getGs().learningManager.getEnemyInfo().wins + getGs().learningManager.getEnemyInfo().losses;
        List<String> validTransitions = new ArrayList<>();
        switch (getGs().enemyRace) {
            case Zerg:
                forZergTransition(validTransitions);
                break;
                
            case Terran:
            case Protoss:
                forTerranAndProtossTransition(validTransitions);
                break;
        }
        String bestUCBStrategy = null;
        double bestUCBStrategyVal = Double.MIN_VALUE;
        for (Map.Entry<String, MutablePair<Integer, Integer>> strat : strategies.entrySet()) {
            final boolean transitionHasNoKey = !validTransitions.contains(strat.getKey());
			if (transitionHasNoKey) continue;
            int sGamesPlayed = strat.getValue().first + strat.getValue().second;
            
            double sWinRate = getWinRate(sGamesPlayed, strat);
            double ucbVal = getUcbVal(C, totalGamesPlayed, sGamesPlayed);
            double val = sWinRate + ucbVal;
            if (val > bestUCBStrategyVal) {
                bestUCBStrategy = strat.getKey();
                bestUCBStrategyVal = val;
            }
        }
        Util.sendText("Transitioning from 14CC to " + bestUCBStrategy + " with UCB: " + bestUCBStrategyVal);
        setStrategy(nameOfStrategies.get(bestUCBStrategy));
        final boolean naturalChokeAllocated = getGs().naturalChoke != null;
		if (naturalChokeAllocated) {
			final Position naturalChokeToCenter = getGs().naturalChoke.getCenter().toPosition();
			getGs().defendPosition = naturalChokeToCenter;
		}
    }
	public double getUcbVal(double C, int totalGamesPlayed, int sGamesPlayed) {
		if(sGamesPlayed == 0) {
			return 0.85;
		}
		else {
			return C * Math.sqrt(Math.log(((double) totalGamesPlayed / (double) sGamesPlayed)));
		}
	}
    public double getWinRate(int sGamesPlayed,Map.Entry<String, MutablePair<Integer, Integer>> strat) {
  	  if (sGamesPlayed > 0) {
        	return (strat.getValue().first / (double) (sGamesPlayed));
        }
        else {
        	return 0;
        }
    }

    void chooseProxyTransition() {
        double C = 0.8;
        int totalGamesPlayed = getGs().learningManager.getEnemyInfo().wins + getGs().learningManager.getEnemyInfo().losses;
        List<String> validTransitions = new ArrayList<>(Arrays.asList(b.name, bMFE.name, FM.name));
        if (getGs().enemyRace == Race.Zerg) validTransitions.add(tPW.name);
        String bestUCBStrategy = null;
        double bestUCBStrategyVal = Double.MIN_VALUE;
       
        for (Map.Entry<String, MutablePair<Integer, Integer>> strat : strategies.entrySet()) {
        	final boolean transitionHasNoKey = !validTransitions.contains(strat.getKey());
            if (transitionHasNoKey) continue;
            int sGamesPlayed = strat.getValue().first + strat.getValue().second;
            double sWinRate = getWinRate(sGamesPlayed, strat);
            double ucbVal = getUcbVal(C, totalGamesPlayed, sGamesPlayed);
            double val = sWinRate + ucbVal;
            if (val > bestUCBStrategyVal) {
                bestUCBStrategy = strat.getKey();
                bestUCBStrategyVal = val;
            }
        }
        Util.sendText("Transitioning from Proxy to " + bestUCBStrategy + " with UCB: " + bestUCBStrategyVal);
        setStrategy(nameOfStrategies.get(bestUCBStrategy));
        if (getGs().naturalChoke != null) getGs().defendPosition = getGs().naturalChoke.getCenter().toPosition();
    }

    void updateStrat() {
        final boolean canTrainFirebat = getStrategy().trainUnits.contains(UnitType.Terran_Firebat);
		final boolean enemyZerg = getGs().enemyRace == Race.Zerg;
		 final boolean canTrainGoliath = getStrategy().trainUnits.contains(UnitType.Terran_Goliath);
		if (canTrainFirebat && enemyZerg) getGs().maxBats = 3;
		else getGs().maxBats = 0;
       if (canTrainGoliath) getGs().maxGoliaths = 0;
    }

    private boolean alwaysZealotRushes() {
        final boolean enemyNotProtoss = getGs().enemyRace != Race.Protoss;
		if (enemyNotProtoss) return false;
        List<String> zealots = new ArrayList<>(Arrays.asList("purplewavelet", "wulibot", "flash", "carstennielsen"));
        final String enemyInfo = getGs().learningManager.getEnemyInfo().opponent.toLowerCase().replace(" ", "");
		return zealots.contains(enemyInfo);
    }

    private Strategy getRandomStrategy() {
        int index = new Random().nextInt(nameOfStrategies.entrySet().size());
        Iterator<Map.Entry<String, Strategy>> iterator = nameOfStrategies.entrySet().iterator();
        for (int i = 0; i < index; i++) {
            iterator.next();
        }
       return iterator.next().getValue();
    }

    private Strategy initStrat() {
        try {
            LearningManager.EnemyInfo enemyInfo = getGs().learningManager.getEnemyInfo();
            String forcedStrategy = ConfigManager.getConfig().ecgConfig.forceStrat;
            final boolean strategyNull = !forcedStrategy.equals("");
			if (strategyNull) {
                final boolean isStrategyRandom = forcedStrategy.toLowerCase().equals("random");
				if (isStrategyRandom) {
                    Strategy randomStrategy = this.getRandomStrategy();
                    Util.sendText("Picked random strategy " + randomStrategy.name);
                    return randomStrategy;
                } else {
					final boolean forcedStrategyInStrategies = nameOfStrategies.containsKey(forcedStrategy);
					if (forcedStrategyInStrategies) {
					    Util.sendText("Picked forced strategy " + forcedStrategy);
					    final boolean isForcedStrategy14CC = forcedStrategy.equals("14CC");
						if (isForcedStrategy14CC) {
					        for (LearningManager.EnemyInfo.StrategyOpponentHistory learningManager : enemyInfo.history) {
					            if (strategies.containsKey(learningManager.strategyName)) {
					                strategies.get(learningManager.strategyName).first += learningManager.wins;
					                strategies.get(learningManager.strategyName).second += learningManager.losses;
					            }
					        }
					    }
					    return nameOfStrategies.get(forcedStrategy);
					}
				}
            }
            final boolean isHumanMode = ConfigManager.getConfig().ecgConfig.humanMode;
			final boolean isRandomLtFive = Math.round(Math.random() * 10) < 5;
			if (isHumanMode && isRandomLtFive) {
                return this.getRandomStrategy();
            }
            if (getGs().enemyRace == Race.Zerg && enemyInfo.naughty) return b;
            final boolean checkTempMapHash = getGs().bw.getBWMap().mapHash().equals("6f5295624a7e3887470f3f2e14727b1411321a67");
			if (checkTempMapHash) {
                getGs().maxWraiths = 200;
                return new PlasmaWraithHell();
            }
            if (alwaysZealotRushes()) {
                IntelligenceAgency.setEnemyStrat(IntelligenceAgency.EnemyStrats.ZealotRush);
                bFE.armyForExpand += 5;
                bFE.workerGas = 2;
                return bFE;
            }
            removeStrategiesMapSpecific();
            int totalGamesPlayed = enemyInfo.wins + enemyInfo.losses;
            for (LearningManager.EnemyInfo.StrategyOpponentHistory learningManager : enemyInfo.history) {
                if (strategies.containsKey(learningManager.strategyName)) {
                    strategies.get(learningManager.strategyName).first += learningManager.wins;
                    strategies.get(learningManager.strategyName).second += learningManager.losses;
                }
            }
            double maxWinRate = 0.0;
            String bestStrategy = null;
            int totalGamesBestS = 0;
            for (Map.Entry<String, MutablePair<Integer, Integer>> s : strategies.entrySet()) {
                int totalGames = s.getValue().first + s.getValue().second;
                final boolean gameLtTwo = totalGames < 2;
				if (gameLtTwo) continue;
                double winRate = (double) s.getValue().first / totalGames;
                if (winRate >= 0.75 && winRate > maxWinRate) {
                    maxWinRate = winRate;
                    bestStrategy = s.getKey();
                    totalGamesBestS = totalGames;
                }
            }
            final boolean maxWinRateNotZero = maxWinRate != 0.0;
			final boolean bestStrategyNotNull = bestStrategy != null;
			if (maxWinRateNotZero && bestStrategyNotNull) {
                Util.sendText("Using best Strategy: " + bestStrategy + " with winrate " + maxWinRate * 100 + "% and " + totalGamesBestS + " games played");
                return nameOfStrategies.get(bestStrategy);
            }
            double C = 0.7;
            String bestUCBStrategy = null;
            double bestUCBStrategyVal = Double.MIN_VALUE;
            for (Map.Entry<String, MutablePair<Integer, Integer>> strat : strategies.entrySet()) {
                int sGamesPlayed = strat.getValue().first + strat.getValue().second;
                double sWinRate = getWinRate(sGamesPlayed, strat);
                double ucbVal = getUcbVal(C, totalGamesPlayed, sGamesPlayed);
                final boolean moreThanOneGamePlayed = totalGamesPlayed > 0;
				final boolean isMapSizeTwo = getGs().mapSize == 2;
				final boolean isStrategyKey14CC = strat.getKey().equals("14CC");
				final boolean isMapSizeFour = getGs().mapSize == 4;
				final boolean isStrategyKeyProxy = nameOfStrategies.get(strat.getKey()).proxy;
				if (moreThanOneGamePlayed && isStrategyKeyProxy && isMapSizeTwo) 
					ucbVal += 0.03;
				if (moreThanOneGamePlayed && isStrategyKey14CC && isMapSizeFour) 
					ucbVal += 0.03;
                double val = sWinRate + ucbVal;
                if (val > bestUCBStrategyVal) {
                    bestUCBStrategy = strat.getKey();
                    bestUCBStrategyVal = val;
                }
            }
            final boolean gameNotPlayed = totalGamesPlayed < 1;
			if (gameNotPlayed)
                Util.sendText("I dont know you that well yet, lets pick " + bestUCBStrategy);
            else Util.sendText("Chose: " + bestUCBStrategy + " with UCB: " + bestUCBStrategyVal);
            return nameOfStrategies.get(bestUCBStrategy);
        } catch (Exception e) {
            System.err.println("Error initStrat, using default strategy");
            e.printStackTrace();
            return b;
        }
    }

    private void removeStrategiesMapSpecific() {
        Consumer<Strategy> removeStrategy = (strategy) -> {
            strategies.remove(strategy.name);
            nameOfStrategies.remove(strategy.name);
        };
        if (checkMapHash()) { // GoldRush
            removeStrategy.accept(pER);
            removeStrategy.accept(bbs);
            removeStrategy.accept(tNK);
            removeStrategy.accept(jOR);
        }
    }
	public Strategy getStrategy() {
		return strategy;
	}
	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}
}
