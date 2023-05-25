package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Factory;


public class ChooseVulture extends Action {

    public ChooseVulture(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (!gameState.Fs.isEmpty()) {
                if (gameState.getStrategyFromManager().name.equals("VultureRush") || Util.countUnitTypeSelf(UnitType.Terran_Vulture) * 2 <= Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Siege_Mode) + Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Tank_Mode) + 2) {
                    for (Factory b : gameState.Fs) {
                        if (!b.isTraining() && b.canTrain(UnitType.Terran_Vulture)) {
                            gameState.chosenUnit = UnitType.Terran_Vulture;
                            gameState.chosenTrainingFacility = b;
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
