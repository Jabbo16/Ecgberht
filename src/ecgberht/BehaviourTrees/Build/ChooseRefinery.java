package ecgberht.BehaviourTrees.Build;

import bwapi.TilePosition;
import bwapi.Unit;
import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.TechType;
import bwapi.UnitType;

import java.util.Map.Entry;

public class ChooseRefinery extends Action {

    public ChooseRefinery(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            String strat = gameState.getStrat().name;
            if (gameState.getPlayer().supplyUsed() < gameState.getStrat().supplyForFirstRefinery || gameState.getCash().second >= 300) {
                return State.FAILURE;
            }
            if ((strat.equals("BioGreedyFE") || strat.equals("FullBio") || strat.equals("FullBioFE"))
                    && gameState.getCash().second >= 150) {
                return State.FAILURE;
            }
            if (gameState.getStrat().techToResearch.contains(TechType.Tank_Siege_Mode) && gameState.getCash().second >= 250) {
                return State.FAILURE;
            }
            if (gameState.refineriesAssigned.size() == 1) {
                boolean found = false;
                for (MutablePair<UnitType, TilePosition> w : gameState.workerBuild.values()) {
                    if (w.first == UnitType.Terran_Barracks) {
                        found = true;
                        break;
                    }
                }
                for (Unit w : gameState.workerTask.values()) {
                    if (w .getType() == UnitType.Terran_Barracks) {
                        found = true;
                        break;
                    }
                }
                if (gameState.MBs.isEmpty() && !found) return State.FAILURE;
            }
            int count = 0;
            Unit geyser = null;
            for (Entry<Unit, Boolean> r : gameState.vespeneGeysers.entrySet()) {
                if (r.getValue()) {
                    count++;
                } else geyser = r.getKey();
            }
            if (count == gameState.vespeneGeysers.size()) return State.FAILURE;
            for (MutablePair<UnitType, TilePosition> w : gameState.workerBuild.values()) {
                if (w.first == UnitType.Terran_Refinery) return State.FAILURE;
            }
            for (Unit w : gameState.workerTask.values()) {
                if (w.getType() == UnitType.Terran_Refinery && geyser != null && w.getTilePosition().equals(geyser.getTilePosition()))
                    return State.FAILURE;
            }
            if ((strat.equals("BioGreedyFE") || strat.equals("MechGreedyFE") || strat.equals("BioMechGreedyFE")) &&
                    !gameState.refineriesAssigned.isEmpty()
                    && Util.getNumberCCs() <= 2 && Util.countUnitTypeSelf(UnitType.Terran_SCV) < 30) {
                return State.FAILURE;
            }
            gameState.chosenToBuild = UnitType.Terran_Refinery;
            return State.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
