package ecgberht.BuildingLot;

import bwapi.Pair;
import bwapi.Unit;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

public class FinishBuilding extends Action {

    public FinishBuilding(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Unit chosen = ((GameState) this.handler).chosenWorker;
            if (chosen.rightClick(((GameState) this.handler).chosenBuildingLot)) {
                if (((GameState) this.handler).workerIdle.contains(chosen)) {
                    ((GameState) this.handler).workerIdle.remove(chosen);
                } else {
                    if (((GameState) this.handler).workerMining.containsKey(chosen)) {
                        Unit mineral = ((GameState) this.handler).workerMining.get(chosen);
                        ((GameState) this.handler).workerMining.remove(chosen);
                        if (((GameState) this.handler).mineralsAssigned.containsKey(mineral)) {
                            ((GameState) this.handler).mining--;
                            ((GameState) this.handler).mineralsAssigned.put(mineral, ((GameState) this.handler).mineralsAssigned.get(mineral) - 1);
                        }
                    }
                }
                ((GameState) this.handler).workerTask.add(new Pair<Unit, Unit>(chosen, ((GameState) this.handler).chosenBuildingLot));
                ((GameState) this.handler).chosenWorker = null;
                ((GameState) this.handler).buildingLot.remove(((GameState) this.handler).chosenBuildingLot);
                ((GameState) this.handler).chosenBuildingLot = null;
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
