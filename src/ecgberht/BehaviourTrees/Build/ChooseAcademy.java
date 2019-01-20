package ecgberht.BehaviourTrees.Build;

import bwapi.TilePosition;
import bwapi.Unit;
import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.Strategy;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.UnitType;

public class ChooseAcademy extends Action {

    public ChooseAcademy(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (Util.countBuildingAll(UnitType.Terran_Refinery) == 0 || Util.countBuildingAll(UnitType.Terran_Academy) > 0) {
                return State.FAILURE;
            }
            Strategy strat = gameState.getStrat();
            if (strat.name.equals("FullMech") || strat.name.equals("MechGreedyFE")) {
                if (gameState.Fs.size() >= strat.facPerCC
                        || IntelligenceAgency.enemyHasType(UnitType.Protoss_Dark_Templar)
                        || IntelligenceAgency.enemyHasType(UnitType.Zerg_Lurker)) {
                    gameState.chosenToBuild = UnitType.Terran_Academy;
                    return State.SUCCESS;
                } else return State.FAILURE;
            }
            if (Util.countBuildingAll(UnitType.Terran_Barracks) >= gameState.getStrat().numRaxForAca) {
                for (MutablePair<UnitType, TilePosition> w : gameState.workerBuild.values()) {
                    if (w.first == UnitType.Terran_Academy) return State.FAILURE;
                }
                for (Unit w : gameState.workerTask.values()) {
                    if (w.getType() == UnitType.Terran_Academy) return State.FAILURE;
                }
                gameState.chosenToBuild = UnitType.Terran_Academy;
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
