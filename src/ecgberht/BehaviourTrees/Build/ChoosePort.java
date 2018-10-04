package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;

public class ChoosePort extends Action {

    public ChoosePort(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            String strat = ((GameState) this.handler).strat.name;
            if (strat.equals("FullMech") || strat.equals("MechGreedyFE")) {
                if (((GameState) this.handler).CCs.size() < 2) return State.FAILURE;
                Player self = ((GameState) this.handler).getPlayer();
                if (Util.countBuildingAll(UnitType.Terran_Starport) >= 1 &&
                        !self.isResearching(TechType.Tank_Siege_Mode) && !self.hasResearched(TechType.Tank_Siege_Mode)) {
                    return State.FAILURE;
                }
            }
            if (((GameState) this.handler).MBs.isEmpty() || ((GameState) this.handler).Fs.isEmpty() || ((GameState) this.handler).strat.numCCForPort > ((GameState) this.handler).CCs.size() ||
                    (Util.countBuildingAll(UnitType.Terran_Starport) > 0 && ((GameState) this.handler).strat.portPerCC == 0)) {
                return State.FAILURE;
            }
            if (Util.countBuildingAll(UnitType.Terran_Starport) == 0 && ((GameState) this.handler).strat.portPerCC == 0) {
                ((GameState) this.handler).chosenToBuild = UnitType.Terran_Starport;
                return State.SUCCESS;
            } else if (Util.countBuildingAll(UnitType.Terran_Starport) < ((GameState) this.handler).strat.portPerCC * ((GameState) this.handler).CCs.size()) {
                ((GameState) this.handler).chosenToBuild = UnitType.Terran_Starport;
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
