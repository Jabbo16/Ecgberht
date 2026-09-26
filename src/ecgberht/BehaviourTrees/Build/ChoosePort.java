package ecgberht.BehaviourTrees.Build;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;

public class ChoosePort extends BrainAction {

    public ChoosePort(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            String strat = gameState.getStrat().name;
            if (strat.equals("FullMech") || strat.equals("MechGreedyFE")) {
                if (Util.getNumberCCs() < 2) return BrainStatus.FAILURE;
                Player self = gameState.getPlayer();
                if (Util.countBuildingAll(UnitType.Terran_Starport) >= 1 &&
                        !self.isResearching(TechType.Tank_Siege_Mode) && !self.hasResearched(TechType.Tank_Siege_Mode)) {
                    return BrainStatus.FAILURE;
                }
            }
            if (gameState.tech.MBs.isEmpty() || gameState.tech.Fs.isEmpty() || gameState.getStrat().numCCForPort > Util.getNumberCCs() ||
                    (Util.countBuildingAll(UnitType.Terran_Starport) > 0 && gameState.getStrat().portPerCC == 0)) {
                return BrainStatus.FAILURE;
            }
            if (Util.countBuildingAll(UnitType.Terran_Starport) == 0 && gameState.getStrat().portPerCC == 0) {
                gameState.chosenToBuild = UnitType.Terran_Starport;
                return BrainStatus.SUCCESS;
            } else if (Util.countBuildingAll(UnitType.Terran_Starport) < gameState.getStrat().portPerCC * Util.getNumberCCs()) {
                gameState.chosenToBuild = UnitType.Terran_Starport;
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






