package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Starport;


public class ChooseWraith extends Action {

    public ChooseWraith(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (!gameState.Ps.isEmpty()) {
                if (Util.countUnitTypeSelf(UnitType.Terran_Wraith) <= gameState.maxWraiths) {
                    for (Starport b : gameState.Ps) {
                        if (!b.isTraining() && b.canTrain(UnitType.Terran_Wraith)) {
                            gameState.chosenUnit = UnitType.Terran_Wraith;
                            gameState.chosenBuilding = b;
                            return State.SUCCESS;
                        }
                    }
                }
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
