package ecgberht.Bother;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;

import bwapi.Race;
import bwapi.Unit;
import bwta.BWTA;
import ecgberht.GameState;

public class CheckWorkerToBother extends Conditional {

	public CheckWorkerToBother(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).enemyRace != Race.Terran) {
				return State.FAILURE;
			}
			for(Unit u : ((GameState)this.handler).getGame().enemy().getUnits()) {
				Unit aux = null;
				if(((GameState)this.handler).enemyBase != null) {
					if(BWTA.getRegion(u.getPosition()).getCenter().equals(((GameState)this.handler).enemyBase.getPosition())){
						if(u.getType().isWorker() && u.isConstructing()) {
							if(u.getBuildType().canProduce()) {
								((GameState)this.handler).chosenSCVToBother = u;
								return State.SUCCESS;
							}
							aux = u;
						}
						if(aux != null) {
							((GameState)this.handler).chosenSCVToBother = aux;
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
