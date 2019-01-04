package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Barracks;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.Refinery;
import org.openbw.bwapi4j.unit.VespeneGeyser;

import java.util.Map.Entry;

public class ChooseRefinery extends Action {

    public ChooseRefinery(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            String strat = gameState.strat.name;
            if (gameState.getPlayer().supplyUsed() < gameState.strat.supplyForFirstRefinery || gameState.getCash().second >= 300) {
                return State.FAILURE;
            }
            if ((strat.equals("BioGreedyFE") || strat.equals("FullBio") || strat.equals("FullBioFE"))
                    && gameState.getCash().second >= 150) {
                return State.FAILURE;
            }
            if (gameState.strat.techToResearch.contains(TechType.Tank_Siege_Mode) && gameState.getCash().second >= 250) {
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
                for (Building w : gameState.workerTask.values()) {
                    if (w instanceof Barracks) {
                        found = true;
                        break;
                    }
                }
                if (gameState.MBs.isEmpty() && !found) return State.FAILURE;
            }
            int count = 0;
            VespeneGeyser geyser = null;
            for (Entry<VespeneGeyser, Boolean> r : gameState.vespeneGeysers.entrySet()) {
                if (r.getValue()) {
                    count++;
                } else geyser = r.getKey();
            }
            if (count == gameState.vespeneGeysers.size()) return State.FAILURE;
            for (MutablePair<UnitType, TilePosition> w : gameState.workerBuild.values()) {
                if (w.first == UnitType.Terran_Refinery) return State.FAILURE;
            }
            for (Building w : gameState.workerTask.values()) {
                if (w instanceof Refinery && geyser != null && w.getTilePosition().equals(geyser.getTilePosition()))
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
