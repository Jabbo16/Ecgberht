package ecgberht.BehaviourTrees.BuildingLot;

import bwapi.Unit;
import bwapi.UnitType;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;

public class ChooseBuildingLot extends Action {

    public ChooseBuildingLot(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Unit savedTurret = null;
            for (Unit b : gameState.buildingLot) {
                if (!b.isUnderAttack()) {
                    if (b.getType() == UnitType.Terran_Bunker) {
                        gameState.chosenBuildingLot = b;
                        return State.SUCCESS;
                    }
                    if (b.getType() == UnitType.Terran_Missile_Turret) savedTurret = b;
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
