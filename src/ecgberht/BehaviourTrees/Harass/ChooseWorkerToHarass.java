package ecgberht.BehaviourTrees.Harass;

import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.unit.Worker;

public class ChooseWorkerToHarass extends Action {

    public ChooseWorkerToHarass(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (this.handler.chosenUnitToHarass != null && this.handler.chosenUnitToHarass instanceof Worker) {
                return State.FAILURE;
            }
            for (Unit u : this.handler.getGame().getUnits(this.handler.getIH().enemy())) {
                if (this.handler.enemyMainBase != null) {
                    if (u instanceof Worker && !((Worker) u).isGatheringGas() && u.exists()) {
                        if (((Worker) u).getOrder() != Order.Move) continue;
                        if (Util.broodWarDistance(this.handler.enemyMainBase.getLocation().toPosition(), this.handler.chosenHarasser.getPosition()) <= 700) {
                            this.handler.chosenUnitToHarass = u;
                            return State.SUCCESS;
                        }
                    }
                }
            }
            this.handler.chosenUnitToHarass = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
