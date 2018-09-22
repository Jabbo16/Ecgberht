package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.Worker;

public class Move extends Action {

    public Move(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Worker chosen = ((GameState) this.handler).chosenWorker;
            Position realEnd = Util.getUnitCenterPosition(((GameState) this.handler).chosenPosition.toPosition(), ((GameState) this.handler).chosenToBuild);
            if (chosen.move(realEnd)) {
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
                if (((GameState) this.handler).chosenToBuild == UnitType.Terran_Command_Center
                        && ((GameState) this.handler).bwem.getMap().getArea(((GameState) this.handler).chosenPosition).equals(((GameState) this.handler).naturalArea)
                        && ((GameState) this.handler).naturalChoke != null) {
                    ((GameState) this.handler).defendPosition = ((GameState) this.handler).naturalChoke.getCenter().toPosition();
                }
                ((GameState) this.handler).workerBuild.put((SCV) chosen, new MutablePair<>(((GameState) this.handler).chosenToBuild, ((GameState) this.handler).chosenPosition));
                ((GameState) this.handler).deltaCash.first += ((GameState) this.handler).chosenToBuild.mineralPrice();
                ((GameState) this.handler).deltaCash.second += ((GameState) this.handler).chosenToBuild.gasPrice();
                ((GameState) this.handler).chosenWorker = null;
                ((GameState) this.handler).chosenToBuild = UnitType.None;
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
