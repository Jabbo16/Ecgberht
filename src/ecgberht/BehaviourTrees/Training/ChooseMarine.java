package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import ecgberht.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Barracks;


public class ChooseMarine extends Action {

    public ChooseMarine(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (!((GameState) this.handler).MBs.isEmpty()) {
                int multiplier = 2;
                String strat = ((GameState) this.handler).strat.name;
                Player self = ((GameState) this.handler).getPlayer();
                if (strat.equals("FullMech") || strat.equals("MechGreedyFE")) multiplier = 15;
                if (!((GameState) this.handler).Fs.isEmpty() && (self.isResearching(TechType.Tank_Siege_Mode) || self.hasResearched(TechType.Tank_Siege_Mode)) && self.gas() >= UnitType.Terran_Siege_Tank_Tank_Mode.gasPrice() && self.minerals() <= 200) {
                    if (Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Siege_Mode) + Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Tank_Mode) < Util.countUnitTypeSelf(UnitType.Terran_Marine) * multiplier) {
                        return State.FAILURE;
                    }
                }
                for (Barracks b : ((GameState) this.handler).MBs) {
                    if (!b.isTraining()) {
                        ((GameState) this.handler).chosenUnit = UnitType.Terran_Marine;
                        ((GameState) this.handler).chosenBuilding = b;
                        return State.SUCCESS;
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
