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
            if (!this.handler.Fs.isEmpty()) {
                if (this.handler.strat.trainUnits.contains(UnitType.Terran_Wraith) &&
                        this.handler.maxWraiths - Util.countUnitTypeSelf(UnitType.Terran_Wraith) > 0 && Math.random() * 10 <= 1) {
                    return State.FAILURE;
                }
                int multiplier = 2;
                String strat = this.handler.strat.name;
                if (strat.equals("FullMech") || strat.equals("MechGreedyFE")) multiplier = 15;
                if (Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Siege_Mode) + Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Tank_Mode) < Util.countUnitTypeSelf(UnitType.Terran_Marine) * multiplier) {
                    MutablePair<Integer, Integer> cash = this.handler.getCash();
                    if (cash.second < (UnitType.Terran_Siege_Tank_Tank_Mode.gasPrice())) {
                        return State.FAILURE;
                    }
                    for (Factory b : this.handler.Fs) {
                        if (!b.isTraining() && b.canTrain(UnitType.Terran_Siege_Tank_Tank_Mode)) {
                            this.handler.chosenUnit = UnitType.Terran_Siege_Tank_Tank_Mode;
                            this.handler.chosenBuilding = b;
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
