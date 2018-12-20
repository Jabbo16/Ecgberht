package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Academy;

public class ChooseNothingTrain extends Action {

    public ChooseNothingTrain(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            // Bio Builds and no Stim
            boolean stim = this.handler.strat.techToResearch.contains(TechType.Stim_Packs);
            if (stim && !this.handler.getPlayer().hasResearched(TechType.Stim_Packs) &&
                    !this.handler.getPlayer().isResearching(TechType.Stim_Packs)
                    && (int) this.handler.UBs.stream().filter(u -> u instanceof Academy).count() >= 1) {
                this.handler.chosenUnit = UnitType.None;
                return State.SUCCESS;
            }
            // Mech builds and no siege
            boolean siege = this.handler.strat.techToResearch.contains(TechType.Tank_Siege_Mode);
            if (siege && Util.getNumberCCs() >= 2 &&
                    !this.handler.getPlayer().hasResearched(TechType.Tank_Siege_Mode) &&
                    !this.handler.getPlayer().isResearching(TechType.Tank_Siege_Mode) &&
                    Util.checkSiege()) {
                this.handler.chosenUnit = UnitType.None;
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
