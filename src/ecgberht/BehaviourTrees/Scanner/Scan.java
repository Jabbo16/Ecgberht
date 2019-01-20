package ecgberht.BehaviourTrees.Scanner;

import bwapi.Order;
import bwapi.TechType;
import bwapi.Unit;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;

public class Scan extends Action {

    public Scan(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            for (Unit u : gameState.CSs) {
                if (u.getEnergy() >= 50 && u.getOrder() != Order.CastScannerSweep && u.useTech(TechType.Scanner_Sweep, gameState.checkScan.toPosition())) {
                    gameState.startCount = gameState.bw.getFrameCount();
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
