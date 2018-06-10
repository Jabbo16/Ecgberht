package ecgberht.Harass;

import bwapi.Unit;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

public class ChooseWorkerToHarass extends Action {

    public ChooseWorkerToHarass(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).chosenUnitToHarass != null && ((GameState) this.handler).chosenUnitToHarass.getType().isWorker()) {
                return State.FAILURE;
            }
            for (Unit u : ((GameState) this.handler).getGame().enemy().getUnits()) {
                if (((GameState) this.handler).enemyBase != null) {
                    if (u.getType().isWorker() && !u.isGatheringGas() && u.exists()) {
                        if (((GameState) this.handler).broodWarDistance(((GameState) this.handler).enemyBase.getPosition(), ((GameState) this.handler).chosenHarasser.getPosition()) <= 700) {
                            ((GameState) this.handler).chosenUnitToHarass = u;
                            return State.SUCCESS;
                        }
                    }
                }
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            System.err.println(e);
            return State.ERROR;
        }
    }
}
