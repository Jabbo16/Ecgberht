package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;

public class ChooseFactory extends Action {

    public ChooseFactory(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            String strat = ((GameState) this.handler).strat.name;
            if (strat.equals("FullMech") || strat.equals("MechGreedyFE")) {
                Player self = ((GameState) this.handler).getPlayer();
                if (((GameState) this.handler).countUnit(UnitType.Terran_Factory) > 1 &&
                        !self.isResearching(TechType.Tank_Siege_Mode) && !self.hasResearched(TechType.Tank_Siege_Mode)) {
                    return State.FAILURE;
                }
            }
            if (((GameState) this.handler).MBs.isEmpty() || ((GameState) this.handler).strat.numRaxForFac > ((GameState) this.handler).countUnit(UnitType.Terran_Barracks) ||
                    (((GameState) this.handler).countUnit(UnitType.Terran_Factory) > 0 && ((GameState) this.handler).strat.facPerCC == 0)) {
                return State.FAILURE;
            }
            if (((GameState) this.handler).countUnit(UnitType.Terran_Factory) == 0 && ((GameState) this.handler).strat.facPerCC == 0) {
                ((GameState) this.handler).chosenToBuild = UnitType.Terran_Factory;
                return State.SUCCESS;
            } else if (((GameState) this.handler).countUnit(UnitType.Terran_Factory) < ((GameState) this.handler).strat.facPerCC * ((GameState) this.handler).CCs.size()) {
                ((GameState) this.handler).chosenToBuild = UnitType.Terran_Factory;
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
