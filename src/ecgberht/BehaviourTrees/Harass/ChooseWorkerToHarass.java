package ecgberht.BehaviourTrees.Harass;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.unit.Worker;

public class ChooseWorkerToHarass extends BrainAction {

    public ChooseWorkerToHarass(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.chosenUnitToHarass instanceof Worker) return BrainStatus.FAILURE;
            for (Unit u : gameState.enemyCombatUnitMemory) {
                if (gameState.enemyMainBase != null && u instanceof Worker && !((Worker) u).isGatheringGas() && u.exists()) {
                    if (((Worker) u).getOrder() != Order.Move && ((Worker) u).getOrder() != Order.PlaceBuilding)
                        continue;
                    if (Util.broodWarDistance(gameState.enemyMainBase.getLocation().toPosition(), gameState.chosenHarasser.getPosition()) <= 700) {
                        gameState.chosenUnitToHarass = u;
                        return BrainStatus.SUCCESS;
                    }
                }
            }
            gameState.chosenUnitToHarass = null;
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}

