package ecgberht.BehaviourTrees.Training;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Academy;
import org.openbw.bwapi4j.unit.Barracks;
import org.openbw.bwapi4j.unit.ResearchingFacility;

import java.util.Set;


public class ChooseMedic extends BrainAction {

    public ChooseMedic(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.UBs.isEmpty()) return BrainStatus.FAILURE;
            else {
                for (ResearchingFacility u : gameState.UBs) {
                    if (u instanceof Academy) {
                        int marine_count = 0;
                        if (!gameState.DBs.isEmpty()) {
                            marine_count = gameState.DBs.values().stream().mapToInt(Set::size).sum();
                        }
                        if (!gameState.MBs.isEmpty() && Util.countUnitTypeSelf(UnitType.Terran_Medic) * 4 < Util.countUnitTypeSelf(UnitType.Terran_Marine) - marine_count) {
                            for (Barracks b : gameState.MBs) {
                                if (!b.isTraining()) {
                                    gameState.chosenUnit = UnitType.Terran_Medic;
                                    gameState.chosenTrainingFacility = b;
                                    return BrainStatus.SUCCESS;
                                }
                            }
                        }
                        break;
                    }
                }
            }
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}

