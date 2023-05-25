package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Factory;


public class ChooseTank extends Action {

    public ChooseTank(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (!gameState.Fs.isEmpty()) {
                if (gameState.getStrategyFromManager().trainUnits.contains(UnitType.Terran_Wraith) &&
                        gameState.maxWraiths - Util.countUnitTypeSelf(UnitType.Terran_Wraith) > 0 && Math.random() * 10 <= 1) {
                    return State.FAILURE;
                }
                int multiplier = 2;
                String strat = gameState.getStrategyFromManager().name;
                if (strat.equals("JoyORush") && gameState.tanksTrained == 3 && Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Tank_Mode) == 3)
                    return State.FAILURE;
                if (strat.equals("FullMech") || strat.equals("MechGreedyFE")) multiplier = 15;
                if (Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Siege_Mode) + Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Tank_Mode) < Util.countUnitTypeSelf(UnitType.Terran_Marine) * multiplier) {
                    MutablePair<Integer, Integer> cash = gameState.getCash();
                    if (cash.second < (UnitType.Terran_Siege_Tank_Tank_Mode.gasPrice())) return State.FAILURE;
                    for (Factory b : gameState.Fs) {
                        if (!b.isTraining() && b.canTrain(UnitType.Terran_Siege_Tank_Tank_Mode)) {
                            gameState.chosenUnit = UnitType.Terran_Siege_Tank_Tank_Mode;
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
