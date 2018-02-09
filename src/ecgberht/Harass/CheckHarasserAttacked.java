package ecgberht.Harass;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;

import bwapi.Race;
import bwapi.Unit;
import ecgberht.GameState;

public class CheckHarasserAttacked extends Conditional {
	public CheckHarasserAttacked(String name, GameHandler gh) {
		super(name, gh);
	}
	@Override
	public State execute() {
		try {
			int count = 0;
			Unit attacker = null;
			int workers = 0;
			for(Unit u : ((GameState)this.handler).getGame().enemy().getUnits()) {
				if(!u.getType().isBuilding() && u.getType().canAttack()) {
					Unit target = (u.getTarget() == null ? u.getOrderTarget() : u.getTarget());
				    if(target != null && target.equals(((GameState)this.handler).chosenHarasser)) {
				        count++;
				        if(u.getType().isWorker()){
				        	workers++;
				        	attacker = u;
				        }
				        continue;
				    }
				}
			}
			if(count > 1) {
				if(workers > 1){
					((GameState)this.handler).EI.defendHarass = true;
				}
				((GameState)this.handler).workerIdle.add(((GameState)this.handler).chosenHarasser);
				((GameState)this.handler).chosenHarasser.stop();
				((GameState)this.handler).chosenHarasser = null;
				((GameState)this.handler).chosenUnitToHarass = null;
				return State.FAILURE;
			}
			else {
				if(workers == 1){
					int hpHarasser = ((GameState)this.handler).chosenHarasser.getHitPoints();
					int hpTarget = attacker.getHitPoints() + attacker.getShields();
					double dpsTarget = ((GameState)this.handler).dpsWorkerRace.get(attacker.getType().getRace());
					double dpsSelf = ((GameState)this.handler).dpsWorkerRace.get(Race.Terran);
					if(hpTarget / dpsSelf > hpHarasser / dpsTarget){
						((GameState)this.handler).workerIdle.add(((GameState)this.handler).chosenHarasser);
						((GameState)this.handler).chosenHarasser.stop();
						((GameState)this.handler).chosenHarasser = null;
						((GameState)this.handler).chosenUnitToHarass = null;
						return State.FAILURE;
					}
					if(!attacker.equals(((GameState)this.handler).chosenUnitToHarass)){
						((GameState)this.handler).chosenHarasser.attack(attacker);
						((GameState)this.handler).chosenUnitToHarass = attacker;
					}
					
				}
				if(count == 1 && !attacker.equals(((GameState)this.handler).chosenUnitToHarass)) {
					
				}
				return State.SUCCESS;
			}
		} catch(Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
	
}
