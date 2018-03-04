package ecgberht.Attack;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Pair;
import bwapi.TilePosition;
import ecgberht.GameState;
import ecgberht.Squad;
import ecgberht.Squad.Status;

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
				Pair<Integer,Integer> p = ((GameState)this.handler).inMap.getPosition(((GameState)this.handler).getSquadCenter(u).toTilePosition(),true);
				if(p.first != null && p.second != null) {
					if(!((GameState)this.handler).firstProxyBBS && ((GameState)this.handler).strat.name == "ProxyBBS") {
						((GameState)this.handler).firstProxyBBS = true;
						((GameState)this.handler).getGame().sendText("Get ready for a party in your house!");
					}
					u.giveAttackOrder(new TilePosition(p.second,p.first).toPosition());
					u.status = Status.ATTACK;
					continue;
				} 
				else if(((GameState)this.handler).enemyBase != null){
					((GameState)this.handler).attackPosition = ((GameState)this.handler).enemyBase.getPosition();
					continue;
				} else {
					u.status = Status.IDLE;
					continue;
				}
				
			}
			return State.SUCCESS;
		} catch(Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
}
