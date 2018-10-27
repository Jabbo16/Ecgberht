package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.UnitType;

public class ChooseBunker extends Action {

    public ChooseBunker(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            if (this.handler.getGame().getBWMap().mapHash().equals("6f5295624a7e3887470f3f2e14727b1411321a67")) {
                return BehavioralTree.State.FAILURE;
            }
            if ((this.handler.strat.bunker || IntelligenceAgency.enemyIsRushing() || this.handler.EI.naughty)
                    && this.handler.MBs.size() >= 1 && Util.countBuildingAll(UnitType.Terran_Bunker) == 0) {
                this.handler.chosenToBuild = UnitType.Terran_Bunker;
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
