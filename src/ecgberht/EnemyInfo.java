package ecgberht;

import java.util.ArrayList;
import java.util.List;

import bwapi.Race;
import ecgberht.Util.Util;

public class EnemyInfo {
    public String opponent;
    public String race;
    public int wins = 0;
    public int losses = 0;
    public boolean naughty = false;
    public boolean defendHarass = false;
    public List<StrategyOpponentHistory> history = new ArrayList<>();

    EnemyInfo(String opponent, org.openbw.bwapi4j.type.Race race2) {
        this.opponent = opponent;
        this.race = Util.raceToString(race2);
    }

    void updateStrategyOpponentHistory(String strategyName, int mapSize, boolean win) {
        for (StrategyOpponentHistory data : history) {
            if (data.mapSize == mapSize && data.strategyName.equals(strategyName)) {
                if (win) data.wins++;
                else data.losses++;
                return;
            }
        }
        StrategyOpponentHistory newData = new StrategyOpponentHistory(strategyName, mapSize, win);
        history.add(newData);
    }

    static class StrategyOpponentHistory {
        int losses = 0;
        int mapSize;
        int wins = 0;
        String strategyName;

        StrategyOpponentHistory(String strategyName, int mapSize, boolean win) {
            this.strategyName = strategyName;
            this.mapSize = mapSize;
            if (win) this.wins++;
            else this.losses++;
        }
    }
}