package ecgberht.BehaviourTrees.Repair;
import ecgberht.brain.*;


import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.Worker;

public class ChooseRepairer extends BrainAction {

    public ChooseRepairer(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            SCV closestWorker = null;
            Position chosen = gameState.chosenUnitRepair.getPosition();
            int frame = gameState.frameCount;
            for (Worker u : gameState.workerIdle) {
                if (u.getLastCommandFrame() == frame) continue;
                if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
                    closestWorker = (SCV) u;
                }
            }
            for (Worker u : gameState.workerMining.keySet()) {
                if (u.getLastCommandFrame() == frame) continue;
                if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen)) && !u.isCarryingMinerals()) {
                    closestWorker = (SCV) u;
                }
            }
            if (closestWorker != null) {
                gameState.chosenRepairer = closestWorker;
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
