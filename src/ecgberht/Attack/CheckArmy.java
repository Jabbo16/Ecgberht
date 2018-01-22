package ecgberht.Attack;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;

public class CheckArmy extends Conditional {

	public CheckArmy(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).getArmySize() >= 35 && !((GameState)this.handler).defense) {
				return State.SUCCESS;
			} else if(((GameState)this.handler).defense) {
				if((((GameState)this.handler).getArmySize() > 50 && ((GameState)this.handler).getArmySize() / ((GameState)this.handler).enemyInBase.size() > 10)) {
					return State.SUCCESS;
				}
			}
			return State.FAILURE;
		} catch(Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
}
