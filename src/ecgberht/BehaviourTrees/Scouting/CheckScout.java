package ecgberht.BehaviourTrees.Scouting;

import bwem.Base;
import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Conditional;
import org.openbw.bwapi4j.type.UnitType;

public class CheckScout extends Conditional {

    public CheckScout(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            String strat = this.handler.strat.name;
            if (strat.equals("PlasmaWraithHell")) {
                if (this.handler.sqManager.squads.isEmpty()) return BehavioralTree.State.FAILURE;
                return BehavioralTree.State.SUCCESS;
            }
            if ((strat.equals("ProxyBBS") || strat.equals("EightRax")) && this.handler.mapSize == 2) {
                for (Base b : this.handler.SLs) {
                    if (b.equals(this.handler.mainCC.first)) continue;
                    this.handler.enemyMainBase = b;
                    return BehavioralTree.State.FAILURE;
                }
            }
            if (this.handler.chosenScout == null && this.handler.mapSize == 2 && Util.countUnitTypeSelf(UnitType.Terran_Supply_Depot) == 0) {
                return BehavioralTree.State.FAILURE;
            }
            if (this.handler.chosenScout == null && this.handler.getPlayer().supplyUsed() >= 12 && this.handler.enemyMainBase == null) {
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
