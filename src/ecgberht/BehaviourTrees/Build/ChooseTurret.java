package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.Util.MutablePair;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.EngineeringBay;
import org.openbw.bwapi4j.unit.MissileTurret;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseTurret extends Action {

    public ChooseTurret(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.getArmySize() < gameState.strat.armyForTurret &&
                    !IntelligenceAgency.enemyHasType(UnitType.Zerg_Lurker, UnitType.Hero_Dark_Templar)) {
                return State.FAILURE;
            }
            boolean tech = false;
            for (ResearchingFacility ub : gameState.UBs) {
                if (ub instanceof EngineeringBay) {
                    tech = true;
                    break;
                }
            }
            if (tech && gameState.Ts.isEmpty()) {
                for (MutablePair<UnitType, TilePosition> w : gameState.workerBuild.values()) {
                    if (w.first == UnitType.Terran_Missile_Turret) return State.FAILURE;
                }
                for (Building w : gameState.workerTask.values()) {
                    if (w instanceof MissileTurret) return State.FAILURE;
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
