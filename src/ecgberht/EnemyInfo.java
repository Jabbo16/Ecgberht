package ecgberht;

import java.util.ArrayList;
import java.util.List;

public class EnemyInfo {
    public String opponent = "";
    public int wins = 0;
    public int losses = 0;
    public boolean naughty = false;
    public boolean defendHarass = false;
    public List<StrategyOpponentHistory> history = new ArrayList<StrategyOpponentHistory>();

    public EnemyInfo(String opponent) {
        this.opponent = opponent;
    }

    public EnemyInfo(String wins, String losses, List<StrategyOpponentHistory> history, String naughty, boolean defendHarass) {
        this.wins = Integer.parseInt(wins);
        this.history = history;
        this.defendHarass = defendHarass;
        this.losses = Integer.parseInt(losses);
        if (naughty == "true") {
            this.naughty = true;
        }
    }

    public void updateStrategyOpponentHistory(String strategyName, int mapSize, boolean win) {
        for (StrategyOpponentHistory data : history) {
            if (data.mapSize == mapSize && data.strategyName.equals(strategyName)) {
                if (win) {
                    data.wins++;
                } else {
                    data.losses++;
                }
                return;
            }
        }
        StrategyOpponentHistory newData = new StrategyOpponentHistory(strategyName, mapSize, win);
        history.add(newData);
    }
}
