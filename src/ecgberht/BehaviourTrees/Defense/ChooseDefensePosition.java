package ecgberht.BehaviourTrees.Defense;

import ecgberht.GameState;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.*;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.*;

public class ChooseDefensePosition extends BrainAction {

    public ChooseDefensePosition(String name, GameState gh) {
        super(name, gh);
    }

    private Position getDefensePosition() {
        if (gameState.defendPosition != null) return gameState.defendPosition;
        if (gameState.initDefensePosition != null)
            return gameState.initDefensePosition.toPosition();
        if (gameState.mainChoke != null)
            return gameState.mainChoke.getCenter().toPosition();
        return gameState.getPlayer().getStartLocation().toPosition();
    }

    private Position chooseDefensePosition() {
        Position chosen = null;
        double maxScore = 0;
        for (Unit b : gameState.enemyInBase) {
            double influence = getScore(b);
            double dist = Util.getGroundDistance(getDefensePosition(), b.getPosition());
            if (dist == Integer.MAX_VALUE) continue;
            double score = influence / (2.5 * dist);
            if (score > maxScore) {
                chosen = b.getPosition();
                maxScore = score;
            }
        }
        return chosen;
    }

    private double getScore(Unit unit) {
        if (unit instanceof Building && unit.getType().canAttack() || unit instanceof Bunker) return 6;
        else if (unit instanceof Building) return 5;
        else if (unit instanceof Organic) return 2;
        else if (unit instanceof Worker) return 1;
        else if (unit instanceof Mechanical) return 3;
        else if (unit instanceof Transporter) return 4;
        return 1;
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.defense) {
                Position chosenDefensePosition = chooseDefensePosition();
                if (chosenDefensePosition != null) {
                    gameState.attackPosition = chosenDefensePosition;
                    return BrainStatus.SUCCESS;
                }
            }
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}

