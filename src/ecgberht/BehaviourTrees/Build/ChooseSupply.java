package ecgberht.BehaviourTrees.Build;

import bwapi.Race;
import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.UnitType;

public class ChooseSupply extends Action {

    public ChooseSupply(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.getPlayer().supplyTotal() >= 400) return State.FAILURE;
            String strat = gameState.getStrat().name;
            if (strat.equals("ProxyBBS") && Util.countBuildingAll(UnitType.Terran_Barracks) < 2) return State.FAILURE;
            if (strat.equals("ProxyEightRax") && Util.countBuildingAll(UnitType.Terran_Barracks) < 1)
                return State.FAILURE;
            if (gameState.learningManager.isNaughty() && gameState.enemyRace == Race.Zerg
                    && Util.countBuildingAll(UnitType.Terran_Barracks) < 1) {
                return State.FAILURE;
            }
            if (gameState.learningManager.isNaughty() && gameState.enemyRace == Race.Zerg
                    && Util.countBuildingAll(UnitType.Terran_Barracks) == 1
                    && Util.countBuildingAll(UnitType.Terran_Supply_Depot) > 0
                    && Util.countBuildingAll(UnitType.Terran_Bunker) < 1) {
                return State.FAILURE;
            }
            if (gameState.getSupply() > 4 * gameState.getCombatUnitsBuildings()) return State.FAILURE;
            int countSupplyDepots = (int) (gameState.workerBuild.values().stream()
                    .filter(u -> u.first == UnitType.Terran_Supply_Depot).count()
                    + gameState.workerTask.values().stream().filter(u -> u.getType() == UnitType.Terran_Supply_Depot).count());
            int maxSupplyDepots = gameState.getCash().first >= 400 && !gameState.isGoingToExpand() ? 2 : 1; // TODO improve
            if (countSupplyDepots < maxSupplyDepots) {
                gameState.chosenToBuild = UnitType.Terran_Supply_Depot;
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
