package ecgberht.Scanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.unit.Attacker;
import org.openbw.bwapi4j.unit.Burrowable;
import org.openbw.bwapi4j.unit.ComsatStation;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.Unit;

import bwem.Base;
import ecgberht.GameState;

public class CheckScan extends Conditional {

	public CheckScan(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() { // TODO Test timer
		try {
			if(((GameState)this.handler).CSs.isEmpty()) {
				return State.FAILURE;
			}
			if(((GameState)this.handler).getIH().getFrameCount() - ((GameState)this.handler).startCount > 24) {
				for (Unit u : ((GameState)this.handler).enemyCombatUnitMemory) {
					PlayerUnit pU = (PlayerUnit)u;
					if((pU.isCloaked() || (pU instanceof Burrowable && ((Burrowable)pU).isBurrowed())) && !pU.isDetected() && u instanceof Attacker) {
						((GameState)this.handler).checkScan = u.getTilePosition();
						return State.SUCCESS;
					}
				}
			}
			List<Base> valid = new ArrayList<>();
			for(Base b : ((GameState)this.handler).EnemyBLs) {
				if(((GameState)this.handler).getGame().getBWMap().isVisible(b.getLocation()) || b.getArea().getAccessibleNeighbors().isEmpty()){
					continue;
				}
				if(((GameState)this.handler).enemyBase != null) {
					if(((GameState)this.handler).enemyBase.getLocation().equals(b.getLocation())) {
						continue;
					}
				}
				valid.add(b);
			}
			if(valid.isEmpty()) {
				return State.FAILURE;
			}
			for(ComsatStation u: ((GameState)this.handler).CSs) {
				if(u.getEnergy() == 200) {
					Random random = new Random();
					((GameState)this.handler).checkScan = valid.get(random.nextInt(valid.size())).getLocation();
					return State.SUCCESS;
				}
			}
			return State.FAILURE;
		} catch (Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
}
