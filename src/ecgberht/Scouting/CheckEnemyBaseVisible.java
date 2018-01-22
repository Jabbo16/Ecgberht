package ecgberht.Scouting;

import java.util.HashSet;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;

import bwapi.Unit;
import bwapi.Utils;
import bwta.BWTA;
import bwta.BaseLocation;

public class CheckEnemyBaseVisible extends Action {

	public CheckEnemyBaseVisible(String name, GameHandler gh) {
		super(name, gh);
	}
	
	@Override
	public State execute() {
		try {
			if(!((GameState)this.handler).getGame().enemy().getUnits().isEmpty()) {
				for (Unit u : ((GameState)this.handler).getGame().enemy().getUnits()) {
					if(u.getType().isBuilding()) {
						if (((GameState)this.handler).getGame().getUnitsInRadius(((GameState)this.handler).choosenScout.getPosition(), 500).contains(u)) {
							((GameState)this.handler).enemyBase = BWTA.getNearestBaseLocation(u.getTilePosition());
							((GameState)this.handler).ScoutSLs = new HashSet<BaseLocation>();
							//((GameState)this.handler).choosenScout.stop();
							//((GameState)this.handler).workerIdle.add(((GameState)this.handler).choosenScout);
							((GameState)this.handler).choosenBotherer = ((GameState)this.handler).choosenScout;
							((GameState)this.handler).choosenScout = null;
							((GameState)this.handler).getGame().sendText(Utils.formatText("!",Utils.Yellow));
							((GameState)this.handler).playSound("gear.wav");
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