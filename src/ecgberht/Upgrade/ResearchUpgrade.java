package ecgberht.Upgrade;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.util.Pair;

public class ResearchUpgrade extends Action {

    public ResearchUpgrade(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (!((GameState) this.handler).defense) {
                if (((GameState) this.handler).chosenToBuild == UnitType.Terran_Command_Center) {
                    boolean found = false;
                    for (Pair<UnitType, TilePosition> w : ((GameState) this.handler).workerBuild.values()) {
                        if (w.first == UnitType.Terran_Command_Center) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) return State.FAILURE;
                }
            }
            if (((GameState) this.handler).chosenUpgrade != null) {
                if (((GameState) this.handler).chosenUnitUpgrader.upgrade(((GameState) this.handler).chosenUpgrade)) {
                    ((GameState) this.handler).chosenUpgrade = null;
                    return State.SUCCESS;
                }
            } else if (((GameState) this.handler).chosenResearch != null) {
                if (((GameState) this.handler).chosenUnitUpgrader.research(((GameState) this.handler).chosenResearch)) {
                    if (((GameState) this.handler).chosenResearch == TechType.Tank_Siege_Mode) {
                        ((GameState) this.handler).siegeResearched = true;
                    }
                    ((GameState) this.handler).chosenResearch = null;
                    return State.SUCCESS;
                }
            }
            ((GameState) this.handler).chosenUnitUpgrader = null;
            ((GameState) this.handler).chosenUpgrade = null;
            ((GameState) this.handler).chosenResearch = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
