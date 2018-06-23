package ecgberht.Build;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.Worker;
import org.openbw.bwapi4j.util.Pair;

public class Move extends Action {

    public Move(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Worker chosen = ((GameState) this.handler).chosenWorker;
            boolean success;
            /*if (((GameState) this.handler).chosenToBuild == UnitType.Terran_Refinery) {
                success = chosen.build(((GameState) this.handler).chosenPosition, ((GameState) this.handler).chosenToBuild);
            } else {
                Position realEnd = ((GameState) this.handler).getCenterFromBuilding(((GameState) this.handler).chosenPosition.toPosition(), ((GameState) this.handler).chosenToBuild);
                success = chosen.move(realEnd);
            }*/
            Position realEnd = ((GameState) this.handler).getCenterFromBuilding(((GameState) this.handler).chosenPosition.toPosition(), ((GameState) this.handler).chosenToBuild);
            success = chosen.move(realEnd);
            if (success) {
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
                if (((GameState) this.handler).chosenToBuild == UnitType.Terran_Command_Center) {
                    if (((GameState) this.handler).bwem.getMap().getArea(((GameState) this.handler).chosenPosition).equals(((GameState) this.handler).naturalRegion)) {
                        ((GameState) this.handler).defendPosition = ((GameState) this.handler).naturalChoke.getCenter().toPosition();
                    }
                }
                ((GameState) this.handler).workerBuild.put((SCV) chosen, new Pair<>(((GameState) this.handler).chosenToBuild, ((GameState) this.handler).chosenPosition));
                ((GameState) this.handler).deltaCash.first += ((GameState) this.handler).chosenToBuild.mineralPrice();
                ((GameState) this.handler).deltaCash.second += ((GameState) this.handler).chosenToBuild.gasPrice();
                ((GameState) this.handler).chosenWorker = null;
                ((GameState) this.handler).chosenToBuild = null;
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
