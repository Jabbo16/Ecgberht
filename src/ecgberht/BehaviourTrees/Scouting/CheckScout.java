package ecgberht.BehaviourTrees.Scouting;

import bwem.Base;
import ecgberht.GameState;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.*;
import org.openbw.bwapi4j.type.UnitType;

public class CheckScout extends BrainAction {

    public CheckScout(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            String strat = gameState.getStrat().name;
            if (strat.equals("PlasmaWraithHell")) {
                if (gameState.sqManager.squads.isEmpty()) return BrainStatus.FAILURE;
                return BrainStatus.SUCCESS;
            }
            if ((strat.equals("ProxyBBS") || strat.equals("ProxyEightRax")) && gameState.mapSize == 2) {
                for (Base b : gameState.SLs) {
                    if (gameState.mainCC != null && b.equals(gameState.mainCC.first)) continue;
                    gameState.enemyMainBase = b;
                    return BrainStatus.FAILURE;
                }
            }
            if (gameState.chosenScout == null && gameState.mapSize == 2 && Util.countUnitTypeSelf(UnitType.Terran_Supply_Depot) == 0) {
                return BrainStatus.FAILURE;
            }
            if (gameState.chosenScout == null && gameState.getPlayer().supplyUsed() >= 12 && gameState.enemyMainBase == null) {
                return BrainStatus.SUCCESS;
            }
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}

