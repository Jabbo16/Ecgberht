package ecgberht.BehaviourTrees.Training;
import ecgberht.brain.*;

import bwem.Base;
import ecgberht.GameState;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Barracks;
import org.openbw.bwapi4j.unit.CommandCenter;

import java.util.Map;

public class ChooseSCV extends BrainAction {

    public ChooseSCV(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            String strat = gameState.getStrat().name;
            if (strat.equals("ProxyBBS") || strat.equals("ProxyEightRax")) {
                boolean notTraining = false;
                for (Barracks b : gameState.tech.MBs) {
                    if (!b.isTraining()) {
                        notTraining = true;
                        break;
                    }
                }
                if (notTraining) return BrainStatus.FAILURE;
            }
            if (gameState.enemyRace == Race.Zerg && gameState.learningManager.isNaughty()
                    && Util.countBuildingAll(UnitType.Terran_Barracks) > 0
                    && Util.countBuildingAll(UnitType.Terran_Bunker) < 1 && gameState.getCash().first < 150) {
                return BrainStatus.FAILURE;
            }
            if (Util.countUnitTypeSelf(UnitType.Terran_SCV) <= 65 && Util.countUnitTypeSelf(UnitType.Terran_SCV) <= gameState.eco.mineralsAssigned.size() * 2 + gameState.eco.refineriesAssigned.size() * 3 + gameState.getStrat().extraSCVs && !gameState.CCs.isEmpty()) {
                for (Map.Entry<Base, CommandCenter> b : gameState.islandCCs.entrySet()) {
                    if (!b.getValue().isTraining() && !b.getValue().isBuildingAddon() && Util.hasFreePatches(b.getKey())) {
                        gameState.chosenUnit = UnitType.Terran_SCV;
                        gameState.chosenTrainingFacility = b.getValue();
                        return BrainStatus.SUCCESS;
                    }
                }
                for (Map.Entry<Base, CommandCenter> b : gameState.CCs.entrySet()) {
                    if (!b.getValue().isTraining() && !b.getValue().isBuildingAddon() && Util.hasFreePatches(b.getKey())) {
                        gameState.chosenUnit = UnitType.Terran_SCV;
                        gameState.chosenTrainingFacility = b.getValue();
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






