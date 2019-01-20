package ecgberht.BehaviourTrees.Training;

import bwapi.Race;
import bwapi.Unit;
import bwem.Base;
import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.UnitType;

import java.util.Map;

public class ChooseSCV extends Action {

    public ChooseSCV(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            String strat = gameState.getStrat().name;
            if (strat.equals("ProxyBBS") || strat.equals("ProxyEightRax")) {
                boolean notTraining = false;
                for (Unit b : gameState.MBs) {
                    if (!b.isTraining()) {
                        notTraining = true;
                        break;
                    }
                }
                if (notTraining) return State.FAILURE;
            }
            if (gameState.enemyRace == Race.Zerg && gameState.learningManager.isNaughty()
                    && Util.countBuildingAll(UnitType.Terran_Barracks) > 0
                    && Util.countBuildingAll(UnitType.Terran_Bunker) < 1 && gameState.getCash().first < 150) {
                return State.FAILURE;
            }
            if (Util.countUnitTypeSelf(UnitType.Terran_SCV) <= 65 && Util.countUnitTypeSelf(UnitType.Terran_SCV) <= gameState.mineralsAssigned.size() * 2 + gameState.refineriesAssigned.size() * 3 + gameState.getStrat().extraSCVs && !gameState.CCs.isEmpty()) {
                for (Map.Entry<Base, Unit> b : gameState.islandCCs.entrySet()) {
                    if (!b.getValue().isTraining() && !(b.getValue().getAddon() != null && !b.getValue().getAddon().isCompleted()) && Util.hasFreePatches(b.getKey())) {
                        gameState.chosenUnit = UnitType.Terran_SCV;
                        gameState.chosenBuilding = b.getValue();
                        return State.SUCCESS;
                    }
                }
                for (Map.Entry<Base, Unit> b : gameState.CCs.entrySet()) {
                    if (!b.getValue().isTraining() && !(b.getValue().getAddon() != null && !b.getValue().getAddon().isCompleted()) && Util.hasFreePatches(b.getKey())) {
                        gameState.chosenUnit = UnitType.Terran_SCV;
                        gameState.chosenBuilding = b.getValue();
                        return State.SUCCESS;
                    }
                }
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
