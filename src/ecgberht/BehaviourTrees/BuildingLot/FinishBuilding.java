package ecgberht.BehaviourTrees.BuildingLot;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.Worker;

public class FinishBuilding extends Action {

    public FinishBuilding(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            Worker chosen = this.handler.chosenWorker;
            if (chosen.rightClick(this.handler.chosenBuildingLot, false)) {
                if (this.handler.workerIdle.contains(chosen)) {
                    this.handler.workerIdle.remove(chosen);
                } else {
                    if (this.handler.workerMining.containsKey(chosen)) {
                        MineralPatch mineral = this.handler.workerMining.get(chosen);
                        this.handler.workerMining.remove(chosen);
                        if (this.handler.mineralsAssigned.containsKey(mineral)) {
                            this.handler.mining--;
                            this.handler.mineralsAssigned.put(mineral, this.handler.mineralsAssigned.get(mineral) - 1);
                        }
                    }
                }
                this.handler.workerTask.put((SCV) chosen, this.handler.chosenBuildingLot);
                this.handler.chosenWorker = null;
                this.handler.buildingLot.remove(this.handler.chosenBuildingLot);
                this.handler.chosenBuildingLot = null;
                return BehavioralTree.State.SUCCESS;
            }
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}
