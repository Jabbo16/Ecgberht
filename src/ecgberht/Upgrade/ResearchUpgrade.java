package ecgberht.Upgrade;

import bwapi.TechType;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

public class ResearchUpgrade extends Action {

    public ResearchUpgrade(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
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
            ((GameState) this.handler).chosenUpgrade = null;
            ((GameState) this.handler).chosenResearch = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            System.err.println(e);
            return State.ERROR;
        }
    }
}
