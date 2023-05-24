package ecgberht;

import java.util.LinkedList;

import bwapi.Race;
import ecgberht.Util.Util;

public class EnemyHistory {
    public LinkedList<EnemyGame> history = new LinkedList<>();

    static class EnemyGame {
        private String opponent;
        private String race;
        private String outcome;
        private String strategy;
        private String mapName;
        public String opponentStrategy;

        EnemyGame(String opponent, org.openbw.bwapi4j.type.Race enemyRace, boolean outcome, String strategy, String mapName, IntelligenceAgency.EnemyStrats enemyStrat) {
            this.opponent = opponent;
            this.race = Util.raceToString(enemyRace);
            this.outcome = outcome ? "Win" : "Lose";
            this.strategy = strategy;
            this.mapName = mapName;
            this.opponentStrategy = enemyStrat.toString();
        }
    }
}
