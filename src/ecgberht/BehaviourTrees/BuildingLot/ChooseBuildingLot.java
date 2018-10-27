package ecgberht.BehaviourTrees.BuildingLot;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.Bunker;
import org.openbw.bwapi4j.unit.MissileTurret;

public class ChooseBuildingLot extends Action {

    public ChooseBuildingLot(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            Building savedTurret = null;
            for (Building b : this.handler.buildingLot) {
                if (!b.isUnderAttack()) {
                    if (b instanceof Bunker) {
                        this.handler.chosenBuildingLot = b;
                        return BehavioralTree.State.SUCCESS;
                    }
                    if (b instanceof MissileTurret) savedTurret = b;
                    this.handler.chosenBuildingLot = b;
                }
            }
            if (savedTurret != null) {
                this.handler.chosenBuildingLot = savedTurret;
                return BehavioralTree.State.SUCCESS;
            }
            if (this.handler.chosenBuildingLot != null) {
                return BehavioralTree.State.SUCCESS;
            }
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}
