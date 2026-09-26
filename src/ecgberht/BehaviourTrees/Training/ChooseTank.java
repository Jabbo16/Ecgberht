package ecgberht.BehaviourTrees.Training;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Factory;


public class ChooseTank extends BrainAction {

    public ChooseTank(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (!gameState.tech.Fs.isEmpty()) {
                if (gameState.getStrat().trainUnits.contains(UnitType.Terran_Wraith) &&
                        gameState.maxWraiths - Util.countUnitTypeSelf(UnitType.Terran_Wraith) > 0 && Math.random() * 10 <= 1) {
                    return BrainStatus.FAILURE;
                }
                int multiplier = 2;
                String strat = gameState.getStrat().name;
                if (strat.equals("JoyORush") && gameState.mil.tanksTrained == 3 && Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Tank_Mode) == 3)
                    return BrainStatus.FAILURE;
                if (strat.equals("FullMech") || strat.equals("MechGreedyFE")) multiplier = 15;
                if (Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Siege_Mode) + Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Tank_Mode) < Util.countUnitTypeSelf(UnitType.Terran_Marine) * multiplier) {
                    MutablePair<Integer, Integer> cash = gameState.getCash();
                    if (cash.second < (UnitType.Terran_Siege_Tank_Tank_Mode.gasPrice())) return BrainStatus.FAILURE;
                    for (Factory b : gameState.tech.Fs) {
                        if (!b.isTraining() && b.canTrain(UnitType.Terran_Siege_Tank_Tank_Mode)) {
                            gameState.chosenUnit = UnitType.Terran_Siege_Tank_Tank_Mode;
                            gameState.chosenTrainingFacility = b;
                            return BrainStatus.SUCCESS;
                        }
                    }
                }
            }
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}






