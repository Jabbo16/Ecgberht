package ecgberht.BehaviourTrees.Defense;

import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.*;

public class ChooseDefensePosition extends Conditional {

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
            //double score = influence / (2 * getEuclideanDist(p, b.pos.toPosition()));
            double score = influence / (2.5 * Util.getGroundDistance(getDefensePosition(), b.getPosition()));
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
    public State execute() {
        try {
            if (gameState.defense) {
                Position chosenDefensePosition = chooseDefensePosition();
                if (chosenDefensePosition != null) {
                    gameState.attackPosition = chosenDefensePosition;
                    return State.SUCCESS;
                }
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
