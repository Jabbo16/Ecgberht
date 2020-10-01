package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

public class ChooseFireBat extends Action {

    public ChooseFireBat(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.enemyRace != Race.Zerg || gameState.UBs.isEmpty()) return State.FAILURE;
            if (Util.countUnitTypeSelf(UnitType.Terran_Marine) >= 4) {
                for (ResearchingFacility r : gameState.UBs) {
                    if (r instanceof Academy) {
                        int count = 0;
                        for (Unit u : gameState.getGame().getUnits(gameState.getPlayer())) {
                            if (!u.exists()) continue;
                            if (u instanceof Firebat) count++;
                            if (count >= gameState.maxBats) return State.FAILURE;
                        }
                        for (Barracks b : gameState.MBs) {
                            if (!b.isTraining()) {
                                gameState.chosenUnit = UnitType.Terran_Firebat;
                                gameState.chosenTrainingFacility = b;
                                return State.SUCCESS;
                            }
                        }
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
