package ecgberht;

import ecgberht.Strategies.*;
import ecgberht.Util.MutablePair;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;

import java.util.*;
import java.util.function.Consumer;

import static ecgberht.Ecgberht.getGs;

public class StrategyManager {
    private FullBio b = new FullBio();
    private ProxyBBS bbs = new ProxyBBS();
    private BioMech bM = new BioMech();
    private FullBioFE bFE = new FullBioFE();
    private BioMechFE bMFE = new BioMechFE();
    private FullMech FM = new FullMech();
    private BioGreedyFE bGFE = new BioGreedyFE();
    private MechGreedyFE mGFE = new MechGreedyFE();
    private BioMechGreedyFE bMGFE = new BioMechGreedyFE();
    private TwoPortWraith tPW = new TwoPortWraith();
    private ProxyEightRax pER = new ProxyEightRax();
    private VultureRush vR = new VultureRush();
    private TheNitekat tNK = new TheNitekat();
    private JoyORush jOR = new JoyORush();
    private FastCC fastCC = new FastCC();

    public Strategy strat;

    private Map<String, MutablePair<Integer, Integer>> strategies = new LinkedHashMap<>();
    private Map<String, Strategy> nameStrat = new LinkedHashMap<>();

    StrategyManager() {
        this.strat = initStrat();
    }

    void choose14CCTransition() {
        double C = 0.7;
        int totalGamesPlayed = getGs().learningManager.getEnemyInfo().wins + getGs().learningManager.getEnemyInfo().losses;
        List<String> validTransitions = new ArrayList<>(Arrays.asList(b.name, bMFE.name, FM.name));
        if (getGs().enemyRace == Race.Zerg) validTransitions.add(tPW.name);
        String bestUCBStrategy = null;
        double bestUCBStrategyVal = Double.MIN_VALUE;
        for (Map.Entry<String, MutablePair<Integer, Integer>> strat : strategies.entrySet()) {
            if (!validTransitions.contains(strat.getKey())) continue;
            int sGamesPlayed = strat.getValue().first + strat.getValue().second;
            double sWinRate = sGamesPlayed > 0 ? (strat.getValue().first / (double) (sGamesPlayed)) : 0;
            double ucbVal = sGamesPlayed == 0 ? 0.85 : C * Math.sqrt(Math.log(((double) totalGamesPlayed / (double) sGamesPlayed)));
            double val = sWinRate + ucbVal;
            if (val > bestUCBStrategyVal) {
                bestUCBStrategy = strat.getKey();
                bestUCBStrategyVal = val;
            }
        }
        getGs().ih.sendText("Transitioning from 14CC to " + bestUCBStrategy + " with UCB: " + bestUCBStrategyVal);
        strat = nameStrat.get(bestUCBStrategy);
        if (getGs().naturalChoke != null) getGs().defendPosition = getGs().naturalChoke.getCenter().toPosition();
    }

    void updateStrat() {
        if (strat.trainUnits.contains(UnitType.Terran_Firebat) && getGs().enemyRace == Race.Zerg) getGs().maxBats = 3;
        else getGs().maxBats = 0;
        if (strat.trainUnits.contains(UnitType.Terran_Goliath)) getGs().maxGoliaths = 0;
    }

    private boolean alwaysZealotRushes() {
        if (getGs().enemyRace != Race.Protoss) return false;
        List<String> zealots = new ArrayList<>(Arrays.asList("purplewavelet", "wulibot", "flash", "carstennielsen"));
        return zealots.contains(getGs().learningManager.getEnemyInfo().opponent.toLowerCase().replace(" ", ""));
    }

    private Strategy initStrat() {
        try {
            String forcedStrat = ConfigManager.getConfig().ecgConfig.forceStrat;
            LearningManager.EnemyInfo EI = getGs().learningManager.getEnemyInfo();
            if (getGs().enemyRace == Race.Zerg && EI.naughty) return b;
            if (getGs().bw.getBWMap().mapHash().equals("6f5295624a7e3887470f3f2e14727b1411321a67")) { // Plasma!!!
                getGs().maxWraiths = 200; // HELL
                return new PlasmaWraithHell();
            }
            if (alwaysZealotRushes()) {
                IntelligenceAgency.setEnemyStrat(IntelligenceAgency.EnemyStrats.ZealotRush);
                bFE.armyForExpand += 5;
                bFE.workerGas = 2;
                return bFE;
            }
            Consumer<Strategy> addStrat = (Strategy strat) -> {
                strategies.put(strat.name, new MutablePair<>(0, 0));
                nameStrat.put(strat.name, strat);
            };

            switch (getGs().enemyRace) {
                case Zerg:
                    addStrat.accept(bFE);
                    addStrat.accept(tPW);
                    addStrat.accept(bGFE);
                    addStrat.accept(pER);
                    addStrat.accept(bMGFE);
                    addStrat.accept(bM);
                    addStrat.accept(fastCC);
                    addStrat.accept(bbs);
                    addStrat.accept(FM);
                    addStrat.accept(b);
                    addStrat.accept(bMFE);
                    break;

                case Terran:
                    addStrat.accept(FM);
                    addStrat.accept(fastCC);
                    addStrat.accept(pER);
                    addStrat.accept(bM);
                    addStrat.accept(mGFE);
                    addStrat.accept(bbs);
                    addStrat.accept(bFE);
                    addStrat.accept(tPW);
                    addStrat.accept(bMGFE);
                    addStrat.accept(bGFE);
                    addStrat.accept(b);
                    addStrat.accept(bMFE);
                    break;

                case Protoss:
                    addStrat.accept(bMGFE);
                    addStrat.accept(jOR);
                    addStrat.accept(FM);
                    addStrat.accept(fastCC);
                    addStrat.accept(pER);
                    addStrat.accept(bM);
                    addStrat.accept(mGFE);
                    addStrat.accept(b);
                    addStrat.accept(bGFE);
                    addStrat.accept(bMFE);
                    addStrat.accept(bFE);
                    break;

                case Unknown:
                    addStrat.accept(b);
                    addStrat.accept(bM);
                    addStrat.accept(bGFE);
                    addStrat.accept(bMGFE);
                    addStrat.accept(FM);
                    addStrat.accept(bbs);
                    addStrat.accept(mGFE);
                    addStrat.accept(jOR);
                    addStrat.accept(bMFE);
                    addStrat.accept(bFE);
                    break;
            }
            if (!forcedStrat.equals("")) {
                if (forcedStrat.equals("Random")) {
                    int index = new Random().nextInt(nameStrat.entrySet().size());
                    Iterator<Map.Entry<String, Strategy>> iter = nameStrat.entrySet().iterator();
                    for (int i = 0; i < index; i++) {
                        iter.next();
                    }
                    Map.Entry<String, Strategy> pickedStrategy = iter.next();
                    getGs().ih.sendText("Picked random strategy " + pickedStrategy.getKey());
                    return pickedStrategy.getValue();
                } else if (nameStrat.containsKey(forcedStrat)) {
                    getGs().ih.sendText("Picked forced strategy " + forcedStrat);
                    if (forcedStrat.equals("14CC")) {
                        for (LearningManager.EnemyInfo.StrategyOpponentHistory r : EI.history) {
                            if (strategies.containsKey(r.strategyName)) {
                                strategies.get(r.strategyName).first += r.wins;
                                strategies.get(r.strategyName).second += r.losses;
                            }
                        }
                    }
                    return nameStrat.get(forcedStrat);
                }
            }

            int totalGamesPlayed = EI.wins + EI.losses;
            if (totalGamesPlayed < 1) {
                Strategy strat = b;
                switch (getGs().enemyRace) {
                    case Zerg:
                        strat = bGFE;
                        break;
                    case Terran:
                        strat = FM;
                        break;
                    case Protoss:
                        strat = bMFE;
                        break;
                }
                getGs().ih.sendText("I dont know you that well yet, lets pick the standard strategy, " + strat.name);
                return strat;
            }
            for (LearningManager.EnemyInfo.StrategyOpponentHistory r : EI.history) {
                if (strategies.containsKey(r.strategyName)) {
                    strategies.get(r.strategyName).first += r.wins;
                    strategies.get(r.strategyName).second += r.losses;
                }
            }
            double maxWinRate = 0.0;
            String bestStrat = null;
            int totalGamesBestS = 0;
            for (Map.Entry<String, MutablePair<Integer, Integer>> s : strategies.entrySet()) {
                int totalGames = s.getValue().first + s.getValue().second;
                if (totalGames < 2) continue;
                double winRate = (double) s.getValue().first / totalGames;
                if (winRate >= 0.75 && winRate > maxWinRate) {
                    maxWinRate = winRate;
                    bestStrat = s.getKey();
                    totalGamesBestS = totalGames;
                }
            }
            if (maxWinRate != 0.0 && bestStrat != null) {
                getGs().ih.sendText("Using best Strategy: " + bestStrat + " with winrate " + maxWinRate * 100 + "% and " + totalGamesBestS + " games played");
                return nameStrat.get(bestStrat);
            }
            double C = 0.7;
            String bestUCBStrategy = null;
            double bestUCBStrategyVal = Double.MIN_VALUE;
            for (Map.Entry<String, MutablePair<Integer, Integer>> strat : strategies.entrySet()) {
                int sGamesPlayed = strat.getValue().first + strat.getValue().second;
                double sWinRate = sGamesPlayed > 0 ? (strat.getValue().first / (double) (sGamesPlayed)) : 0;
                double ucbVal = sGamesPlayed == 0 ? 0.85 : C * Math.sqrt(Math.log(((double) totalGamesPlayed / (double) sGamesPlayed)));
                if (nameStrat.get(strat.getKey()).proxy && getGs().mapSize == 2) ucbVal += 0.03;
                if (strat.getKey().equals("14CC") && getGs().mapSize == 4) ucbVal += 0.03;
                double val = sWinRate + ucbVal;
                if (val > bestUCBStrategyVal) {
                    bestUCBStrategy = strat.getKey();
                    bestUCBStrategyVal = val;
                }
            }
            getGs().ih.sendText("Chose: " + bestUCBStrategy + " with UCB: " + bestUCBStrategyVal);
            return nameStrat.get(bestUCBStrategy);
        } catch (Exception e) {
            System.err.println("Error initStrat, using default strategy");
            e.printStackTrace();
            return b;
        }
    }
}
