package ecgberht.BehaviourTrees.Build;

import bwapi.Race;
import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.UnitType;

public class ChooseBunker extends Action {

    public ChooseBunker(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.getGame().mapHash().equals("6f5295624a7e3887470f3f2e14727b1411321a67")) {
                return State.FAILURE;
            }
            if ((needBunker() || gameState.getStrat().bunker || IntelligenceAgency.enemyIsRushing() || gameState.learningManager.isNaughty())
                    && gameState.MBs.size() >= 1 && Util.countBuildingAll(UnitType.Terran_Bunker) == 0) {
                gameState.chosenToBuild = UnitType.Terran_Bunker;
                return State.SUCCESS;
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }

    private boolean needBunker() {
        return gameState.enemyRace == Race.Zerg && !gameState.getStrat().name.equals("ProxyBBS")
                && !gameState.getStrat().name.equals("ProxyEightRax") && !gameState.getStrat().name.equals("TwoPortWraith");
    }
}
