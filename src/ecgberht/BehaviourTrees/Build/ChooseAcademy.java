package ecgberht.BehaviourTrees.Build;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.Strategy;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Academy;
import org.openbw.bwapi4j.unit.Building;

public class ChooseAcademy extends BrainAction {

    public ChooseAcademy(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (Util.countBuildingAll(UnitType.Terran_Refinery) == 0 || Util.countBuildingAll(UnitType.Terran_Academy) > 0) {
                return BrainStatus.FAILURE;
            }
            Strategy strat = gameState.getStrat();
            if (strat.name.equals("FullMech") || strat.name.equals("MechGreedyFE")) {
                if (gameState.Fs.size() >= strat.facPerCC
                        || IntelligenceAgency.enemyHasType(UnitType.Protoss_Dark_Templar)
                        || IntelligenceAgency.enemyHasType(UnitType.Zerg_Lurker)) {
                    gameState.chosenToBuild = UnitType.Terran_Academy;
                    return BrainStatus.SUCCESS;
                } else return BrainStatus.FAILURE;
            }
            if (Util.countBuildingAll(UnitType.Terran_Barracks) >= gameState.getStrat().numRaxForAca) {
                for (MutablePair<UnitType, TilePosition> w : gameState.workerBuild.values()) {
                    if (w.first == UnitType.Terran_Academy) return BrainStatus.FAILURE;
                }
                for (Building w : gameState.workerTask.values()) {
                    if (w instanceof Academy) return BrainStatus.FAILURE;
                }
                gameState.chosenToBuild = UnitType.Terran_Academy;
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

