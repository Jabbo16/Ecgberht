package ecgberht.BehaviourTrees.Recollection;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.unit.GasMiningFacility;
import org.openbw.bwapi4j.unit.Worker;

import java.util.Map.Entry;

public class CollectGas extends BrainAction {

    public CollectGas(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.getPlayer().gas() >= 400 * Math.max(1, gameState.CCs.size())) return BrainStatus.FAILURE;
            Worker chosen = gameState.chosenWorker;
            if (!gameState.eco.refineriesAssigned.isEmpty()) {
                GasMiningFacility closestGeyser = null;
                int workerGas = gameState.getStrat().workerGas == 0 ? 3 : gameState.getStrat().workerGas;
                for (Entry<GasMiningFacility, Integer> g : gameState.eco.refineriesAssigned.entrySet()) {
                    if (gameState.bwem.getMap().getArea(chosen.getTilePosition()) != null && gameState.bwem.getMap().getArea(g.getKey().getTilePosition()) != null && !gameState.bwem.getMap().getArea(chosen.getTilePosition()).isAccessibleFrom(gameState.bwem.getMap().getArea(g.getKey().getTilePosition())))
                        continue;
                    if ((closestGeyser == null || chosen.getDistance(g.getKey()) < chosen.getDistance(closestGeyser)) && g.getValue() < workerGas && gameState.eco.mining > 3) {
                        closestGeyser = g.getKey();
                    }
                }
                if (closestGeyser != null) {
                    if (chosen.gather(closestGeyser, false)) {
                        Integer aux = gameState.eco.refineriesAssigned.get(closestGeyser);
                        aux++;
                        gameState.eco.refineriesAssigned.put(closestGeyser, aux);
                        gameState.workerIdle.remove(chosen);
                        gameState.eco.workerGas.put(chosen, closestGeyser);
                        gameState.chosenWorker = null;
                        return BrainStatus.SUCCESS;
                    }
                }
            }
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}






