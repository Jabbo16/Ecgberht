package ecgberht.Defense;

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.PhotonCannon;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.Pylon;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.unit.Worker;
import org.openbw.bwapi4j.util.Pair;

import ecgberht.GameState;
import ecgberht.Squad;
import ecgberht.UnitComparator;
import ecgberht.Squad.Status;

public class SendDefenders extends Action {

	public SendDefenders(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			boolean air_only = true;
			boolean cannon_rush = false;
			for(Unit u : ((GameState)this.handler).enemyInBase) {
				if(u.isFlying() || ((PlayerUnit)u).isCloaked()) {
					continue;
				}
				if(!cannon_rush) {
					if(u instanceof Pylon || u instanceof PhotonCannon) {
						cannon_rush = true;
					}
				}
				air_only = false;
			}
			Set<Unit> friends = new TreeSet<Unit>(new UnitComparator());
			for (Squad s : ((GameState)this.handler).squads.values()){
				for(Unit u : s.members) {
					friends.add(u);
				}
			}
			boolean bunker = false;
			if(!((GameState)this.handler).DBs.isEmpty()){
				for(Unit u : ((GameState)this.handler).DBs.keySet()) {
					friends.add(u);
				}
				bunker = true;
			}
			int defenders = 6;

			if(((GameState)this.handler).enemyInBase.size() == 1 && ((GameState)this.handler).enemyInBase.iterator().next() instanceof Worker) {
				defenders = 1;
			}

			Pair<Boolean,Boolean> battleWin = new Pair<>(true,false);
			if(defenders != 1) {
				if(((GameState)this.handler).enemyInBase.size() + friends.size() < 40) {
					battleWin = ((GameState)this.handler).simulateDefenseBattle(friends, ((GameState)this.handler).enemyInBase, 150, bunker);
				}
				if(((GameState)this.handler).enemyInBase.size() >= 3* friends.size()) {
					battleWin.first = false;
				}
			}

			if(cannon_rush) {
				battleWin.first = false;
			}
			int frame = ((GameState)this.handler).frameCount;
			int notFound = 0;
			if(!air_only && ((!battleWin.first || battleWin.second) || defenders == 1)) {
				while(((GameState)this.handler).workerDefenders.size() + notFound < defenders && !((GameState)this.handler).workerIdle.isEmpty()) {
					Worker closestWorker = null;
					Position chosen = ((GameState)this.handler).attackPosition;
					for (Worker u : ((GameState)this.handler).workerIdle) {
						if(u.getLastCommandFrame() == frame) {
							continue;
						}
						if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
							closestWorker = u;
						}
					}
					if(closestWorker != null) {
						((GameState)this.handler).workerDefenders.put(closestWorker,null);
						((GameState)this.handler).workerIdle.remove(closestWorker);
					} else {
						notFound++;
					}
				}
				notFound = 0;
				while(((GameState)this.handler).workerDefenders.size() + notFound < defenders && !((GameState)this.handler).workerMining.isEmpty()) {
					Worker closestWorker = null;
					Position chosen = ((GameState)this.handler).attackPosition;
					for (Entry<Worker, MineralPatch> u : ((GameState)this.handler).workerMining.entrySet()) {
						if(u.getKey().getLastCommandFrame() == frame) {
							continue;
						}
						if ((closestWorker == null || u.getKey().getDistance(chosen) < closestWorker.getDistance(chosen))) {
							closestWorker = u.getKey();
						}
					}
					if(closestWorker != null) {
						if(((GameState)this.handler).workerMining.containsKey(closestWorker)) {
							Unit mineral = ((GameState)this.handler).workerMining.get(closestWorker);
							((GameState)this.handler).workerDefenders.put(closestWorker,null);
							if(((GameState)this.handler).mineralsAssigned.containsKey(mineral)) {
								((GameState)this.handler).mining--;
								((GameState)this.handler).mineralsAssigned.put(mineral, ((GameState)this.handler).mineralsAssigned.get(mineral) - 1);
							}
							((GameState)this.handler).workerMining.remove(closestWorker);
						}
					}else {
						notFound++;
					}
				}
				for(Entry<Worker, Position> u: ((GameState)this.handler).workerDefenders.entrySet()) {
					if(frame == u.getKey().getLastCommandFrame()){
						continue;
					}
					if(((GameState)this.handler).attackPosition != null) {
						if(u.getKey().isIdle() || !((GameState)this.handler).attackPosition.equals(u.getValue())) {
							((GameState)this.handler).workerDefenders.put(u.getKey(),((GameState)this.handler).attackPosition);
							if(((GameState)this.handler).enemyInBase.size() == 1) {
								u.getKey().attack(((GameState)this.handler).enemyInBase.iterator().next());
							}
							else {
								Unit toAttack = ((GameState)this.handler).getUnitToAttack(u.getKey(), ((GameState)this.handler).enemyInBase);

								if(toAttack != null) {
									Unit lastTarget = u.getKey().getOrderTarget();
									if(lastTarget != null) {
										if(lastTarget.equals(toAttack)) {
											continue;
										}

									}
									u.getKey().attack(toAttack);
								}
								else {
									u.getKey().attack(((GameState)this.handler).attackPosition);
								}
								continue;
							}

						}
					}
				}
			} else {
				if(((GameState)this.handler).strat.name != "ProxyBBS") {
					for(Entry<String,Squad> u :((GameState)this.handler).squads.entrySet()) {
						if(((GameState)this.handler).attackPosition != null) {
							//if(u.getValue().estado == Status.IDLE || !((GameState)this.handler).attackPosition.equals(u.getValue().attack)) {
								u.getValue().giveAttackOrder(((GameState)this.handler).attackPosition);
								u.getValue().status = Status.DEFENSE;
								continue;
							//}
						}
						else {
							u.getValue().status = Status.IDLE;
							u.getValue().attack = Position.None;
							continue;
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