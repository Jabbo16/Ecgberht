package ecgberht.BehaviourTrees.Training;

import bwapi.Unit;
import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.UnitType;

import java.util.Set;


public class ChooseMedic extends Action {

    public ChooseMedic(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.UBs.isEmpty()) return State.FAILURE;
            else {
                for (Unit u : gameState.UBs) {
                    if (u.getType() == UnitType.Terran_Academy) {
                        int marine_count = 0;
                        if (!gameState.DBs.isEmpty()) {
                            marine_count = gameState.DBs.values().stream().mapToInt(Set::size).sum();
                        }
                        if (!gameState.MBs.isEmpty() && Util.countUnitTypeSelf(UnitType.Terran_Medic) * 4 < Util.countUnitTypeSelf(UnitType.Terran_Marine) - marine_count) {
                            for (Unit b : gameState.MBs) {
                                if (!b.isTraining()) {
                                    gameState.chosenUnit = UnitType.Terran_Medic;
                                    gameState.chosenBuilding = b;
                                    return State.SUCCESS;
                                }
                            }
                        }
                        break;
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
