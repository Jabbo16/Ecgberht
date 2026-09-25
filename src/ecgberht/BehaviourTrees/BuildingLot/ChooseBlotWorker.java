package ecgberht.BehaviourTrees.BuildingLot;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.Worker;

import java.util.Map.Entry;

public class ChooseBlotWorker extends BrainAction {

    public ChooseBlotWorker(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            Worker closestWorker = null;
            Position chosen = gameState.chosenBuildingLot.getPosition();
            if (!gameState.workerIdle.isEmpty()) {
                for (Worker u : gameState.workerIdle) {
                    if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
                        closestWorker = u;
                    }
                }
            }
            if (!gameState.workerMining.isEmpty()) {
                for (Entry<Worker, MineralPatch> u : gameState.workerMining.entrySet()) {
                    if ((closestWorker == null || u.getKey().getDistance(chosen) < closestWorker.getDistance(chosen)) && !u.getKey().isCarryingMinerals()) {
                        closestWorker = u.getKey();
                    }
                }
            }
            if (closestWorker != null) {
                gameState.chosenWorker = closestWorker;
                return BrainStatus.SUCCESS;
            }
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}

