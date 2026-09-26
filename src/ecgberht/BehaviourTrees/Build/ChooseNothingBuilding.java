package ecgberht.BehaviourTrees.Build;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Academy;

public class ChooseNothingBuilding extends BrainAction {

    public ChooseNothingBuilding(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            // Bio Builds and no Stim
            boolean stim = gameState.getStrat().techToResearch.contains(TechType.Stim_Packs);
            if (stim && !gameState.getPlayer().hasResearched(TechType.Stim_Packs) &&
                    !gameState.getPlayer().isResearching(TechType.Stim_Packs)
                    && (int) gameState.tech.UBs.stream().filter(u -> u instanceof Academy).count() >= 1) {
                gameState.chosenToBuild = UnitType.None;
                return BrainStatus.SUCCESS;
            }
            // Mech builds and no siege
            boolean siege = gameState.getStrat().techToResearch.contains(TechType.Tank_Siege_Mode);
            if (siege && Util.getNumberCCs() >= 2 &&
                    !gameState.getPlayer().hasResearched(TechType.Tank_Siege_Mode) &&
                    !gameState.getPlayer().isResearching(TechType.Tank_Siege_Mode) &&
                    Util.checkSiege()) {
                gameState.chosenToBuild = UnitType.None;
                return BrainStatus.SUCCESS;
            }
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}






