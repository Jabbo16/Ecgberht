package ecgberht.BehaviourTrees.Scouting;

import bwem.Base;
import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.UnitType;

public class CheckScout extends Conditional {

    public CheckScout(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            String strat = ((GameState) this.handler).strat.name;
            if (strat.equals("PlasmaWraithHell")) {
                if (((GameState) this.handler).sqManager.squads.isEmpty()) return State.FAILURE;
                return State.SUCCESS;
            }
            if ((strat.equals("ProxyBBS") || strat.equals("EightRax")) && ((GameState) this.handler).mapSize == 2) {
                for (Base b : ((GameState) this.handler).SLs) {
                    if (b.equals(((GameState) this.handler).mainCC.first)) continue;
                    ((GameState) this.handler).enemyMainBase = b;
                    return State.FAILURE;
                }
            }
            if (((GameState) this.handler).chosenScout == null && ((GameState) this.handler).mapSize == 2 && Util.countUnitTypeSelf(UnitType.Terran_Supply_Depot) == 0) {
                return State.FAILURE;
            }
            if (((GameState) this.handler).chosenScout == null && ((GameState) this.handler).getPlayer().supplyUsed() >= 12 && ((GameState) this.handler).enemyMainBase == null) {
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
