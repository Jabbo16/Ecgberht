package ecgberht.Expansion;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.unit.Worker;

import ecgberht.GameState;

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
			if(!((GameState)this.handler).workerMining.isEmpty()) {
				for (Unit u : ((GameState)this.handler).workerMining.keySet()) {
					double unitChosen = ((GameState)this.handler).broodWarDistance(u.getPosition(), chosen);
					if(closestWorker == null) {
						closestWorker = u;
					}
					else {
						double closestChosen = ((GameState)this.handler).broodWarDistance(closestWorker.getPosition(), chosen);
						if(unitChosen < closestChosen){
							closestWorker = u;
						}
					}
				}
			}

			if(closestWorker != null) {
				if(!((GameState)this.handler).workerIdle.isEmpty() && ((GameState)this.handler).workerIdle.contains(closestWorker)) {
					((GameState)this.handler).workerIdle.remove(closestWorker);
				} else if(!((GameState)this.handler).workerMining.isEmpty()){
					if(((GameState)this.handler).workerMining.containsKey(closestWorker)) {
						Unit mineral = ((GameState)this.handler).workerMining.get(closestWorker);
						((GameState)this.handler).workerMining.remove(closestWorker);
						if(((GameState)this.handler).mineralsAssigned.containsKey(mineral)) {
							((GameState)this.handler).mining--;
							((GameState)this.handler).mineralsAssigned.put((MineralPatch) mineral, ((GameState)this.handler).mineralsAssigned.get(mineral) - 1);
						}
					}
				}
				if(((GameState)this.handler).chosenWorker != null && ((GameState)this.handler).chosenWorker.equals(closestWorker)){
					((GameState)this.handler).chosenWorker = null;
				}
				((GameState)this.handler).chosenBuilderBL = (Worker) closestWorker;
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