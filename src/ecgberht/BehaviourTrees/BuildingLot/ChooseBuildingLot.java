package ecgberht.BehaviourTrees.BuildingLot;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.Bunker;
import org.openbw.bwapi4j.unit.MissileTurret;

public class ChooseBuildingLot extends Action {

    public ChooseBuildingLot(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Building savedTurret = null;
            for (Building b : gameState.buildingLot) {
                if (!b.isUnderAttack()) {
                    if (b instanceof Bunker) {
                        gameState.chosenBuildingLot = b;
                        return State.SUCCESS;
                    }
                    if (b instanceof MissileTurret) savedTurret = b;
                    gameState.chosenBuildingLot = b;
                }
            }
            if (savedTurret != null) {
                gameState.chosenBuildingLot = savedTurret;
                return State.SUCCESS;
            }
            if (gameState.chosenBuildingLot != null) return State.SUCCESS;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
