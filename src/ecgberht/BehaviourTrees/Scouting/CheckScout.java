package ecgberht.BehaviourTrees.Scouting;

import bwem.Base;
import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;
import org.openbw.bwapi4j.type.UnitType;

public class CheckScout extends Conditional {

    public CheckScout(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            String strat = this.handler.strat.name;
            if (strat.equals("PlasmaWraithHell")) {
                if (this.handler.sqManager.squads.isEmpty()) return State.FAILURE;
                return State.SUCCESS;
            }
            if ((strat.equals("ProxyBBS") || strat.equals("ProxyEightRax")) && this.handler.mapSize == 2) {
                for (Base b : this.handler.SLs) {
                    if (this.handler.mainCC != null && b.equals(this.handler.mainCC.first)) continue;
                    this.handler.enemyMainBase = b;
                    return State.FAILURE;
                }
            }
            if (this.handler.chosenScout == null && this.handler.mapSize == 2 && Util.countUnitTypeSelf(UnitType.Terran_Supply_Depot) == 0) {
                return State.FAILURE;
            }
            if (this.handler.chosenScout == null && this.handler.getPlayer().supplyUsed() >= 12 && this.handler.enemyMainBase == null) {
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
