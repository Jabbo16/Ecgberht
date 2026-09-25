package ecgberht.BehaviourTrees.Scanner;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.unit.ComsatStation;

public class Scan extends BrainAction {

    public Scan(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.checkScan == null) return BrainStatus.FAILURE;
            MutablePair<ComsatStation, Position> pair = gameState.checkScan;
            if (pair.first.getEnergy() >= 50 && pair.first.getOrder() != Order.CastScannerSweep && pair.first.scannerSweep(pair.second)) {
                gameState.startCount = gameState.getIH().getFrameCount();
                gameState.playSound("uav.mp3");
                gameState.checkScan = null;
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

