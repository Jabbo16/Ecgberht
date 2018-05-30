package ecgberht;

import com.google.gson.Gson;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigManager {

    public static class EcgberhtConfig {
        public boolean debugConsole;
        public boolean debugScreen;
        public boolean debugText;
        public boolean sounds;
        public boolean enableLatCom;

        public EcgberhtConfig() { }
    }

    private static EcgberhtConfig config;

    public static void readConfig() {
        Gson configJSON = new Gson();
        String path = "bwapi-data/AI/config.json";
        try {
            if (Files.exists(Paths.get(path))) {
                config = configJSON.fromJson(new FileReader(path), EcgberhtConfig.class);
                return;
            }
            path = "bwapi-data/read/config.json";
            if (Files.exists(Paths.get(path))) {
                config = configJSON.fromJson(new FileReader(path), EcgberhtConfig.class);
                return;
            }
            path = "config.json";
            if (Files.exists(Paths.get(path))) {
                config = configJSON.fromJson(new FileReader(path), EcgberhtConfig.class);
                return;
            }
            config = new EcgberhtConfig();
            return;
        } catch (Exception e) {
            System.err.println("readConfig Exception");
            e.printStackTrace();
            config = new EcgberhtConfig();
            return;
        }
    }

    public static EcgberhtConfig getConfig() {
        return config;
    }
}

