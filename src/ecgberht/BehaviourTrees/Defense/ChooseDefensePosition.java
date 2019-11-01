package ecgberht.BehaviourTrees.Defense;

import bwapi.Unit;
import bwapi.UnitType;
import ecgberht.GameState;
import ecgberht.UnitInfo;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;
import bwapi.Position;

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
        for (UnitInfo b : gameState.enemyInBase) {
            double influence = getScore(b.unitType);
            //double score = influence / (2 * getEuclideanDist(p, b.pos.toPosition()));
            double score = influence / (2.5 * Util.getGroundDistance(getDefensePosition(), b.position));
            if (score > maxScore) {
                chosen = b.position;
                maxScore = score;
            }
        }
        return chosen;
    }

    private double getScore(UnitType type) {
        if (type.isBuilding() && (type.canAttack() || type == UnitType.Terran_Bunker)) return 6;
        else if (type.isBuilding()) return 5;
        else if (type.isOrganic()) return 2;
        else if (type.isWorker()) return 1;
        else if (type.isMechanical()) return 3;
        else if (type.spaceProvided() > 0) return 4;
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
