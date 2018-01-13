package ecgberht.Defense;

import java.util.Map.Entry;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;
import ecgberht.Squad;
import ecgberht.Squad.Status;

import bwapi.Pair;
import bwapi.Position;
import bwapi.Unit;

public class SendDefenders extends Action {

	public SendDefenders(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			boolean air_only = true;
			for(Unit u : ((GameState)this.handler).enemyInBase) {
				if(u.isFlying() || u.isCloaked()) {
					continue;
				}
				air_only = false;
			}
			if(!air_only && ((GameState)this.handler).squads.isEmpty()) {
				while(((GameState)this.handler).workerDefenders.size() < 2 && !((GameState)this.handler).workerIdle.isEmpty()) {
					Unit closestWorker = null;
					Position chosen = ((GameState)this.handler).attackPosition;
					for (Unit u : ((GameState)this.handler).workerIdle) {
						if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
							closestWorker = u;
						}
					}
					if(closestWorker != null) {
						((GameState)this.handler).workerDefenders.add(new Pair<Unit, Position>(closestWorker,null));
						((GameState)this.handler).workerIdle.remove(closestWorker);
					}
				}
				int defenders = 2;
				if(((GameState)this.handler).enemyInBase.size() == 1 && ((GameState)this.handler).enemyInBase.iterator().next().getType().isWorker()) {
					defenders = 1;
				}
				while(((GameState)this.handler).workerDefenders.size() < defenders && !((GameState)this.handler).workerTask.isEmpty()) {
					for(Pair<Unit, Unit> u:((GameState)this.handler).workerTask) {
						if(u.second.getType().isNeutral()) {
							break;
						}
						if(((GameState)this.handler).workerTask.indexOf(u) == ((GameState)this.handler).workerTask.size() - 1 && !u.second.getType().isNeutral()) {
							return State.FAILURE;
						}
					}
					Unit closestWorker = null;
					Position chosen = ((GameState)this.handler).attackPosition;
					for (Pair<Unit, Unit> u : ((GameState)this.handler).workerTask) {
						if ((closestWorker == null || u.first.getDistance(chosen) < closestWorker.getDistance(chosen))) {
							if(u.second.getType().isNeutral()) {
								closestWorker = u.first;
							}
						}
					}
					if(closestWorker != null) {
						for(Pair<Unit,Unit> u:((GameState)this.handler).workerTask) {
							if(u.first.equals(closestWorker)) {
								((GameState)this.handler).workerTask.remove(u);
								((GameState)this.handler).workerDefenders.add(new Pair<Unit, Position>(closestWorker,null));
								for(Pair<Unit,Integer> m:((GameState)this.handler).mineralsAssigned) {
									if(m.first.equals(u.second)) {
										((GameState)this.handler).mining--;
										((GameState)this.handler).mineralsAssigned.get(((GameState)this.handler).mineralsAssigned.indexOf(m)).second--;
										break;
									}
								}
								break;
							}
						}
					}
				}
				
				for(Pair<Unit,Position> u: ((GameState)this.handler).workerDefenders) {
					if(((GameState)this.handler).attackPosition != null) {
						if(u.first.isIdle() || !((GameState)this.handler).attackPosition.equals(u.second)) {
							((GameState)this.handler).workerDefenders.get(((GameState)this.handler).workerDefenders.indexOf(u)).second = ((GameState)this.handler).attackPosition;
							u.first.attack(((GameState)this.handler).attackPosition);
						}
					}
				}
			} else {
				for(Entry<String,Squad> u :((GameState)this.handler).squads.entrySet()) {
					if(((GameState)this.handler).attackPosition != null) {
						if(u.getValue().estado == Status.IDLE || !((GameState)this.handler).attackPosition.equals(u.getValue().attack)) {
							u.getValue().giveAttackOrder(((GameState)this.handler).attackPosition, ((GameState)this.handler).getGame().getFrameCount());
							u.getValue().estado = Status.DEFENSE;
						}
					}
					else {
						u.getValue().estado = Status.IDLE;
					}
				}
			}
			return State.SUCCESS;
		} catch(Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
}
