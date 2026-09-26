package ecgberht.BehaviourTrees.Training;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

public class ChooseFireBat extends BrainAction {

    public ChooseFireBat(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.enemyRace != Race.Zerg || gameState.tech.UBs.isEmpty()) return BrainStatus.FAILURE;
            if (Util.countUnitTypeSelf(UnitType.Terran_Marine) >= 4) {
                for (ResearchingFacility r : gameState.tech.UBs) {
                    if (r instanceof Academy) {
                        int count = 0;
                        for (Unit u : gameState.getGame().getUnits(gameState.getPlayer())) {
                            if (!u.exists()) continue;
                            if (u instanceof Firebat) count++;
                            if (count >= gameState.maxBats) return BrainStatus.FAILURE;
                        }
                        for (Barracks b : gameState.tech.MBs) {
                            if (!b.isTraining()) {
                                gameState.chosenUnit = UnitType.Terran_Firebat;
                                gameState.chosenTrainingFacility = b;
                                return BrainStatus.SUCCESS;
                            }
                        }
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






