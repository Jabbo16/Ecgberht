package ecgberht.BehaviourTrees.Harass;

import bwapi.Race;
import bwapi.UnitType;
import ecgberht.GameState;
import ecgberht.Simulation.SimInfo;
import ecgberht.UnitInfo;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.Unit;

public class ChooseBuilderToHarass extends Action {

    public ChooseBuilderToHarass(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.enemyRace != Race.Terran) return State.FAILURE;
            UnitInfo unitInfo = gameState.unitStorage.getAllyUnits().get(gameState.chosenHarasser);
            if (unitInfo == null) return State.FAILURE;
            SimInfo s = gameState.sim.getSimulation(unitInfo, SimInfo.SimType.GROUND);
            if (s == null) return State.FAILURE;
            for (UnitInfo u : s.enemies) {
                Unit aux = null;
                if (gameState.enemyMainBase != null && u.unitType == UnitType.Terran_SCV && u.unit.isConstructing()) {
                    if (gameState.bwem.getMap().getArea(u.tileposition).equals(gameState.bwem.getMap().getArea(gameState.enemyMainBase.getLocation()))) {
                        if (u.unit.getBuildType().canProduce()) {
                            gameState.chosenUnitToHarass = u.unit;
                            return State.SUCCESS;
                        }
                        aux = u.unit;
                    }
                    if (aux != null) {
                        gameState.chosenUnitToHarass = aux;
                        return State.SUCCESS;
                    }
                }
            }
            gameState.chosenUnitToHarass = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
