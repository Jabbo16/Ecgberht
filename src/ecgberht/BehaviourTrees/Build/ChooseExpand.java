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

public class ChooseExpand extends Action {

    public ChooseExpand(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            String strat = gameState.getStrat().name;
            if (strat.equals("ProxyBBS") || strat.equals("ProxyEightRax") || ((strat.equals("JoyORush") || strat.equals("TheNitekat")) && gameState.getCash().first <= 550))
                return State.FAILURE;
            if (strat.equals("FullMech") && (gameState.myArmy.stream().noneMatch(u -> u.isTank()) || !gameState.getPlayer().hasResearched(TechType.Tank_Siege_Mode)) && gameState.firstExpand)
                return State.FAILURE;
            for (MutablePair<UnitType, TilePosition> w : gameState.workerBuild.values()) {
                if (w.first == UnitType.Terran_Command_Center) return State.FAILURE;
            }
            for (Unit w : gameState.workerTask.values()) {
                if (w.getType() == UnitType.Terran_Command_Center) return State.FAILURE;
            }
            if (strat.equals("PlasmaWraithHell") && Util.countUnitTypeSelf(UnitType.Terran_Command_Center) > 2) {
                return State.FAILURE;
            }
            if (gameState.iReallyWantToExpand || (gameState.getCash().first >= 550 && gameState.getArmySize() >= gameState.getStrat().armyForExpand) || (strat.equals("14CC") && gameState.supplyMan.getSupplyUsed() == 28)) {
                gameState.chosenToBuild = UnitType.Terran_Command_Center;
                return State.SUCCESS;
            }
            if ((strat.equals("BioGreedyFE") || strat.equals("MechGreedyFE") || strat.equals("BioMechGreedyFE") ||
                    strat.equals("PlasmaWraithHell")) && !gameState.MBs.isEmpty() && gameState.CCs.size() == 1) {
                gameState.chosenToBuild = UnitType.Terran_Command_Center;
                return State.SUCCESS;
            }
            int workers = gameState.workerIdle.size();
            for (Integer wt : gameState.mineralsAssigned.values()) workers += wt;
            if (gameState.mineralsAssigned.size() * 2 <= workers - 1 &&
                    gameState.getArmySize() >= gameState.getStrat().armyForExpand) {
                gameState.chosenToBuild = UnitType.Terran_Command_Center;
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
