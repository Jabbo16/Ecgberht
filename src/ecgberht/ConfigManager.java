package ecgberht;

import com.google.gson.Gson;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigManager {

    private static Config config = new Config();

    static void readConfig() {
        Gson configJSON = new Gson();
        String path = "bwapi-data/AI/config.json";
        try {
            if (Files.exists(Paths.get(path))) {
                config = configJSON.fromJson(new FileReader(path), Config.class);
                return;
            }
            path = "bwapi-data/read/config.json";
            if (Files.exists(Paths.get(path))) {
                config = configJSON.fromJson(new FileReader(path), Config.class);
                return;
            }
            path = "config.json";
            if (Files.exists(Paths.get(path))) {
                config = configJSON.fromJson(new FileReader(path), Config.class);
                return;
            }
            config = new Config();
        } catch (Exception e) {
            System.err.println("readConfig Exception");
            e.printStackTrace();
            config = new Config();
        }
    }

    public static Config getConfig() {
        return config;
    }

    public static class Config {

        public EcgberhtConfig ecgConfig = new EcgberhtConfig();
        BwapiConfig bwapiConfig = new BwapiConfig();

        public static class EcgberhtConfig {
            public boolean sscait = true;
            boolean debugConsole = false;
            boolean debugScreen = false;
            boolean debugText = false;
            boolean sounds = false;
            boolean enableLatCom  = true;
            boolean enableSkyCladObserver = false;
            String forceStrat = "";

        }

        static class BwapiConfig {
            int localSpeed = 42;
            int frameSkip = -1;
            boolean userInput = true;
            boolean completeMapInformation = false;

        }
    }
}

