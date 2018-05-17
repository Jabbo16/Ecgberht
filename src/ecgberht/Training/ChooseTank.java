package ecgberht.Training;

import bwapi.Pair;
import bwapi.Unit;
import bwapi.UnitType;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;


public class ChooseTank extends Action {

    public ChooseTank(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (!((GameState) this.handler).Fs.isEmpty()) {
                if (((GameState) this.handler).siegeResearched) {
                    if (((GameState) this.handler).strat.name != "FullMech") {
                        if (((GameState) this.handler).getPlayer().allUnitCount(UnitType.Terran_Vulture) == 0) {
                            return State.FAILURE;
                        }
                        if (((GameState) this.handler).getPlayer().allUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) + ((GameState) this.handler).getPlayer().allUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) * 5 < ((GameState) this.handler).getPlayer().allUnitCount(UnitType.Terran_Marine) + ((GameState) this.handler).getPlayer().allUnitCount(UnitType.Terran_Medic) + ((GameState) this.handler).getPlayer().allUnitCount(UnitType.Terran_Vulture)) {
                            for (Unit b : ((GameState) this.handler).Fs) {
                                if (!b.isTraining() && b.canTrain()) {
                                    ((GameState) this.handler).chosenUnit = UnitType.Terran_Siege_Tank_Tank_Mode;
                                    ((GameState) this.handler).chosenBuilding = b;
                                    return State.SUCCESS;
                                }
                            }
                        }
                    } else {
                        if (((GameState) this.handler).getPlayer().allUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) + ((GameState) this.handler).getPlayer().allUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode) < ((GameState) this.handler).getPlayer().allUnitCount(UnitType.Terran_Marine) * 2 + ((GameState) this.handler).getPlayer().allUnitCount(UnitType.Terran_Vulture)) {
                            Pair<Integer, Integer> cash = ((GameState) this.handler).getCash();
                            if (cash.second < (UnitType.Terran_Siege_Tank_Tank_Mode.gasPrice())) {
                                return State.FAILURE;
                            }
                            for (Unit b : ((GameState) this.handler).Fs) {
                                if (!b.isTraining() && b.canTrain()) {
                                    ((GameState) this.handler).chosenUnit = UnitType.Terran_Siege_Tank_Tank_Mode;
                                    ((GameState) this.handler).chosenBuilding = b;
                                    return State.SUCCESS;
                                }
                            }
                        }
                    }
                }
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            System.err.println(e);
            return State.ERROR;
        }
    }
}
