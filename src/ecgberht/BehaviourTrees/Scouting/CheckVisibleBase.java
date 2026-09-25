package ecgberht.BehaviourTrees.Scouting;

import bwem.Base;
import ecgberht.GameState;
import ecgberht.Util.BaseLocationComparator;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.*;

public class CheckVisibleBase extends BrainAction {

    public CheckVisibleBase(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.chosenScout == null) return BrainStatus.FAILURE;
            if (gameState.scoutSLs.size() == 1 && gameState.enemyMainBase == null) {
                gameState.enemyMainBase = gameState.scoutSLs.iterator().next();
                gameState.enemyBLs.clear();
                gameState.enemyBLs.addAll(gameState.BLs);
                gameState.enemyBLs.sort(new BaseLocationComparator(gameState.enemyMainBase));
                if (gameState.firstScout) {
                    gameState.enemyStartBase = gameState.enemyMainBase;
                    gameState.enemyMainArea = gameState.enemyStartBase.getArea();
                    gameState.enemyNaturalBase = gameState.enemyBLs.get(1);
                    gameState.enemyNaturalArea = gameState.enemyNaturalBase.getArea();
                }
            }
            if (!gameState.scoutSLs.isEmpty()) {
                for (Base b : gameState.scoutSLs) {
                    if ((gameState.getGame().getBWMap().isVisible(b.getLocation()))) {
                        gameState.scoutSLs.remove(b);
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

