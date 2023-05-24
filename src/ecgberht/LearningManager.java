package ecgberht;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ecgberht.Util.Util;
import org.openbw.bwapi4j.type.Race;
import ecgberht.EnemyHistory;
import ecgberht.EnemyInfo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
            System.err.println("writeJSON");
            e.printStackTrace();
        }
    }

    private void writeOpponentInfo(String name, boolean enemyIsRandom) {
        if (enemyIsRandom && enemyInfo.naughty) enemyInfo.naughty = false;
        String path = dir + name + "_" + enemyInfo.race + ".json";
        Util.sendText("Writing result to: " + path);
        writeJSON(enemyInfo, path);
    }

    private void writeOpponentHistory(String name) {
        String path = dir + name + "_" + enemyInfo.race + "-History.json";
        Util.sendText("Writing history to: " + path);
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
}
