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

public class ChooseArmory extends Action {

    public ChooseArmory(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.Fs.isEmpty() || !gameState.getPlayer().hasResearched(TechType.Tank_Siege_Mode))
                return State.FAILURE;
            if (gameState.Fs.size() < gameState.getStrat().facForArmory) return State.FAILURE;
            if (Util.countUnitTypeSelf(UnitType.Terran_Armory) < gameState.getStrat().numArmories) {
                for (MutablePair<UnitType, TilePosition> w : gameState.workerBuild.values()) {
                    if (w.first == UnitType.Terran_Armory) return State.FAILURE;
                }
                for (Unit w : gameState.workerTask.values()) {
                    if (w.getType() == UnitType.Terran_Armory) return State.FAILURE;
                }
                gameState.chosenToBuild = UnitType.Terran_Armory;
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
