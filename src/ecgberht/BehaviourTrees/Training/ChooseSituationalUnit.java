package ecgberht.BehaviourTrees.Training;

import bwapi.Unit;
import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.UnitType;


public class ChooseSituationalUnit extends Action {

    public ChooseSituationalUnit(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            // Testing dropships islands
            boolean dropship = true;
            if (!gameState.islandBases.isEmpty()) {
                for (Unit u : gameState.getPlayer().getUnits()) {
                    if (!u.exists()) continue;
                    if (u.getType() == UnitType.Terran_Dropship) {
                        dropship = false;
                        break;
                    }
                }
            } else dropship = false;
            boolean tower = false;
            if (dropship && !gameState.getStrat().name.equals("2PortWraith")) {
                for (Unit u : gameState.UBs) {
                    if (u.getType() == UnitType.Terran_Control_Tower) {
                        tower = true;
                        break;
                    }
                }
                if (!tower) return State.FAILURE;
                for (Unit s : gameState.Ps) {
                    if (s.getAddon() != null && s.getAddon().isCompleted() && !s.isTraining()) {
                        gameState.chosenUnit = UnitType.Terran_Dropship;
                        gameState.chosenBuilding = s;
                        return State.SUCCESS;
                    }
                }
            }
            // Testing dropships offensive drops
            /*if (Util.countUnitTypeSelf(UnitType.Terran_Dropship) > 0) return State.FAILURE;

            for (ResearchingFacility u : ((GameState) gameState).UBs) {
                if (u instanceof ControlTower) {
                    tower = true;
                    break;
                }
            }
            if (!tower) return State.FAILURE;
            for (Starport s : ((GameState) gameState).Ps) {
                if (s.getAddon() != null && s.getAddon().isCompleted() && !s.isTraining()) {
                    ((GameState) gameState).chosenUnit = UnitType.Terran_Dropship;
                    ((GameState) gameState).chosenBuilding = s;
                    return State.SUCCESS;
                }
            }*/

            // Testing vessels
            if (Util.countUnitTypeSelf(UnitType.Terran_Science_Vessel) > gameState.maxVessels || gameState.workerMining.isEmpty())
                return State.FAILURE;
            if (Util.countUnitTypeSelf(UnitType.Terran_Science_Vessel) > 0 && !gameState.needToAttack())
                return State.FAILURE;
            String strat = gameState.getStrat().name;
            if (strat.equals("FullMech") || strat.equals("MechGreedyFE") && Util.getNumberCCs()
                    + (int) gameState.workerTask.values().stream().filter(u -> u.getType() == UnitType.Terran_Command_Center).count() < 3)
                return State.FAILURE;
            tower = false;
            boolean science = false;
            for (Unit u : gameState.UBs) {
                if (u.getType() == UnitType.Terran_Control_Tower) tower = true;
                else if (u.getType() == UnitType.Terran_Science_Facility) science = true;
                if (science && tower) break;
            }
            if (!tower || !science) return State.FAILURE;
            for (Unit s : gameState.Ps) {
                if (s.getAddon() != null && s.getAddon().isCompleted() && !s.isTraining()) {
                    if (strat.contains("Bio") && gameState.getCash().second < UnitType.Terran_Science_Vessel.gasPrice()
                            && gameState.getCash().first >= UnitType.Terran_Science_Vessel.mineralPrice() + 50) {
                        for (Unit b : gameState.MBs) {
                            if (!b.isTraining()) {
                                gameState.chosenUnit = UnitType.Terran_Marine;
                                gameState.chosenBuilding = b;
                                return State.SUCCESS;
                            }
                        }
                    }
                    gameState.chosenUnit = UnitType.Terran_Science_Vessel;
                    gameState.chosenBuilding = s;
                    return State.SUCCESS;
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
