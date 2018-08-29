package ecgberht.BehaviourTrees.BuildingLot;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.Bunker;
import org.openbw.bwapi4j.unit.MissileTurret;

public class ChooseBuildingLot extends Action {

    public ChooseBuildingLot(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Building savedTurret = null;
            for (Building b : ((GameState) this.handler).buildingLot) {
                if (!b.isUnderAttack()) {
                    if (b instanceof Bunker) {
                        ((GameState) this.handler).chosenBuildingLot = b;
                        return State.SUCCESS;
                    }
                    if (b instanceof MissileTurret) savedTurret = b;
                    ((GameState) this.handler).chosenBuildingLot = b;
                }
            }
            if (savedTurret != null) {
                ((GameState) this.handler).chosenBuildingLot = savedTurret;
                return State.SUCCESS;
            }
            if (((GameState) this.handler).chosenBuildingLot != null) {
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
