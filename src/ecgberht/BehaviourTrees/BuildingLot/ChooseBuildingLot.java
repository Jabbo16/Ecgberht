package ecgberht.BehaviourTrees.BuildingLot;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.Bunker;
import org.openbw.bwapi4j.unit.MissileTurret;

public class ChooseBuildingLot extends BrainAction {

    public ChooseBuildingLot(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            Building savedTurret = null;
            for (Building b : gameState.buildingLot) {
                if (!b.isUnderAttack()) {
                    if (b instanceof Bunker) {
                        gameState.chosenBuildingLot = b;
                        return BrainStatus.SUCCESS;
                    }
                    if (b instanceof MissileTurret) savedTurret = b;
                    gameState.chosenBuildingLot = b;
                }
            }
            if (savedTurret != null) {
                gameState.chosenBuildingLot = savedTurret;
                return BrainStatus.SUCCESS;
            }
            if (gameState.chosenBuildingLot != null) return BrainStatus.SUCCESS;
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}

