package ecgberht.BehaviourTrees.Recollection;

import bwapi.Unit;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;

import java.util.Map.Entry;

public class CollectGas extends Action {

    public CollectGas(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.getPlayer().gas() >= 400) return State.FAILURE;
            Unit chosen = gameState.chosenWorker;
            if (!gameState.refineriesAssigned.isEmpty()) {
                Unit closestGeyser = null;
                int workerGas = gameState.getStrat().workerGas == 0 ? 3 : gameState.getStrat().workerGas;
                for (Entry<Unit, Integer> g : gameState.refineriesAssigned.entrySet()) {
                    if ((closestGeyser == null || chosen.getDistance(g.getKey()) < chosen.getDistance(closestGeyser)) && g.getValue() < workerGas && gameState.mining > 3) {
                        closestGeyser = g.getKey();
                    }
                }
                if (closestGeyser != null) {
                    if (chosen.gather(closestGeyser, false)) {
                        Integer aux = gameState.refineriesAssigned.get(closestGeyser);
                        aux++;
                        gameState.refineriesAssigned.put(closestGeyser, aux);
                        gameState.workerIdle.remove(chosen);
                        gameState.workerGas.put(chosen, closestGeyser);
                        gameState.chosenWorker = null;
                        return State.SUCCESS;
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
