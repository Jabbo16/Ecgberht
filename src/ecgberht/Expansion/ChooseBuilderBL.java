package ecgberht.Expansion;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;

import bwapi.Pair;
import bwapi.Position;
import bwapi.Unit;

public class ChooseBuilderBL extends Action{

	public ChooseBuilderBL(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			
			if(((GameState)this.handler).chosenBuilderBL != null) {
				return State.SUCCESS;
			}
			Unit closestWorker = null;
			Position chosen = ((GameState)this.handler).chosenBaseLocation.toPosition();
			if(!((GameState)this.handler).workerIdle.isEmpty()) {
				for (Unit u : ((GameState)this.handler).workerIdle) {
					double unitChosen = ((GameState)this.handler).broodWarDistance(u.getPosition(), chosen);
					if(closestWorker == null) {
						closestWorker = u;
					} 
					else{
						double closestChosen = ((GameState)this.handler).broodWarDistance(closestWorker.getPosition(), chosen);
						if(unitChosen < closestChosen){
							closestWorker = u;	
						}
					}
				}
			}
			if(!((GameState)this.handler).workerTask.isEmpty()) {
				for (Pair<Unit,Unit> u : ((GameState)this.handler).workerTask) {
					double unitChosen = ((GameState)this.handler).broodWarDistance(u.first.getPosition(), chosen);
					if(closestWorker == null) {
						closestWorker = u.first;
					} 
					else {
						double closestChosen = ((GameState)this.handler).broodWarDistance(closestWorker.getPosition(), chosen);
						if(unitChosen < closestChosen && u.second.getType().isMineralField()){
							closestWorker = u.first;
						}
					}	
				}
			}

			if(closestWorker != null) {
				if(!((GameState)this.handler).workerTask.isEmpty() && ((GameState)this.handler).workerIdle.contains(closestWorker)) {
					((GameState)this.handler).workerIdle.remove(closestWorker);
				} else if(!((GameState)this.handler).workerTask.isEmpty()){
					for(Pair<Unit,Unit> u:((GameState)this.handler).workerTask) {
						if(u.first.equals(closestWorker)) {
							((GameState)this.handler).workerTask.remove(u);
							if(((GameState)this.handler).mineralsAssigned.containsKey(u.second)) {
								((GameState)this.handler).mining--;
								((GameState)this.handler).mineralsAssigned.put(u.second, ((GameState)this.handler).mineralsAssigned.get(u.second) - 1);
							}
							break;
						}
					}
				}
				if(((GameState)this.handler).chosenWorker != null && ((GameState)this.handler).chosenWorker.equals(closestWorker)){
					((GameState)this.handler).chosenWorker = null;
				}
				((GameState)this.handler).chosenBuilderBL = closestWorker;
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