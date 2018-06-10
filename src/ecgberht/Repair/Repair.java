package ecgberht.Repair;

import bwapi.Pair;
import bwapi.Unit;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

public class Repair extends Action {

    public Repair(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).chosenRepairer.repair(((GameState) this.handler).chosenBuildingRepair)) {
                if (((GameState) this.handler).workerIdle.contains(((GameState) this.handler).chosenRepairer)) {
                    ((GameState) this.handler).workerIdle.remove(((GameState) this.handler).chosenRepairer);
                } else {
                    if (((GameState) this.handler).workerMining.containsKey(((GameState) this.handler).chosenRepairer)) {
                        Unit mineral = ((GameState) this.handler).workerMining.get(((GameState) this.handler).chosenRepairer);
                        ((GameState) this.handler).workerMining.remove(((GameState) this.handler).chosenRepairer);
                        if (((GameState) this.handler).mineralsAssigned.containsKey(mineral)) {
                            ((GameState) this.handler).mining--;
                            ((GameState) this.handler).mineralsAssigned.put(mineral, ((GameState) this.handler).mineralsAssigned.get(mineral) - 1);
                        }
                    }
                }
                ((GameState) this.handler).repairerTask.add(new Pair<Unit, Unit>(((GameState) this.handler).chosenRepairer, ((GameState) this.handler).chosenBuildingRepair));
                return State.SUCCESS;
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            System.err.println(e);
            return State.ERROR;
        }
    }
}
