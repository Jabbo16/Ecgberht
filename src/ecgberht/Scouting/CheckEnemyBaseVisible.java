package ecgberht.Scouting;

import java.util.HashSet;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Unit;
import bwapi.Utils;
import bwta.BWTA;
import bwta.BaseLocation;
//import ecgberht.BaseLocationComparator;
import ecgberht.GameState;

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
						if (((GameState)this.handler).getGame().getUnitsInRadius(((GameState)this.handler).chosenScout.getPosition(), 500).contains(u)) {
							((GameState)this.handler).enemyBase = BWTA.getNearestBaseLocation(u.getTilePosition());
							((GameState)this.handler).ScoutSLs = new HashSet<BaseLocation>();
							//((GameState)this.handler).choosenScout.stop();
							//((GameState)this.handler).workerIdle.add(((GameState)this.handler).choosenScout);
							((GameState)this.handler).chosenHarasser = ((GameState)this.handler).chosenScout;
							((GameState)this.handler).chosenScout = null;
							((GameState)this.handler).getGame().sendText(Utils.formatText("!",Utils.Yellow));
							((GameState)this.handler).playSound("gear.mp3");
							((GameState)this.handler).EnemyBLs.clear();
							((GameState)this.handler).EnemyBLs.addAll(((GameState)this.handler).BLs);
							//((GameState)this.handler).EnemyBLs.sort(new BaseLocationComparator(true));
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