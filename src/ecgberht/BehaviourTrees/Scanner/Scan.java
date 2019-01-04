package ecgberht.BehaviourTrees.Scanner;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.unit.ComsatStation;

public class Scan extends Action {

    public Scan(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            for (ComsatStation u : gameState.CSs) {
                if (u.getEnergy() >= 50 && u.getOrder() != Order.CastScannerSweep && u.scannerSweep(gameState.checkScan.toPosition())) {
                    gameState.startCount = gameState.getIH().getFrameCount();
                    gameState.playSound("uav.mp3");
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
