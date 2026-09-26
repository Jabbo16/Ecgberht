package ecgberht.BehaviourTrees.Build;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.SupplyDepot;

public class ChooseSupply extends BrainAction {

    public ChooseSupply(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.getPlayer().supplyTotal() >= 400) return BrainStatus.FAILURE;
            String strat = gameState.getStrat().name;
            if (strat.equals("ProxyBBS") && Util.countBuildingAll(UnitType.Terran_Barracks) < 2) return BrainStatus.FAILURE;
            if (strat.equals("ProxyEightRax") && Util.countBuildingAll(UnitType.Terran_Barracks) < 1)
                return BrainStatus.FAILURE;
            if (gameState.learningManager.isNaughty() && gameState.enemyRace == Race.Zerg
                    && Util.countBuildingAll(UnitType.Terran_Barracks) < 1) {
                return BrainStatus.FAILURE;
            }
            if (gameState.learningManager.isNaughty() && gameState.enemyRace == Race.Zerg
                    && Util.countBuildingAll(UnitType.Terran_Barracks) == 1
                    && Util.countBuildingAll(UnitType.Terran_Supply_Depot) > 0
                    && Util.countBuildingAll(UnitType.Terran_Bunker) < 1) {
                return BrainStatus.FAILURE;
            }
            if (gameState.getSupply() > 4 * gameState.getCombatUnitsBuildings()) return BrainStatus.FAILURE;
            int countSupplyDepots = (int) (gameState.eco.workerBuild.values().stream()
                    .filter(u -> u.first == UnitType.Terran_Supply_Depot).count()
                    + gameState.eco.workerTask.values().stream().filter(u -> u instanceof SupplyDepot).count());
            int maxSupplyDepots = 1;
            if (gameState.getCash().first >= 800) maxSupplyDepots = 4;
            else if (gameState.getCash().first >= 400 && !gameState.isGoingToExpand()) maxSupplyDepots = 2;
            if (countSupplyDepots < maxSupplyDepots) {
                gameState.chosenToBuild = UnitType.Terran_Supply_Depot;
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






