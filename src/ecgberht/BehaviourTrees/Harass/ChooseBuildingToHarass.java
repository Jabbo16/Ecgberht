package ecgberht.BehaviourTrees.Harass;

import ecgberht.GameState;
import ecgberht.UnitStorage;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;

import java.util.stream.Collectors;

public class ChooseBuildingToHarass extends Action {

    public ChooseBuildingToHarass(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (this.handler.chosenUnitToHarass != null) return State.FAILURE;
            for (UnitStorage.UnitInfo u : this.handler.unitStorage.getEnemyUnits().values().stream().filter(u -> u.unitType.isBuilding()).collect(Collectors.toSet())) {
                if (this.handler.enemyMainBase != null && this.handler.bwem.getMap().getArea(u.tileposition).equals(this.handler.bwem.getMap().getArea(this.handler.enemyMainBase.getLocation()))) {
                    this.handler.chosenUnitToHarass = u.unit;
                    return State.SUCCESS;
                }
            }
            if (this.handler.chosenHarasser.isIdle()) {
                this.handler.workerIdle.add(this.handler.chosenHarasser);
                this.handler.chosenHarasser.stop(false);
                this.handler.chosenHarasser = null;
                this.handler.chosenUnitToHarass = null;
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
