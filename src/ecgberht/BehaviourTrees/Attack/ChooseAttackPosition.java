package ecgberht.BehaviourTrees.Attack;

import ecgberht.GameState;
import ecgberht.Squad;
import ecgberht.Squad.Status;
import ecgberht.Util.MutablePair;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.TilePosition;

public class ChooseAttackPosition extends Action {

    public ChooseAttackPosition(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).sqManager.squads.isEmpty()) {
                return State.FAILURE;
            }
            for (Squad u : ((GameState) this.handler).sqManager.squads.values()) {
                if (u.members.isEmpty()) continue;
                MutablePair<Integer, Integer> p = ((GameState) this.handler).inMap.getPosition(u.getSquadCenter().toTilePosition(), true);
                if (p.first != -1 && p.second != -1) {
                    TilePosition attackPos = new TilePosition(p.second, p.first);
                    if (!((GameState) this.handler).firstProxyBBS && ((GameState) this.handler).strat.name.equals("ProxyBBS")) {
                        ((GameState) this.handler).firstProxyBBS = true;
                        ((GameState) this.handler).getIH().sendText("Get ready for a party in your house!");
                    }
                    if (((GameState) this.handler).getGame().getBWMap().isValidPosition(attackPos)) {
                        u.giveAttackOrder(attackPos.toPosition());
                        u.status = Status.ATTACK;
                    }
                }
                else if (((GameState) this.handler).enemyMainBase != null) {
                    if (!((GameState) this.handler).firstProxyBBS && ((GameState) this.handler).strat.name.equals("ProxyBBS")) {
                        ((GameState) this.handler).firstProxyBBS = true;
                        ((GameState) this.handler).getIH().sendText("Get ready for a party in your house!");
                    }
                    u.giveAttackOrder(((GameState) this.handler).enemyMainBase.getLocation().toPosition());
                    u.status = Status.ATTACK;
                } else u.status = Status.IDLE;
            }
            return State.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
