package ecgberht.BehaviourTrees.BuildingLot;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.*;

public class CheckBuildingsLot extends BrainAction {

    public CheckBuildingsLot(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.buildingLot.isEmpty()) return BrainStatus.FAILURE;
            return BrainStatus.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}

