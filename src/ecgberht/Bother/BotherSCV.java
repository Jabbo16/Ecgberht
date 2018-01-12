package ecgberht.Bother;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;

import ecgberht.GameState;

public class BotherSCV extends Conditional {

	public BotherSCV(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).chosenSCVToBother == null) {
				return State.FAILURE;
			}
			if(((GameState)this.handler).choosenBotherer.attack(((GameState)this.handler).chosenSCVToBother)) {
				return State.SUCCESS;
			}
			return State.FAILURE;
		} catch(Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
}
