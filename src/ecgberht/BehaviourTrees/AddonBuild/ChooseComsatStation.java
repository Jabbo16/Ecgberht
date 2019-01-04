package ecgberht.BehaviourTrees.AddonBuild;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Academy;
import org.openbw.bwapi4j.unit.CommandCenter;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseComsatStation extends Action {

    public ChooseComsatStation(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (!gameState.CCs.isEmpty()) {
                for (CommandCenter c : gameState.CCs.values()) {
                    if (!c.isTraining() && c.getAddon() == null) {
                        for (ResearchingFacility u : gameState.UBs) {
                            if (u instanceof Academy) {
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
