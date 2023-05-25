package ecgberht.BehaviourTrees.Training;

import bwem.Base;
import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Barracks;
import org.openbw.bwapi4j.unit.CommandCenter;

import java.util.Map;

public class ChooseSCV extends Action {

    public ChooseSCV(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            String strat = gameState.getStrategyFromManager().name;
            if (strat.equals("ProxyBBS") || strat.equals("ProxyEightRax")) {
                boolean notTraining = false;
                for (Barracks b : gameState.MBs) {
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
            if (Util.countUnitTypeSelf(UnitType.Terran_SCV) <= 65 && Util.countUnitTypeSelf(UnitType.Terran_SCV) <= gameState.mineralsAssigned.size() * 2 + gameState.refineriesAssigned.size() * 3 + gameState.getStrategyFromManager().extraSCVs && !gameState.CCs.isEmpty()) {
                for (Map.Entry<Base, CommandCenter> b : gameState.islandCCs.entrySet()) {
                    if (!b.getValue().isTraining() && !b.getValue().isBuildingAddon() && Util.hasFreePatches(b.getKey())) {
                        gameState.chosenUnit = UnitType.Terran_SCV;
                        gameState.chosenTrainingFacility = b.getValue();
                        return State.SUCCESS;
                    }
                }
                for (Map.Entry<Base, CommandCenter> b : gameState.CCs.entrySet()) {
                    if (!b.getValue().isTraining() && !b.getValue().isBuildingAddon() && Util.hasFreePatches(b.getKey())) {
                        gameState.chosenUnit = UnitType.Terran_SCV;
                        gameState.chosenTrainingFacility = b.getValue();
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
