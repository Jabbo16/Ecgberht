package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.Worker;

public class Move extends Action {

    public Move(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Worker chosen = gameState.chosenWorker;
            Position realEnd = Util.getUnitCenterPosition(gameState.chosenPosition.toPosition(), gameState.chosenToBuild);
            if (chosen.move(realEnd)) {
                if (gameState.workerIdle.contains(chosen)) gameState.workerIdle.remove(chosen);
                else if (gameState.workerMining.containsKey(chosen)) {
                    MineralPatch mineral = gameState.workerMining.get(chosen);
                    gameState.workerMining.remove(chosen);
                    if (gameState.mineralsAssigned.containsKey(mineral)) {
                        gameState.mining--;
                        gameState.mineralsAssigned.put(mineral, gameState.mineralsAssigned.get(mineral) - 1);
                    }
                }
                if (gameState.chosenToBuild == UnitType.Terran_Command_Center
                        && gameState.bwem.getMap().getArea(gameState.chosenPosition).equals(gameState.naturalArea)
                        && gameState.naturalChoke != null) {
                    gameState.defendPosition = gameState.naturalChoke.getCenter().toPosition();
                }
                gameState.workerBuild.put((SCV) chosen, new MutablePair<>(gameState.chosenToBuild, gameState.chosenPosition));
                gameState.deltaCash.first += gameState.chosenToBuild.mineralPrice();
                gameState.deltaCash.second += gameState.chosenToBuild.gasPrice();
                gameState.chosenWorker = null;
                gameState.chosenToBuild = UnitType.None;
                return State.SUCCESS;
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
