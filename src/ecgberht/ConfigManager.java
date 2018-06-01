package ecgberht;

import com.google.gson.Gson;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigManager {


    private static Config config;

    public static void readConfig() {
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
            return;
        } catch (Exception e) {
            System.err.println("readConfig Exception");
            e.printStackTrace();
            config = new Config();
            return;
        }
    }

    public static Config getConfig() {
        return config;
    }

    public static class Config {

        public EcgberhtConfig ecgConfig = new EcgberhtConfig();
        public BwapiConfig bwapiConfig = new BwapiConfig();

        public Config() {
        }

        public static class EcgberhtConfig {
            public boolean debugConsole;
            public boolean debugScreen;
            public boolean debugText;
            public boolean sounds;
            public boolean enableLatCom;

            public EcgberhtConfig() {
            }
        }

        public static class BwapiConfig {
            public int localSpeed = 42;
            public int frameSkip;
            public boolean userInput = true;
            public boolean completeMapInformation;

            public BwapiConfig() {
            }
        }
    }
}

