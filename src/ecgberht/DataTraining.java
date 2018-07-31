package ecgberht;

import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.SCV;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.TreeMap;

public class DataTraining {
    public static Map<SCV, TravelData> travelData = new TreeMap<>();

    public static void copyOnStart() {
        File yourFile = new File("bwapi-data/write/travelData.txt");
        try {
            yourFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Path copied = Paths.get("bwapi-data/write/travelData.txt");
        Path originalPath = Paths.get("bwapi-data/read/travelData.txt");
        if (!Files.exists(originalPath)) return;
        try {
            Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void writeTravelData() {
        try {
            for (TravelData t : travelData.values()) {
                Files.write(Paths.get("bwapi-data/write/travelData.txt"), t.toString().getBytes(), StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            System.err.println("writeTravelData Exception");
            e.printStackTrace();
        }
    }

    public static class TravelData {
        int x1;
        int y1;
        int x2;
        int y2;
        double vx;
        double vy;
        double distance;
        int frames;

        public TravelData(Position p1, Position p2, double vx, double vy, double distance, int frames) {
            this.x1 = p1.getX();
            this.y1 = p1.getY();
            this.x2 = p2.getX();
            this.y2 = p2.getY();
            this.vx = vx;
            this.vy = vy;
            this.distance = distance;
            this.frames = frames;
        }

        @Override
        public String toString() {
            return Integer.toString(x1) + "," + Integer.toString(y1) + "," + Integer.toString(x2) + "," +
                    Integer.toString(y2) + "," + Double.toString(vx) + "," + Double.toString(vy) + "," + Double.toString(distance) + "," +
                    Integer.toString(frames) + "\n";
        }
    }
}
