package ecgberht.BehaviourTrees.Build;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;

public class ChooseBunker extends BrainAction {

    public ChooseBunker(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.getGame().getBWMap().mapHash().equals("6f5295624a7e3887470f3f2e14727b1411321a67")) {
                return BrainStatus.FAILURE;
            }
            if ((needBunker() || gameState.getStrat().bunker || IntelligenceAgency.enemyIsRushing() || gameState.learningManager.isNaughty())
                    && gameState.tech.MBs.size() >= 1 && Util.countBuildingAll(UnitType.Terran_Bunker) == 0) {
                gameState.chosenToBuild = UnitType.Terran_Bunker;
                return BrainStatus.SUCCESS;
            }
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }

    private boolean needBunker() {
        return gameState.enemyRace == Race.Zerg && !gameState.getStrat().name.equals("ProxyBBS")
                && !gameState.getStrat().name.equals("ProxyEightRax") && !gameState.getStrat().name.equals("TwoPortWraith");
    }
}






