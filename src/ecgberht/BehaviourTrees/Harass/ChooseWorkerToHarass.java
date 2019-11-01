package ecgberht.BehaviourTrees.Harass;

import bwapi.Order;
import ecgberht.GameState;
import ecgberht.Simulation.SimInfo;
import ecgberht.UnitInfo;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.Unit;

public class ChooseWorkerToHarass extends Action {

    public ChooseWorkerToHarass(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.chosenUnitToHarass == null || gameState.chosenUnitToHarass.getType().isWorker()) return State.FAILURE;
            UnitInfo unitInfo = gameState.unitStorage.getAllyUnits().get(gameState.chosenHarasser);
            if (unitInfo == null) return State.FAILURE;
            SimInfo s = gameState.sim.getSimulation(unitInfo, SimInfo.SimType.GROUND);
            if (s == null) return State.FAILURE;
            for (UnitInfo u : s.enemies) {
                if (gameState.enemyMainBase != null && u.unit.exists() && u.unitType.isWorker() && !u.unit.isGatheringGas()) {
                    if (u.currentOrder != Order.Move && u.currentOrder != Order.PlaceBuilding) continue;
                    if (Util.broodWarDistance(gameState.enemyMainBase.getLocation().toPosition(), gameState.chosenHarasser.getPosition()) <= 700) {
                        gameState.chosenUnitToHarass = u.unit;
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
