package ecgberht.Expansion;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwta.BWTA;
import bwta.BaseLocation;
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
			Position main = null;
			if(((GameState)this.handler).MainCC != null) {
				main = ((GameState)this.handler).MainCC.getPosition();
			}
			else {
				main = ((GameState)this.handler).getPlayer().getStartLocation().toPosition();
			}
			TilePosition closestBase = null;
			for(BaseLocation b : ((GameState)this.handler).BLs) {
				boolean close = false;
				for(Unit cc : ((GameState)this.handler).CCs) {
					if(BWTA.getRegion(cc.getTilePosition()).getCenter().equals(BWTA.getRegion(b.getTilePosition()).getCenter())) {
						close = true;
					}
				}
				if(!close && BWTA.isConnected(b.getTilePosition(), main.toTilePosition())) {
					boolean found = false;
					if(!((GameState)this.handler).getGame().enemy().getUnits().isEmpty()) {
						for(Unit u : ((GameState)this.handler).getGame().enemy().getUnits()) {
							//if(u.getDistance(b) < 200) {
							if(BWTA.getRegion(u.getPosition()) == null) {
								continue;
							}
							if(BWTA.getRegion(u.getPosition()).getCenter().equals(BWTA.getRegion(b.getPosition()).getCenter())) {
								found = true;
								break;
							}
						}
					}
					if (!found && (closestBase == null || BWTA.getGroundDistance(b.getTilePosition(), main.toTilePosition()) < BWTA.getGroundDistance(closestBase, main.toTilePosition()))) {
						closestBase = b.getTilePosition();
					}
				}
			}
			if(closestBase != null) {
				((GameState)this.handler).chosenBaseLocation = closestBase;
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