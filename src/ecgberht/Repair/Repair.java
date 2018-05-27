package ecgberht.Repair;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.unit.Mechanical;
import org.openbw.bwapi4j.unit.MineralPatch;

public class Repair extends Action {

    public Repair(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).chosenRepairer.repair((Mechanical) ((GameState) this.handler).chosenBuildingRepair)) {
                if (((GameState) this.handler).workerIdle.contains(((GameState) this.handler).chosenRepairer)) {
                    ((GameState) this.handler).workerIdle.remove(((GameState) this.handler).chosenRepairer);
                } else {
                    if (((GameState) this.handler).workerMining.containsKey(((GameState) this.handler).chosenRepairer)) {
                        MineralPatch mineral = ((GameState) this.handler).workerMining.get(((GameState) this.handler).chosenRepairer);
                        ((GameState) this.handler).workerMining.remove(((GameState) this.handler).chosenRepairer);
                        if (((GameState) this.handler).mineralsAssigned.containsKey(mineral)) {
                            ((GameState) this.handler).mining--;
                            ((GameState) this.handler).mineralsAssigned.put(mineral, ((GameState) this.handler).mineralsAssigned.get(mineral) - 1);
                        }
                    }
                }
                ((GameState) this.handler).repairerTask.put(((GameState) this.handler).chosenRepairer, ((GameState) this.handler).chosenBuildingRepair);
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
