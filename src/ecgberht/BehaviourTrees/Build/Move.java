package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.Worker;

public class Move extends Action {

    public Move(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            Worker chosen = this.handler.chosenWorker;
            Position realEnd = Util.getUnitCenterPosition(this.handler.chosenPosition.toPosition(), this.handler.chosenToBuild);
            if (chosen.move(realEnd)) {
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
                if (this.handler.chosenToBuild == UnitType.Terran_Command_Center
                        && this.handler.bwem.getMap().getArea(this.handler.chosenPosition).equals(this.handler.naturalArea)
                        && this.handler.naturalChoke != null) {
                    this.handler.defendPosition = this.handler.naturalChoke.getCenter().toPosition();
                }
                this.handler.workerBuild.put((SCV) chosen, new MutablePair<>(this.handler.chosenToBuild, this.handler.chosenPosition));
                this.handler.deltaCash.first += this.handler.chosenToBuild.mineralPrice();
                this.handler.deltaCash.second += this.handler.chosenToBuild.gasPrice();
                this.handler.chosenWorker = null;
                this.handler.chosenToBuild = UnitType.None;
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
