package ecgberht.BehaviourTrees.Upgrade;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;

public class ResearchUpgrade extends Action {

    public ResearchUpgrade(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (!this.handler.defense && this.handler.chosenToBuild == UnitType.Terran_Command_Center) {
                boolean found = false;
                for (MutablePair<UnitType, TilePosition> w : this.handler.workerBuild.values()) {
                    if (w.first == UnitType.Terran_Command_Center) {
                        found = true;
                        break;
                    }
                }
                if (!found) return State.FAILURE;
            }
            if (this.handler.chosenUpgrade != null) {
                if (this.handler.chosenUnitUpgrader.upgrade(this.handler.chosenUpgrade)) {
                    this.handler.chosenUpgrade = null;
                    return State.SUCCESS;
                }
            } else if (this.handler.chosenResearch != null) {
                if (this.handler.chosenUnitUpgrader.research(this.handler.chosenResearch)) {
                    this.handler.chosenResearch = null;
                    return State.SUCCESS;
                }
            }
            this.handler.chosenUnitUpgrader = null;
            this.handler.chosenUpgrade = null;
            this.handler.chosenResearch = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
