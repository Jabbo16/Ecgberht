package ecgberht;

import bwapi.Race;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ecgberht.Util.Util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static ecgberht.Ecgberht.getGame;

public class LearningManager {

    private EnemyHistory enemyHistory = new EnemyHistory();
    private EnemyInfo enemyInfo;
    private final String dir = "bwapi-data/write/";

    LearningManager(String name, Race race) {
        enemyInfo = new EnemyInfo(name, race);
    }

    private void readOpponentHistory(String opponentName) {
        Gson enemyHistoryJSON = new Gson();
        String filename = opponentName + "_" + enemyInfo.race + "-History.json";
        String path = "bwapi-data/read/" + filename;
        try {
            if (Files.exists(Paths.get(path))) {
                enemyHistory = enemyHistoryJSON.fromJson(new FileReader(path), EnemyHistory.class);
                return;
            }
            path = "bwapi-data/write/" + filename;
            if (Files.exists(Paths.get(path))) {
                enemyHistory = enemyHistoryJSON.fromJson(new FileReader(path), EnemyHistory.class);
                return;
            }
            path = "bwapi-data/AI/" + filename;
            if (Files.exists(Paths.get(path)))
                enemyHistory = enemyHistoryJSON.fromJson(new FileReader(path), EnemyHistory.class);
        } catch (Exception e) {
            System.err.println("readOpponentHistory");
            e.printStackTrace();
        }
    }

    private void writeJSON(Object content, String path) {
        Gson aux = new GsonBuilder().setPrettyPrinting().create();
        File directory = new File(dir);
        if (!directory.exists()) directory.mkdir();
        try (FileWriter writer = new FileWriter(path)) {
            aux.toJson(content, writer);
        } catch (IOException e) {
            System.err.println("writeOpponentHistory");
            e.printStackTrace();
        }
    }

    private void writeOpponentInfo(String name, boolean enemyIsRandom) {
        if (enemyIsRandom && enemyInfo.naughty) enemyInfo.naughty = false;
        String path = dir + name + "_" + enemyInfo.race + ".json";
        getGame().sendText("Writing result to: " + path);
        writeJSON(enemyInfo, path);
    }

    private void writeOpponentHistory(String name) {
        String path = dir + name + "_" + enemyInfo.race + "-History.json";
        getGame().sendText("Writing history to: " + path);
        writeJSON(enemyHistory, path);
    }

    private void readOpponentInfo(String opponentName) {
        Gson enemyInfoJSON = new Gson();
        String filename = opponentName + "_" + enemyInfo.race + ".json";
        String path = "bwapi-data/read/" + filename;
        try {
            if (Files.exists(Paths.get(path))) {
                enemyInfo = enemyInfoJSON.fromJson(new FileReader(path), EnemyInfo.class);
                return;
            }
            path = "bwapi-data/write/" + filename;
            if (Files.exists(Paths.get(path))) {
                enemyInfo = enemyInfoJSON.fromJson(new FileReader(path), EnemyInfo.class);
                return;
            }
            path = "bwapi-data/AI/" + filename;
            if (Files.exists(Paths.get(path))) {
                enemyInfo = enemyInfoJSON.fromJson(new FileReader(path), EnemyInfo.class);
            }

        } catch (Exception e) {
            System.err.println("readOpponentInfo");
            e.printStackTrace();
        }
    }

    EnemyInfo getEnemyInfo() {
        return enemyInfo;
    }

    LinkedList<EnemyHistory.EnemyGame> getEnemyHistory() {
        return enemyHistory.history;
    }

    public void setRace(String raceToString) {
        if (enemyInfo.race == null) enemyInfo.race = raceToString;
    }

    public boolean isNaughty() {
        return enemyInfo.naughty;
    }

    void onEnd(String stratName, int mapSize, boolean win, String opponentName, Race enemyRace, String mapName, boolean enemyIsRandom, IntelligenceAgency.EnemyStrats enemyStrat) {
        enemyInfo.updateStrategyOpponentHistory(stratName, mapSize, win);
        enemyHistory.history.add(new EnemyHistory.EnemyGame(opponentName, enemyRace, win, stratName, mapName, enemyStrat));
        if (win) enemyInfo.wins++;
        else enemyInfo.losses++;
        writeOpponentInfo(opponentName, enemyIsRandom);
        writeOpponentHistory(opponentName);
    }

    void setNaughty(boolean naughty) {
        enemyInfo.naughty = naughty;
    }

    public boolean defendHarass() {
        return enemyInfo.defendHarass;
    }

    public void setHarass(boolean harass) {
        enemyInfo.defendHarass = harass;
    }

    void onStart(String name, String raceToString) {
        setRace(raceToString);
        readOpponentInfo(name);
        readOpponentHistory(name);
    }

    public static class EnemyHistory {
        public LinkedList<EnemyGame> history = new LinkedList<>();

        static class EnemyGame {
            private String opponent;
            private String race;
            private String outcome;
            private String strategy;
            private String mapName;
            public String opponentStrategy;

            EnemyGame(String opponent, Race race, boolean outcome, String strategy, String mapName, IntelligenceAgency.EnemyStrats enemyStrat) {
                this.opponent = opponent;
                this.race = Util.raceToString(race);
                this.outcome = outcome ? "Win" : "Lose";
                this.strategy = strategy;
                this.mapName = mapName;
                this.opponentStrategy = enemyStrat.toString();
            }
        }
    }

    public static class EnemyInfo {
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
}
