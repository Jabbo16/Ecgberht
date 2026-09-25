package ecgberht.BehaviourTrees.Recollection;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.unit.Worker;

public class FreeWorker extends BrainAction {

    public FreeWorker(String name, GameState gh) {
        super(name, gh);

    }

    @Override
    public BrainStatus execute() {
        try {
            gameState.chosenWorker = null;
            if (!gameState.workerIdle.isEmpty()) {
                int frame = gameState.frameCount;
                for (Worker w : gameState.workerIdle) {
                    if (w.getLastCommandFrame() != frame) {
                        gameState.chosenWorker = w;
                        return BrainStatus.SUCCESS;
                    }
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
