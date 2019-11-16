package ecgberht.CherryVis;

import com.github.luben.zstd.Zstd;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ecgberht.GameState;
import ecgberht.Util.Util;
import org.openbw.bwapi4j.unit.Unit;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class CherryVisDumper {
    private GameState gameState;
    private TraceData traceData;
    private GameSummary gameSummary;
    private final String dir = "bwapi-data/write/cherryvis";

    public CherryVisDumper(GameState gameState) {
        this.gameState = gameState;
        traceData = new TraceData();
        gameSummary = new GameSummary();
    }

    public void onFrame() {
        String frame = String.valueOf(gameState.frameCount);
        fillBoard(frame);
    }

    public void onUnitShow(Unit u) {
        String frame = String.valueOf(gameState.getIH().getFrameCount());
        UnitSeenInfo unitSeenInfo = new UnitSeenInfo(u);
        traceData.units_first_seen.computeIfAbsent(frame, s -> new ArrayList<>()).add(unitSeenInfo);
    }

    private void writeJSONCompressed(Object content, String path) {
        Gson aux = new GsonBuilder().create();
        String data = aux.toJson(content);
        byte[] compressedData = Zstd.compress(data.getBytes());
        try (OutputStream writer = new FileOutputStream(new File(path))) {
            writer.write(compressedData);
        } catch (IOException e) {
            System.err.println("writeJSONCompressed");
            e.printStackTrace();
        }
    }

    private void writeJSON(Object content, String path) {
        Gson aux = new GsonBuilder().create();
        try (FileWriter writer = new FileWriter(path)) {
            aux.toJson(content, writer);
        } catch (IOException e) {
            System.err.println("writeJSON");
            e.printStackTrace();
        }
    }

    private String getDumpDirectory(String opponentName) {
        int i = 1;
        while (i <= 100) {
            String path = dir + "/" + gameState.getPlayer().getName() + "_" + opponentName + ".cvis." + i + "/";
            File directory = new File(path);
            if (directory.exists()) {
                i++;
                continue;
            }
            boolean created = directory.mkdir();
            if (created) return path;
        }
        return null;
    }
    public void onEnd(boolean win, String startStrat) {
        File directory = new File(dir);
        boolean created = directory.exists();
        if (!created) created = directory.mkdir();
        if (!created) {
            System.err.println("Couldnt create CherryVis dump folder");
            return;
        }
        String opponentName = gameState.getIH().enemy().getName();
        String path = getDumpDirectory(opponentName);
        if (path != null) {
            fillGameSummary(win, startStrat);
            Util.getIH().sendText("Writing GameSummary to: " + path);
            writeJSON(gameSummary, path + "game_summary.json");
            Util.getIH().sendText("Writing traceData to: " + path);
            writeJSONCompressed(traceData, path + "trace.json");
        }
    }

    private void fillGameSummary(boolean win, String startStrat) {
        gameSummary.opening_bo = startStrat;
        gameSummary.final_bo = gameState.getStrat().name;
        gameSummary.game_duration_frames = gameState.getIH().getFrameCount();
        gameSummary.draw = gameState.getIH().getFrameCount() == 0;
        gameSummary.map = gameState.getGame().getBWMap().mapFileName();
        gameSummary.p0_name = gameState.getPlayer().getName();
        gameSummary.p0_race = gameState.getPlayer().getRace().toString();
        gameSummary.p0_win = win;
        gameSummary.p1_name = gameState.getIH().enemy().getName();
        gameSummary.p1_race = gameState.enemyRace.toString();
        gameSummary.p1_win = !win && gameState.getIH().getFrameCount() != 0;
    }

    private void fillBoard(String frame) {
        HashMap<String, String> board = traceData.board_updates.computeIfAbsent(frame, s -> new HashMap<>());
        board.put("chosenUpgrade", getStringObject(gameState.chosenUpgrade));
        board.put("chosenResearch", getStringObject(gameState.chosenResearch));
        board.put("chosenTrainingFacility", getStringUnitTypeUnit(gameState.chosenTrainingFacility));
        board.put("explore", getStringObject(gameState.explore));
        board.put("firstExpand", getStringObject(gameState.firstExpand));
        board.put("maxFirebats", getStringObject(gameState.maxBats));
        board.put("maxGoliaths", getStringObject(gameState.maxGoliaths));
        board.put("maxVessels", getStringObject(gameState.maxVessels));
        board.put("maxWraiths", getStringObject(gameState.maxWraiths));
        board.put("iReallyWantToExpand", getStringObject(gameState.iReallyWantToExpand));
        board.put("firstScout", getStringObject(gameState.firstScout));
        board.put("chosenToBuild", getStringObject(gameState.chosenToBuild));
        board.put("deltaCash", getStringObject(gameState.deltaCash));
        board.put("proxyBuilding", getStringUnitTypeUnit(gameState.proxyBuilding));
        board.put("islandExpand", getStringObject(gameState.islandExpand));
        board.put("APM", getStringObject(gameState.getIH().getFrameCount()));
        board.put("islandBases", getStringObject(gameState.islandBases.size()));
        // TODO more board variables

    }

    private String getStringObject(Object obj) {
        return Objects.toString(obj, "None");
    }

    private String getStringUnitTypeUnit(Unit unit) {
        return unit != null ? unit.getType().toString() : "None";
    }

}
