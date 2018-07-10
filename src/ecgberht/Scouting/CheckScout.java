package ecgberht.Scouting;

import bwem.Base;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;

public class CheckScout extends Conditional {

    public CheckScout(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).strat.name.equals("PlasmaWraithHell")) {
                if (((GameState) this.handler).squads.isEmpty()) return State.FAILURE;
                return State.SUCCESS;
            }
            if (((GameState) this.handler).strat.name.equals("ProxyBBS") && ((GameState) this.handler).mapSize == 2) {
                for (Base b : ((GameState) this.handler).SLs) {
                    if (b.equals(((GameState) this.handler).MainCC.first)) continue;
                    ((GameState) this.handler).enemyBase = b;
                    return State.FAILURE;
                }
            }
            if (((GameState) this.handler).chosenScout == null && ((GameState) this.handler).getPlayer().supplyUsed() >= 12 && ((GameState) this.handler).enemyBase == null) {
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
