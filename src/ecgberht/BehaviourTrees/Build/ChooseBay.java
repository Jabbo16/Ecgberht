package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.EngineeringBay;

public class ChooseBay extends Action {

    public ChooseBay(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (!IntelligenceAgency.enemyHasAirOrCloakedThreats()){
                if (gameState.getArmySize() < gameState.strat.armyForBay) return State.FAILURE;
                if (gameState.strat.name.contains("BioMech") && gameState.CCs.size() < 2) return State.FAILURE;
            }
            if (Util.countUnitTypeSelf(UnitType.Terran_Engineering_Bay) < gameState.strat.numBays) {
                for (MutablePair<UnitType, TilePosition> w : gameState.workerBuild.values()) {
                    if (w.first == UnitType.Terran_Engineering_Bay) return State.FAILURE;
                }
                for (Building w : gameState.workerTask.values()) {
                    if (w instanceof EngineeringBay) return State.FAILURE;
                }
                gameState.chosenToBuild = UnitType.Terran_Engineering_Bay;
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
