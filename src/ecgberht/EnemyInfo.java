package ecgberht;

import ecgberht.Util.Util;
import org.openbw.bwapi4j.type.Race;

import java.util.ArrayList;
import java.util.List;

public class EnemyInfo {
    public String opponent;
    public String race;
    public int wins = 0;
    public int losses = 0;
    public boolean naughty = false;
    public boolean defendHarass = false;
    public List<StrategyOpponentHistory> history = new ArrayList<>();

    EnemyInfo(String opponent, Race race) {
        this.opponent = opponent;
        this.race = Util.raceToString(race);
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
}
