package ecgberht.Attack;

import java.util.Map.Entry;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;
import ecgberht.Squad;
import ecgberht.Squad.Status;

public class SendArmy extends Action {

	public SendArmy(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {

		try {
			if (((GameState) this.handler).squads.isEmpty()) {
				return State.FAILURE;
			}
			for (Entry<String, Squad> u : ((GameState) this.handler).squads.entrySet()) {
				if (((GameState) this.handler).attackPosition != null) {
					if (u.getValue().estado == Status.IDLE || !((GameState) this.handler).attackPosition.equals(u.getValue().attack)) {
						u.getValue().giveAttackOrder(((GameState) this.handler).attackPosition);
						u.getValue().estado = Status.ATTACK;
					}
				} else {
					u.getValue().estado = Status.IDLE;
				}
			}
			return State.SUCCESS;
		} catch (Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
}
