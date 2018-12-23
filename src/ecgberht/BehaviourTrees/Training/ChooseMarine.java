package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Barracks;


public class ChooseMarine extends Action {

    public ChooseMarine(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (!this.handler.MBs.isEmpty()) {
                int multiplier = 2;
                String strat = this.handler.strat.name;
                Player self = this.handler.getPlayer();
                if (strat.equals("FullMech") || strat.equals("MechGreedyFE") || strat.equals("VultureRush")) multiplier = 15;
                if (!this.handler.Fs.isEmpty() && (self.isResearching(TechType.Tank_Siege_Mode) || self.hasResearched(TechType.Tank_Siege_Mode)) && self.gas() >= UnitType.Terran_Siege_Tank_Tank_Mode.gasPrice() && self.minerals() <= 200) {
                    if (Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Siege_Mode) + Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Tank_Mode) < Util.countUnitTypeSelf(UnitType.Terran_Marine) * multiplier) {
                        return State.FAILURE;
                    }
                }
                if ((strat.equals("FullMech") || strat.equals("MechGreedyFE") || strat.equals("2PortWraith")) && Util.countUnitTypeSelf(UnitType.Terran_Marine) > 6 && !this.handler.defense)
                    return State.FAILURE;
                if (strat.equals("VultureRush") && Util.countUnitTypeSelf(UnitType.Terran_Marine) > 2 && !this.handler.defense)
                    return State.FAILURE;
                for (Barracks b : this.handler.MBs) {
                    if (!b.isTraining()) {
                        this.handler.chosenUnit = UnitType.Terran_Marine;
                        this.handler.chosenBuilding = b;
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
