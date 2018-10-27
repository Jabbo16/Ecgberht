package ecgberht;

class StrategyOpponentHistory {

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



