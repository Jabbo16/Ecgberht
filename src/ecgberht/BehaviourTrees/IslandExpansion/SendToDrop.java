package ecgberht.BehaviourTrees.IslandExpansion;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.Worker;

import java.util.Collections;
import java.util.TreeSet;

public class SendToDrop extends Action {

    public SendToDrop(String name, GameState gh) {
        super(name, gh);

    }

    @Override
    public State execute() {
        try {
            if (this.handler.chosenDropShip != null && this.handler.chosenWorker != null) {
                Worker chosen = this.handler.chosenWorker;
                if (this.handler.workerIdle.contains(chosen)) {
                    this.handler.workerIdle.remove(chosen);
                } else if (this.handler.workerMining.containsKey(chosen)) {
                    MineralPatch mineral = this.handler.workerMining.get(chosen);
                    this.handler.workerMining.remove(chosen);
                    if (this.handler.mineralsAssigned.containsKey(mineral)) {
                        this.handler.mining--;
                        this.handler.mineralsAssigned.put(mineral, this.handler.mineralsAssigned.get(mineral) - 1);
                    }
                }
                this.handler.chosenDropShip.setCargo(new TreeSet<>(Collections.singletonList(this.handler.chosenWorker)));
                this.handler.chosenDropShip.setTarget(this.handler.chosenIsland.getLocation().toPosition());
                this.handler.chosenWorkerDrop = this.handler.chosenWorker;
                this.handler.chosenWorker = null;
                return State.SUCCESS;
            }
            this.handler.chosenDropShip = null;
            this.handler.chosenWorker = null;
            this.handler.chosenIsland = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}