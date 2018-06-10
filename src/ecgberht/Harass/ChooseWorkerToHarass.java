package ecgberht.Harass;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.unit.Worker;

public class ChooseWorkerToHarass extends Action {

    public ChooseWorkerToHarass(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).chosenUnitToHarass != null && ((GameState) this.handler).chosenUnitToHarass instanceof Worker) {
                return State.FAILURE;
            }
            for (Unit u : ((GameState) this.handler).getGame().getUnits(((GameState) this.handler).getIH().enemy())) {
                if (((GameState) this.handler).enemyBase != null) {
                    if (u instanceof Worker && !((Worker) u).isGatheringGas() && u.exists()) {
                        if (((Worker) u).getOrder() != Order.Move) continue;
                        if (((GameState) this.handler).broodWarDistance(((GameState) this.handler).enemyBase.getLocation().toPosition(), ((GameState) this.handler).chosenHarasser.getPosition()) <= 700) {
                            ((GameState) this.handler).chosenUnitToHarass = u;
                            return State.SUCCESS;
                        }
                    }
                }
            }
            ((GameState) this.handler).chosenUnitToHarass = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
