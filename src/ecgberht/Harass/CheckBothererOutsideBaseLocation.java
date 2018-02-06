package ecgberht.Harass;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;

import ecgberht.GameState;

public class CheckBothererOutsideBaseLocation extends Conditional {

	public CheckBothererOutsideBaseLocation(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).chosenUnitToHarass == null && !((GameState)this.handler).getGame().isVisible(((GameState)this.handler).enemyBase.getTilePosition())) {
				((GameState)this.handler).chosenHarasser.move(((GameState)this.handler).enemyBase.getPosition());
				return State.SUCCESS;
			}
			else if(((GameState)this.handler).chosenUnitToHarass == null && ((GameState)this.handler).getGame().isVisible(((GameState)this.handler).enemyBase.getTilePosition()) && ((GameState)this.handler).enemyBuildingMemory.isEmpty()) {
				((GameState)this.handler).workerIdle.add(((GameState)this.handler).chosenHarasser);
				((GameState)this.handler).chosenHarasser.stop();
				((GameState)this.handler).chosenHarasser = null;
				((GameState)this.handler).chosenUnitToHarass = null;
				return State.FAILURE;
			}
			
			return State.FAILURE;
			
		} catch(Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
}
