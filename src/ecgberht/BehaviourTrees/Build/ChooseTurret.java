package ecgberht.BehaviourTrees.Build;

import bwapi.TilePosition;
import bwapi.Unit;
import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.Util.MutablePair;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.UnitType;

public class ChooseTurret extends Action {

    public ChooseTurret(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.getArmySize() < gameState.getStrat().armyForTurret &&
                    !IntelligenceAgency.enemyHasType(UnitType.Zerg_Lurker, UnitType.Hero_Dark_Templar)) {
                return State.FAILURE;
            }
            boolean tech = false;
            for (Unit ub : gameState.UBs) {
                if (ub.getType() == UnitType.Terran_Engineering_Bay) {
                    tech = true;
                    break;
                }
            }
            if (tech && gameState.Ts.isEmpty()) {
                for (MutablePair<UnitType, TilePosition> w : gameState.workerBuild.values()) {
                    if (w.first == UnitType.Terran_Missile_Turret) return State.FAILURE;
                }
                for (Unit w : gameState.workerTask.values()) {
                    if (w.getType() == UnitType.Terran_Missile_Turret) return State.FAILURE;
                }
                gameState.chosenToBuild = UnitType.Terran_Missile_Turret;
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
