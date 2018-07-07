package ecgberht.Training;

import ecgberht.GameState;
import ecgberht.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Factory;
import org.openbw.bwapi4j.util.Pair;


public class ChooseTank extends Action {

    public ChooseTank(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (!((GameState) this.handler).Fs.isEmpty()) {
                /*if (((GameState) this.handler).siegeResearched) {
                    if (!((GameState) this.handler).strat.name.equals("FullMech") &&
                            !((GameState) this.handler).strat.name.equals("MechGreedyFE")) {
                        if (Util.countUnitTypeSelf(UnitType.Terran_Vulture) == 0) {
                            return State.FAILURE;
                        }
                        if (Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Siege_Mode) + Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Tank_Mode) * 5 <
                                Util.countUnitTypeSelf(UnitType.Terran_Marine) + Util.countUnitTypeSelf(UnitType.Terran_Medic) + Util.countUnitTypeSelf(UnitType.Terran_Vulture)) {
                            for (Factory b : ((GameState) this.handler).Fs) {
                                if (!b.isTraining() && b.canTrain(UnitType.Terran_Siege_Tank_Tank_Mode)) {
                                    ((GameState) this.handler).chosenUnit = UnitType.Terran_Siege_Tank_Tank_Mode;
                                    ((GameState) this.handler).chosenBuilding = b;
                                    return State.SUCCESS;
                                }
                            }
                        }
                    } else {*/
                if (((GameState) this.handler).strat.trainUnits.contains(UnitType.Terran_Wraith) &&
                        ((GameState) this.handler).maxWraiths - Util.countUnitTypeSelf(UnitType.Terran_Wraith) > 0 && Math.random() * 10 <= 1) {
                    return State.FAILURE;
                }
                        /*if (Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Siege_Mode) + Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Tank_Mode) < Util.countUnitTypeSelf(UnitType.Terran_Marine) * 2 +
                                Util.countUnitTypeSelf(UnitType.Terran_Vulture)) {*/
                int multiplier = 2;
                String strat = ((GameState) this.handler).strat.name;
                if (strat.equals("FullMech") || strat.equals("MechGreedyFE")) multiplier = 15;
                if (Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Siege_Mode) + Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Tank_Mode) < Util.countUnitTypeSelf(UnitType.Terran_Marine) * multiplier) {
                    Pair<Integer, Integer> cash = ((GameState) this.handler).getCash();
                    if (cash.second < (UnitType.Terran_Siege_Tank_Tank_Mode.gasPrice())) {
                        return State.FAILURE;
                    }
                    for (Factory b : ((GameState) this.handler).Fs) {
                        if (!b.isTraining() && b.canTrain(UnitType.Terran_Siege_Tank_Tank_Mode)) {
                            ((GameState) this.handler).chosenUnit = UnitType.Terran_Siege_Tank_Tank_Mode;
                            ((GameState) this.handler).chosenBuilding = b;
                            return State.SUCCESS;
                        }
                    }
                }
            }
            //}
            //}
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
