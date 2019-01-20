package ecgberht.BehaviourTrees.Build;

import bwapi.*;
import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;

public class ChooseBarracks extends Action {

    public ChooseBarracks(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            String strat = gameState.getStrat().name;
            if ((strat.equals("BioGreedyFE") || strat.equals("MechGreedyFE") || strat.equals("BioMechGreedyFE")) &&
                    Util.countBuildingAll(UnitType.Terran_Command_Center) == 1 &&
                    Util.countBuildingAll(UnitType.Terran_Barracks) > 1 &&
                    gameState.frameCount <= 24 * 240) {
                return State.FAILURE;
            }
            if (strat.equals("14CC") && Util.countBuildingAll(UnitType.Terran_Command_Center) < 2) return State.FAILURE;
            if (!gameState.getStrat().techToResearch.contains(TechType.Stim_Packs) && gameState.getStrat().raxPerCC == 1
                    && gameState.MBs.size() > 0) {
                return State.FAILURE;
            }
            if (gameState.learningManager.isNaughty() && gameState.enemyRace == Race.Zerg
                    && Util.countBuildingAll(UnitType.Terran_Barracks) == 1
                    && Util.countBuildingAll(UnitType.Terran_Bunker) < 1) {
                return State.FAILURE;
            }
            if (!strat.equals("ProxyBBS") && !strat.equals("ProxyEightRax")) {
                if (!gameState.MBs.isEmpty() && Util.countBuildingAll(UnitType.Terran_Barracks) == gameState.getStrat().numRaxForAca && Util.countBuildingAll(UnitType.Terran_Academy) == 0) {
                    return State.FAILURE;
                }
                if (Util.countBuildingAll(UnitType.Terran_Barracks) == gameState.getStrat().numRaxForAca && Util.countBuildingAll(UnitType.Terran_Refinery) == 0) {
                    return State.FAILURE;
                }
            } else if (gameState.getPlayer().supplyUsed() < 16) return State.FAILURE;
            if (gameState.getStrat().buildUnits.contains(UnitType.Terran_Factory)) {
                int count = 0;
                boolean found = false;
                for (MutablePair<UnitType, TilePosition> w : gameState.workerBuild.values()) {
                    if (w.first == UnitType.Terran_Barracks) count++;
                    if (w.first == UnitType.Terran_Factory) found = true;
                }
                for (Unit w : gameState.workerTask.values()) {
                    if (w.getType() == UnitType.Terran_Barracks) count++;
                    if (w.getType() == UnitType.Terran_Factory) found = true;
                }
                if (!gameState.Fs.isEmpty()) found = true;
                if (count + gameState.MBs.size() > gameState.getStrat().numRaxForFac && !found) return State.FAILURE;
            }
            if (Util.countBuildingAll(UnitType.Terran_Academy) == 0 && Util.countBuildingAll(UnitType.Terran_Barracks) >= 2) {
                return State.FAILURE;
            }
            if (Util.countBuildingAll(UnitType.Terran_Barracks) == gameState.MBs.size()
                    && gameState.getPlayer().minerals() >= 600) {
                gameState.chosenToBuild = UnitType.Terran_Barracks;
                return State.SUCCESS;
            }
            if (Util.countBuildingAll(UnitType.Terran_Barracks) < gameState.getStrat().raxPerCC * Util.getNumberCCs()) {
                gameState.chosenToBuild = UnitType.Terran_Barracks;
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
