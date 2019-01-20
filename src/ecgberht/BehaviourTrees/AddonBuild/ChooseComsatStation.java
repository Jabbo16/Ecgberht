package ecgberht.BehaviourTrees.AddonBuild;

import bwapi.Unit;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.UnitType;

public class ChooseComsatStation extends Action {

    public ChooseComsatStation(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (!gameState.CCs.isEmpty()) {
                for (Unit c : gameState.CCs.values()) {
                    if (!c.isTraining() && c.getAddon() == null) {
                        for (Unit u : gameState.UBs) {
                            if (u.getType() == UnitType.Terran_Academy) {
                                gameState.chosenBuildingAddon = c;
                                gameState.chosenAddon = UnitType.Terran_Comsat_Station;
                                return State.SUCCESS;
                            }
                        }

                    }
                }
            }
            gameState.chosenBuildingAddon = null;
            gameState.chosenAddon = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
