package ecgberht.BehaviourTrees.Scouting;

import bwem.Base;
import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;
import bwapi.UnitType;

public class CheckScout extends Conditional {

    public CheckScout(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            String strat = gameState.getStrat().name;
            if (strat.equals("PlasmaWraithHell")) {
                if (gameState.sqManager.squads.isEmpty()) return State.FAILURE;
                return State.SUCCESS;
            }
            if ((strat.equals("ProxyBBS") || strat.equals("ProxyEightRax")) && gameState.mapSize == 2) {
                for (Base b : gameState.SLs) {
                    if (gameState.mainCC != null && b.equals(gameState.mainCC.first)) continue;
                    gameState.enemyMainBase = b;
                    return State.FAILURE;
                }
            }
            if (gameState.chosenScout == null && gameState.mapSize == 2 && Util.countUnitTypeSelf(UnitType.Terran_Supply_Depot) == 0) {
                return State.FAILURE;
            }
            if (gameState.chosenScout == null && gameState.getPlayer().supplyUsed() >= 12 && gameState.enemyMainBase == null) {
                return State.SUCCESS;
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
