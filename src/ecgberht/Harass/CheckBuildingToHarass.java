package ecgberht.Harass;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;

import bwta.BWTA;
import ecgberht.EnemyBuilding;
import ecgberht.GameState;

public class CheckBuildingToHarass extends Conditional {

	public CheckBuildingToHarass(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			for(EnemyBuilding u : ((GameState)this.handler).enemyBuildingMemory.values()) {
				if(((GameState)this.handler).enemyBase != null) {
					if(u.type.isBuilding()) {
						if(BWTA.getRegion(u.pos).getCenter().equals(BWTA.getRegion(((GameState)this.handler).enemyBase.getPosition()).getCenter())){
							((GameState)this.handler).chosenUnitToHarass = u.unit;
							return State.SUCCESS;
						}
					}
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
