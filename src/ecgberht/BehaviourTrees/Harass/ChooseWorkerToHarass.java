package ecgberht.BehaviourTrees.Harass;

import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.unit.Worker;

public class ChooseWorkerToHarass extends Action {

    public ChooseWorkerToHarass(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.chosenUnitToHarass instanceof Worker) return State.FAILURE;
            for (Unit u : gameState.enemyCombatUnitMemory) {
                if (gameState.enemyMainBase != null && u instanceof Worker && !((Worker) u).isGatheringGas() && u.exists()) {
                    if (((Worker) u).getOrder() != Order.Move && ((Worker) u).getOrder() != Order.PlaceBuilding)
                        continue;
                    if (Util.broodWarDistance(gameState.enemyMainBase.getLocation().toPosition(), gameState.chosenHarasser.getPosition()) <= 700) {
                        gameState.chosenUnitToHarass = u;
                        return State.SUCCESS;
                    }
                }
            }
            gameState.chosenUnitToHarass = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
