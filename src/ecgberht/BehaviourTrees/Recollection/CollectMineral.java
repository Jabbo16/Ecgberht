package ecgberht.BehaviourTrees.Recollection;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.Worker;

import java.util.Map.Entry;

public class CollectMineral extends BrainAction {

    public CollectMineral(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            Worker chosen = gameState.chosenWorker;
            if (!gameState.eco.mineralsAssigned.isEmpty()) {
                MineralPatch closestMineral = null;
                int workerPerPatch = 2;
                if (gameState.eco.workerMining.size() < 7) workerPerPatch = 1;
                for (Entry<MineralPatch, Integer> m : gameState.eco.mineralsAssigned.entrySet()) {
                    if (gameState.bwem.getMap().getArea(chosen.getTilePosition()) != null && gameState.bwem.getMap().getArea(m.getKey().getTilePosition()) != null && !gameState.bwem.getMap().getArea(chosen.getTilePosition()).isAccessibleFrom(gameState.bwem.getMap().getArea(m.getKey().getTilePosition())))
                        continue;
                    if ((closestMineral == null || chosen.getDistance(m.getKey()) < chosen.getDistance(closestMineral))
                            && m.getValue() < workerPerPatch) {
                        closestMineral = m.getKey();
                    }
                }
                if (closestMineral == null) {
                    for (Entry<MineralPatch, Integer> m : gameState.eco.mineralsAssigned.entrySet()) {
                        if (gameState.bwem.getMap().getArea(chosen.getTilePosition()) != null && gameState.bwem.getMap().getArea(m.getKey().getTilePosition()) != null && !gameState.bwem.getMap().getArea(chosen.getTilePosition()).isAccessibleFrom(gameState.bwem.getMap().getArea(m.getKey().getTilePosition())))
                            continue;
                        if (closestMineral == null || chosen.getDistance(m.getKey()) < chosen.getDistance(closestMineral)) {
                            closestMineral = m.getKey();
                        }
                    }
                }
                if (closestMineral != null && chosen.gather(closestMineral, false)) {
                    gameState.eco.mineralsAssigned.put(closestMineral, gameState.eco.mineralsAssigned.get(closestMineral) + 1);
                    gameState.eco.workerMining.put(chosen, closestMineral);
                    gameState.workerIdle.remove(chosen);
                    gameState.chosenWorker = null;
                    gameState.eco.mining++;
                    return BrainStatus.SUCCESS;
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






