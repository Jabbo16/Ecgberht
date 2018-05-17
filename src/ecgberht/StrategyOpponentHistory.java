package ecgberht;
//import java.util.Calendar;
//import java.text.SimpleDateFormat;

public class StrategyOpponentHistory {

    int losses = 0;
    int mapSize = 0;
    int wins = 0;
    String strategyName = "";

    public StrategyOpponentHistory(String strategyName, int mapSize, boolean win) {
        this.strategyName = strategyName;
        this.mapSize = mapSize;
        if (win) {
            this.wins++;
        } else {
            this.losses++;
        }
    }
}



