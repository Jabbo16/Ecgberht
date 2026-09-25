package ecgberht.BehaviourTrees.AddonBuild;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Academy;
import org.openbw.bwapi4j.unit.CommandCenter;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseComsatStation extends BrainAction {

    public ChooseComsatStation(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (!gameState.CCs.isEmpty()) {
                for (CommandCenter c : gameState.CCs.values()) {
                    if (!c.isTraining() && c.getAddon() == null) {
                        for (ResearchingFacility u : gameState.UBs) {
                            if (u instanceof Academy) {
                                gameState.chosenBuildingAddon = c;
                                gameState.chosenAddon = UnitType.Terran_Comsat_Station;
                                return BrainStatus.SUCCESS;
                            }
                        }

                    }
                }
            }
            gameState.chosenBuildingAddon = null;
            gameState.chosenAddon = null;
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}

