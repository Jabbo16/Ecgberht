package ecgberht.BehaviourTrees.Training;

import bwapi.Race;
import bwapi.Unit;
import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.UnitType;

public class ChooseFireBat extends Action {

    public ChooseFireBat(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.enemyRace != Race.Zerg) return State.FAILURE;
            if (gameState.UBs.isEmpty()) return State.FAILURE;
            else if (Util.countUnitTypeSelf(UnitType.Terran_Marine) >= 4) {
                for (Unit r : gameState.UBs) {
                    if (r.getType() == UnitType.Terran_Academy) {
                        int count = 0;
                        for (Unit u : gameState.getPlayer().getUnits()) {
                            if (!u.exists()) continue;
                            if (u.getType() == UnitType.Terran_Firebat) count++;
                            if (count >= gameState.maxBats) return State.FAILURE;
                        }
                        for (Unit b : gameState.MBs) {
                            if (!b.isTraining()) {
                                gameState.chosenUnit = UnitType.Terran_Firebat;
                                gameState.chosenBuilding = b;
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
