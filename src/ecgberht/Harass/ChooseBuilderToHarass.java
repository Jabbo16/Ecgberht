package ecgberht.Harass;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Race;
import bwapi.Unit;
import bwta.BWTA;
import ecgberht.GameState;

public class ChooseBuilderToHarass extends Action {

	public ChooseBuilderToHarass(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).enemyRace != Race.Terran) {
				return State.FAILURE;
			}
			if(((GameState)this.handler).chosenUnitToHarass != null && ((GameState)this.handler).chosenUnitToHarass.getType().isWorker() ) {
				return State.FAILURE;
			}
			for(Unit u : ((GameState)this.handler).getGame().enemy().getUnits()) {
				Unit aux = null;
				if(((GameState)this.handler).enemyBase != null) {
					if(u.getType().isWorker() && u.isConstructing()) {
						if(BWTA.getRegion(u.getPosition()).getCenter().equals(BWTA.getRegion(((GameState)this.handler).enemyBase.getPosition()).getCenter())){
							if(u.getBuildType().canProduce()) {
								((GameState)this.handler).chosenUnitToHarass = u;
								return State.SUCCESS;
							}
							aux = u;
						}
						if(aux != null) {
							((GameState)this.handler).chosenUnitToHarass = aux;
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
