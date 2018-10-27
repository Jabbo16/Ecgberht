package ecgberht.BehaviourTrees.Scanner;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.unit.ComsatStation;

public class Scan extends Action {

    public Scan(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            for (ComsatStation u : this.handler.CSs) {
                if (u.getEnergy() >= 50 && u.getOrder() != Order.CastScannerSweep) {
                    if (u.scannerSweep(this.handler.checkScan.toPosition())) {
                        this.handler.startCount = this.handler.getIH().getFrameCount();
                        this.handler.playSound("uav.mp3");
                        return BehavioralTree.State.SUCCESS;
                    }
                }
            }
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}
