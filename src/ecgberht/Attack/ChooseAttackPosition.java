package ecgberht.Attack;

import ecgberht.GameState;
import ecgberht.Squad;
import ecgberht.Squad.Status;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.util.Pair;

public class ChooseAttackPosition extends Action {

    public ChooseAttackPosition(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).squads.isEmpty()) {
                return State.FAILURE;
            }
            for (Squad u : ((GameState) this.handler).squads.values()) {
                if (u.members.isEmpty()) continue;
                Pair<Integer, Integer> p = ((GameState) this.handler).inMap.getPosition(((GameState) this.handler).getSquadCenter(u).toTilePosition(), true);
                if (p.first != -1 && p.second != -1) {
                    TilePosition attackPos = new TilePosition(p.second, p.first);
                    if (!((GameState) this.handler).firstProxyBBS && ((GameState) this.handler).strat.name == "ProxyBBS") {
                        ((GameState) this.handler).firstProxyBBS = true;
                        ((GameState) this.handler).getIH().sendText("Get ready for a party in your house!");
                    }
                    if (((GameState) this.handler).getGame().getBWMap().isValidPosition(attackPos)) {
                        u.giveAttackOrder(new TilePosition(p.second, p.first).toPosition());
                        u.status = Status.ATTACK;
                        continue;
                    }
                }
                if (((GameState) this.handler).enemyBase != null) {
                    if (!((GameState) this.handler).firstProxyBBS && ((GameState) this.handler).strat.name == "ProxyBBS") {
                        ((GameState) this.handler).firstProxyBBS = true;
                        ((GameState) this.handler).getIH().sendText("Get ready for a party in your house!");
                    }
                    u.giveAttackOrder(((GameState) this.handler).enemyBase.getLocation().toPosition());
                    u.status = Status.ATTACK;
                    continue;
                } else {
                    u.status = Status.IDLE;
                }
            }
            return State.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
