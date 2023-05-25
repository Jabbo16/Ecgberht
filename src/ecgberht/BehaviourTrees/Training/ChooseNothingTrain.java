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
            boolean stim = gameState.getStrategyFromManager().techToResearch.contains(TechType.Stim_Packs);
            if (stim && !gameState.getPlayer().hasResearched(TechType.Stim_Packs) &&
                    !gameState.getPlayer().isResearching(TechType.Stim_Packs)
                    && (int) gameState.UBs.stream().filter(u -> u instanceof Academy).count() >= 1) {
                gameState.chosenUnit = UnitType.None;
                return State.SUCCESS;
            }
            // Mech builds and no siege
            boolean siege = gameState.getStrategyFromManager().techToResearch.contains(TechType.Tank_Siege_Mode);
            if (siege && Util.getNumberCCs() >= 2 &&
                    !gameState.getPlayer().hasResearched(TechType.Tank_Siege_Mode) &&
                    !gameState.getPlayer().isResearching(TechType.Tank_Siege_Mode) &&
                    Util.checkSiege()) {
                gameState.chosenUnit = UnitType.None;
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
