package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;

public class ChooseBunker extends Action {

    public ChooseBunker(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.getGame().getBWMap().mapHash().equals("6f5295624a7e3887470f3f2e14727b1411321a67")) {
                return State.FAILURE;
            }
            if ((needBunker() || gameState.strat.bunker || IntelligenceAgency.enemyIsRushing() || gameState.learningManager.isNaughty())
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
        return gameState.enemyRace == Race.Zerg && !gameState.strat.name.equals("ProxyBBS")
                && !gameState.strat.name.equals("ProxyEightRax") && !gameState.strat.name.equals("TwoPortWraith");
    }
}
