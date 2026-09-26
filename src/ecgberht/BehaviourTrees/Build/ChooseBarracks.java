package ecgberht.BehaviourTrees.Build;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Barracks;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.Factory;

public class ChooseBarracks extends BrainAction {

    public ChooseBarracks(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            String strat = gameState.getStrat().name;
            if ((strat.equals("BioGreedyFE") || strat.equals("MechGreedyFE") || strat.equals("BioMechGreedyFE")) &&
                    Util.countBuildingAll(UnitType.Terran_Command_Center) == 1 &&
                    Util.countBuildingAll(UnitType.Terran_Barracks) > 1 &&
                    gameState.frameCount <= 24 * 240) {
                return BrainStatus.FAILURE;
            }
            if (strat.equals("14CC") && Util.countBuildingAll(UnitType.Terran_Command_Center) < 2) return BrainStatus.FAILURE;
            if (!gameState.getStrat().techToResearch.contains(TechType.Stim_Packs) && gameState.getStrat().raxPerCC == 1
                    && gameState.tech.MBs.size() > 0) {
                return BrainStatus.FAILURE;
            }
            if (gameState.learningManager.isNaughty() && gameState.enemyRace == Race.Zerg
                    && Util.countBuildingAll(UnitType.Terran_Barracks) == 1
                    && Util.countBuildingAll(UnitType.Terran_Bunker) < 1) {
                return BrainStatus.FAILURE;
            }
            if (!strat.equals("ProxyBBS") && !strat.equals("ProxyEightRax")) {
                if (!gameState.tech.MBs.isEmpty() && Util.countBuildingAll(UnitType.Terran_Barracks) == gameState.getStrat().numRaxForAca && Util.countBuildingAll(UnitType.Terran_Academy) == 0) {
                    return BrainStatus.FAILURE;
                }
                if (Util.countBuildingAll(UnitType.Terran_Barracks) == gameState.getStrat().numRaxForAca && Util.countBuildingAll(UnitType.Terran_Refinery) == 0) {
                    return BrainStatus.FAILURE;
                }
            } else if (gameState.getPlayer().supplyUsed() < 16) return BrainStatus.FAILURE;
            if (gameState.getStrat().buildUnits.contains(UnitType.Terran_Factory)) {
                int count = 0;
                boolean found = false;
                for (MutablePair<UnitType, TilePosition> w : gameState.eco.workerBuild.values()) {
                    if (w.first == UnitType.Terran_Barracks) count++;
                    if (w.first == UnitType.Terran_Factory) found = true;
                }
                for (Building w : gameState.eco.workerTask.values()) {
                    if (w instanceof Barracks) count++;
                    if (w instanceof Factory) found = true;
                }
                if (!gameState.tech.Fs.isEmpty()) found = true;
                if (count + gameState.tech.MBs.size() > gameState.getStrat().numRaxForFac && !found) return BrainStatus.FAILURE;
            }
            if (Util.countBuildingAll(UnitType.Terran_Academy) == 0 && Util.countBuildingAll(UnitType.Terran_Barracks) >= 2) {
                return BrainStatus.FAILURE;
            }
            if (Util.countBuildingAll(UnitType.Terran_Barracks) == gameState.tech.MBs.size()
                    && gameState.getPlayer().minerals() >= 600) {
                gameState.chosenToBuild = UnitType.Terran_Barracks;
                return BrainStatus.SUCCESS;
            }
            if (Util.countBuildingAll(UnitType.Terran_Barracks) < gameState.getStrat().raxPerCC * Util.getNumberCCs()) {
                gameState.chosenToBuild = UnitType.Terran_Barracks;
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






