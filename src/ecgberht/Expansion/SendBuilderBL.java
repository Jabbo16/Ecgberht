package ecgberht.Expansion;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;

public class SendBuilderBL extends Action{

	public SendBuilderBL(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			
			if(((GameState)this.handler).movingToExpand){
				return State.SUCCESS;
			}
			if(((GameState)this.handler).chosenBuilderBL.move(((GameState)this.handler).chosenBaseLocation.toPosition())) {
				((GameState)this.handler).movingToExpand = true;
				((GameState)this.handler).moveUnitFromChokeWhenExpand();
				return State.SUCCESS;
			}
			((GameState)this.handler).movingToExpand = false;
			
			return State.FAILURE;
		} catch(Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
	
}