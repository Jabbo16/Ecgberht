package ecgberht.Expansion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.TilePosition;
import bwapi.Unit;
import bwta.BWTA;
import bwta.BaseLocation;
import ecgberht.BaseLocationComparator;
import ecgberht.GameState;

public class ChooseBaseLocation extends Action {

	public ChooseBaseLocation(String name, GameHandler gh) {
		super(name, gh);

	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).chosenBaseLocation != null) {
				return State.SUCCESS;
			}
			TilePosition main = null;
			if(((GameState)this.handler).MainCC != null) {
				main = ((GameState)this.handler).MainCC.getTilePosition();
			}
			else {
				main = ((GameState)this.handler).getPlayer().getStartLocation();
			}
			List<BaseLocation> valid = new ArrayList<>();
			
			for(BaseLocation b : ((GameState)this.handler).BLs) {
				for(Unit cc : ((GameState)this.handler).CCs) {
					if(BWTA.isConnected(b.getTilePosition(), main) && !cc.getTilePosition().equals(b.getTilePosition())) {
						valid.add(b);
						break;
					}
				}
			}
			Collections.sort(valid, new BaseLocationComparator());
			
			for(Unit u : ((GameState)this.handler).enemyCombatUnitMemory) {
				List<BaseLocation> remove = new ArrayList<>();
				if(valid.isEmpty()) {
					break;
				}
				for(BaseLocation b : valid) {
					if(BWTA.getRegion(u.getPosition()) == null || !u.getType().canAttack() || u.getType().isWorker()) {
						continue;
					}
					if(BWTA.getRegion(u.getPosition()).getCenter().equals(BWTA.getRegion(b.getPosition()).getCenter())) {
						remove.add(b);
					}
				}
				valid.removeAll(remove);
			}
			if(valid.isEmpty()) {
				return State.FAILURE;
			}
			((GameState)this.handler).chosenBaseLocation = valid.get(0).getTilePosition();
			return State.SUCCESS;
		} catch(Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
}