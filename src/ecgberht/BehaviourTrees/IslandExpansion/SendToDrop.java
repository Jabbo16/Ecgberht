package ecgberht.BehaviourTrees.IslandExpansion;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.Worker;

import java.util.Collections;
import java.util.TreeSet;

public class SendToDrop extends Action {

    public SendToDrop(String name, GameHandler gh) {
        super(name, gh);

    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).chosenDropShip != null && ((GameState) this.handler).chosenWorker != null) {
                Worker chosen = ((GameState) this.handler).chosenWorker;
                if (((GameState) this.handler).workerIdle.contains(chosen)) {
                    ((GameState) this.handler).workerIdle.remove(chosen);
                } else {
                    if (((GameState) this.handler).workerMining.containsKey(chosen)) {
                        MineralPatch mineral = ((GameState) this.handler).workerMining.get(chosen);
                        ((GameState) this.handler).workerMining.remove(chosen);
                        if (((GameState) this.handler).mineralsAssigned.containsKey(mineral)) {
                            ((GameState) this.handler).mining--;
                            ((GameState) this.handler).mineralsAssigned.put(mineral, ((GameState) this.handler).mineralsAssigned.get(mineral) - 1);
                        }
                    }
                }
                ((GameState) this.handler).chosenDropShip.setCargo(new TreeSet<>(Collections.singletonList(((GameState) this.handler).chosenWorker)));
                ((GameState) this.handler).chosenDropShip.setTarget(((GameState) this.handler).chosenIsland.getLocation().toPosition(), true);
                ((GameState) this.handler).chosenWorkerDrop = ((GameState) this.handler).chosenWorker;
                ((GameState) this.handler).chosenWorker = null;
                return State.SUCCESS;
            }
            ((GameState) this.handler).chosenDropShip = null;
            ((GameState) this.handler).chosenWorker = null;
            ((GameState) this.handler).chosenIsland = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}