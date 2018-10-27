package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;
import org.openbw.bwapi4j.type.UnitType;

public class CheckResourcesUnit extends Conditional {

    public CheckResourcesUnit(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            MutablePair<Integer, Integer> cash = this.handler.getCash();
            if (cash.first >= (this.handler.chosenUnit.mineralPrice() + this.handler.deltaCash.first) && cash.second >= (this.handler.chosenUnit.gasPrice()) + this.handler.deltaCash.second) {
                return State.SUCCESS;
            }
            this.handler.chosenBuilding = null;
            this.handler.chosenToBuild = UnitType.None;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
