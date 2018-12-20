package ecgberht.BehaviourTrees.Build;

import bwem.area.Area;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.Worker;

public class ChooseWorker extends Action {

    public ChooseWorker(String name, GameState gh) {
        super(name, gh);

    }

    @Override
    public State execute() {
        try {
            Worker closestWorker = null;
            int frame = this.handler.frameCount;
            Position chosen = this.handler.chosenPosition.toPosition();
            Area posArea = this.handler.bwem.getMap().getArea(this.handler.chosenPosition);
            if (!this.handler.workerIdle.isEmpty()) {
                for (Worker u : this.handler.workerIdle) {
                    if (u.getLastCommandFrame() == frame) continue;
                    Area workerArea = this.handler.bwem.getMap().getArea(u.getTilePosition());
                    if (workerArea == null) continue;
                    if (posArea != null && !posArea.equals(workerArea) && !posArea.isAccessibleFrom(workerArea)) {
                        continue;
                    }
                    if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen)))
                        closestWorker = u;
                }
            }
            if (!this.handler.workerMining.isEmpty()) {
                for (Worker u : this.handler.workerMining.keySet()) {
                    if (u.getLastCommandFrame() == frame) continue;
                    Area workerArea = this.handler.bwem.getMap().getArea(u.getTilePosition());
                    if (workerArea == null) continue;
                    if (posArea != null && !posArea.equals(workerArea) && !posArea.isAccessibleFrom(workerArea)) {
                        continue;
                    }
                    if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen)) && !u.isCarryingMinerals()) {
                        closestWorker = u;
                    }
                }
            }
            if (closestWorker != null) {
                this.handler.chosenWorker = closestWorker;
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