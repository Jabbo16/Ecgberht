package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;

public class ChooseBunker extends Action {

    public ChooseBunker(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (this.handler.getGame().getBWMap().mapHash().equals("6f5295624a7e3887470f3f2e14727b1411321a67")) {
                return State.FAILURE;
            }
            if ((needBunker() || this.handler.strat.bunker || IntelligenceAgency.enemyIsRushing() || this.handler.learningManager.isNaughty())
                    && this.handler.MBs.size() >= 1 && Util.countBuildingAll(UnitType.Terran_Bunker) == 0) {
                this.handler.chosenToBuild = UnitType.Terran_Bunker;
                return State.SUCCESS;
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }

    private boolean needBunker(){
        return this.handler.enemyRace == Race.Zerg && !this.handler.strat.name.equals("ProxyBBS")
                && !this.handler.strat.name.equals("EightRax");
    }
}
