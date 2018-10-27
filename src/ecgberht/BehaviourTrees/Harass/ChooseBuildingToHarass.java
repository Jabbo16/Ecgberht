package ecgberht.BehaviourTrees.Harass;

import ecgberht.EnemyBuilding;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;

public class ChooseBuildingToHarass extends Action {

    public ChooseBuildingToHarass(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (this.handler.chosenUnitToHarass != null) {
                return State.FAILURE;
            }
            for (EnemyBuilding u : this.handler.enemyBuildingMemory.values()) {
                if (this.handler.enemyMainBase != null) {
                    if (u.type.isBuilding()) {
                        if (this.handler.bwem.getMap().getArea(u.pos).equals(this.handler.bwem.getMap().getArea(this.handler.enemyMainBase.getLocation()))) {
                            this.handler.chosenUnitToHarass = u.unit;
                            return State.SUCCESS;
                        }
                    }
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
