package ecgberht.BehaviourTrees.Build;
import ecgberht.brain.*;

import ecgberht.Agents.WorkerScoutAgent;
import ecgberht.GameState;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;

public class ChooseFactory extends BrainAction {

    public ChooseFactory(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            String strat = gameState.getStrat().name;
            if (strat.equals("FullMech") || strat.equals("MechGreedyFE")) {
                Player self = gameState.getPlayer();
                if (Util.countBuildingAll(UnitType.Terran_Factory) > 1 &&
                        !self.isResearching(TechType.Tank_Siege_Mode) && !self.hasResearched(TechType.Tank_Siege_Mode)) {
                    return BrainStatus.FAILURE;
                }
            }
            if (gameState.MBs.isEmpty() || gameState.getStrat().numRaxForFac > Util.countBuildingAll(UnitType.Terran_Barracks) ||
                    (Util.countBuildingAll(UnitType.Terran_Factory) > 0 && gameState.getStrat().facPerCC == 0)) {
                return BrainStatus.FAILURE;
            }
            if (Util.countBuildingAll(UnitType.Terran_Factory) == 0 && gameState.getStrat().facPerCC == 0) {
                gameState.chosenToBuild = UnitType.Terran_Factory;
                return BrainStatus.SUCCESS;
            } else if (Util.countBuildingAll(UnitType.Terran_Factory) < gameState.getStrat().facPerCC * Util.getNumberCCs()) {
                if (strat.equals("TwoPortWraith") && gameState.naughtySCV != null && gameState.Fs.isEmpty() && gameState.proxyBuilding == null) {
                    WorkerScoutAgent w = (WorkerScoutAgent) gameState.agents.get(gameState.naughtySCV);
                    if (w != null && w.statusToString().equals("Proxying")) {
                        gameState.chosenToBuild = UnitType.None;
                        return BrainStatus.SUCCESS;
                    }
                }
                gameState.chosenToBuild = UnitType.Terran_Factory;
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

