package ecgberht.Harass;

import static ecgberht.Ecgberht.getGs;

import java.util.HashSet;
import java.util.Set;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;

import bwapi.Position;
import bwapi.Unit;
import ecgberht.GameState;

public class CheckHarasserAttacked extends Conditional {
	public CheckHarasserAttacked(String name, GameHandler gh) {
		super(name, gh);
	}
	@Override
	public State execute() {
		try {
			Unit attacker = null;
			int workers = 0;
			Set<Unit> attackers = new HashSet<>();
			//Thanks to @N00byEdge to the cleaner code
			for(Unit u : ((GameState)this.handler).getGame().enemy().getUnits()) {
				if(!u.getType().isBuilding() && u.getType().canAttack()) {
					Unit target = (u.getTarget() == null ? u.getOrderTarget() : u.getTarget());
				    if(target != null && target.equals(((GameState)this.handler).chosenHarasser)) {
				        if(u.getType().isWorker()){
				        	workers++;
				        	attacker = u;
				        }
				        attackers.add(u);
				        continue;
				    }
				}
			}
			if(workers > 1){
				((GameState)this.handler).EI.defendHarass = true;
			}
			if(attackers.isEmpty()) {
				if(!((GameState)this.handler).getGame().isVisible(((GameState)this.handler).enemyBase.getTilePosition()) && ((GameState)this.handler).chosenUnitToHarass == null){
					((GameState)this.handler).chosenHarasser.move(((GameState)this.handler).enemyBase.getPosition());
				}
				return State.SUCCESS;
			}
			else{
				boolean winHarass = ((GameState)this.handler).simulateHarass(((GameState)this.handler).chosenHarasser, attackers, 70);
				if(winHarass) {
					if(workers == 1 && !attacker.equals(((GameState)this.handler).chosenUnitToHarass)){
						((GameState)this.handler).chosenHarasser.attack(attacker);
						((GameState)this.handler).chosenUnitToHarass = attacker;
						return State.SUCCESS;
					}
				} else {
					
					if(((GameState)this.handler).chosenHarasser.getHitPoints() <= 15) {
						((GameState)this.handler).workerIdle.add(((GameState)this.handler).chosenHarasser);
						((GameState)this.handler).chosenHarasser.stop();
						((GameState)this.handler).chosenHarasser = null;
						((GameState)this.handler).chosenUnitToHarass = null;
					} else {
						Position kite = getGs().kiteAway(((GameState)this.handler).chosenHarasser, attackers);
						((GameState)this.handler).chosenHarasser.move(kite);
						((GameState)this.handler).chosenUnitToHarass = null;
					}
					
					return State.FAILURE;
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
