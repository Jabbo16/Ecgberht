package ecgberht;

import ecgberht.Util.Util;
import org.openbw.bwapi4j.type.Race;

import java.util.ArrayList;
import java.util.List;

public class EnemyHistory {
    public List<EnemyGame> history = new ArrayList<>();

    public static class EnemyGame {
        private String opponent;
        private String race;
        private String outcome;
        private String strategy;
        private String mapName;

        public EnemyGame(String opponent, Race race, boolean outcome, String strategy, String mapName) {
            this.opponent = opponent;
            this.race = Util.raceToString(race);
            this.outcome = outcome ? "Win" : "Lose";
            this.strategy = strategy;
            this.mapName = mapName;
        }
    }


}
