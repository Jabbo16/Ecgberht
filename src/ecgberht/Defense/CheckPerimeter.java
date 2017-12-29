package ecgberht.Defense;

import java.util.ArrayList;
import java.util.List;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;
import ecgberht.GameState;

import bwapi.Pair;
import bwapi.Position;
import bwapi.Unit;

public class CheckPerimeter extends Conditional {

	public CheckPerimeter(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {

		try {
			((GameState)this.handler).enemyInBase = new ArrayList<Unit>();
			for (Unit u : ((GameState)this.handler).getGame().enemy().getUnits()) {
				if(!u.getType().isBuilding()) {
					for(Unit c : ((GameState)this.handler).CCs) {
						if (((GameState)this.handler).getGame().getUnitsInRadius(c.getPosition(), 500).contains(u) && !((GameState)this.handler).enemyInBase.contains(u)) {
							((GameState)this.handler).enemyInBase.add(u);
						}
					}
					for(Pair<Unit,List<Unit> > b : ((GameState)this.handler).DBs) {
						if (((GameState)this.handler).getGame().getUnitsInRadius(b.first.getPosition(), 200).contains(u) && !((GameState)this.handler).enemyInBase.contains(u)) {
							((GameState)this.handler).enemyInBase.add(u);
						}
					}
					for(Unit b : ((GameState)this.handler).SBs) {
						if (((GameState)this.handler).getGame().getUnitsInRadius(b.getPosition(), 200).contains(u) && !((GameState)this.handler).enemyInBase.contains(u)) {
							((GameState)this.handler).enemyInBase.add(u);
						}
					}
					for(Unit b : ((GameState)this.handler).MBs) {
						if (((GameState)this.handler).getGame().getUnitsInRadius(b.getPosition(), 200).contains(u) && !((GameState)this.handler).enemyInBase.contains(u)) {
							((GameState)this.handler).enemyInBase.add(u);
						}
					}
				}
			}
			if(!((GameState)this.handler).enemyInBase.isEmpty()) {
				((GameState)this.handler).defense = true;
				return State.SUCCESS;
			}
			for(Pair<Unit, Position> u : ((GameState)this.handler).workerDefenders) {
				u.first.stop();
			}
			((GameState)this.handler).defense = false;
			return State.FAILURE;
		} catch(Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
}
