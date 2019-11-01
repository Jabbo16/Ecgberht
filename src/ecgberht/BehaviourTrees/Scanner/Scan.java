package ecgberht.BehaviourTrees.Scanner;

import bwapi.Order;
import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;

public class Scan extends Action {

    public Scan(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.checkScan == null) return State.FAILURE;
            MutablePair<Unit, Position> pair = gameState.checkScan;
            if (pair.first.getEnergy() >= 50 && pair.first.getOrder() != Order.CastScannerSweep && pair.first.useTech(TechType.Scanner_Sweep, pair.second)) {
                gameState.startCount = gameState.frameCount;
                gameState.playSound("uav.mp3");
                gameState.checkScan = null;
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
