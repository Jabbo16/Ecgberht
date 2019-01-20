package ecgberht.BehaviourTrees.Scanner;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.unit.ComsatStation;

public class Scan extends Action {

    public Scan(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if(gameState.checkScan == null) return State.FAILURE;
            MutablePair<ComsatStation, Position> pair = gameState.checkScan;
            if (pair.first.getEnergy() >= 50 && pair.first.getOrder() != Order.CastScannerSweep && pair.first.scannerSweep(pair.second)) {
                gameState.startCount = gameState.getIH().getFrameCount();
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
