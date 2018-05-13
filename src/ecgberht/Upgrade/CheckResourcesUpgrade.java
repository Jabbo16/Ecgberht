package ecgberht.Upgrade;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.util.Pair;

import ecgberht.GameState;

public class CheckResourcesUpgrade extends Conditional {

	public CheckResourcesUpgrade(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			Pair<Integer,Integer> cash = ((GameState)this.handler).getCash();
			if(((GameState)this.handler).chosenUpgrade != null) {
				if(cash.first >= (((GameState)this.handler).chosenUpgrade.mineralPrice(((GameState)this.handler).getPlayer().getUpgradeLevel(((GameState)this.handler).chosenUpgrade)) +
						((GameState)this.handler).deltaCash.first) && cash.second >= (((GameState)this.handler).chosenUpgrade.gasPrice(((GameState)this.handler).getPlayer().getUpgradeLevel(((GameState)this.handler).chosenUpgrade)))
						+ ((GameState)this.handler).deltaCash.second) {
					return State.SUCCESS;
				}
			}
			else if(((GameState)this.handler).chosenResearch != null) {
				if(cash.first >= (((GameState)this.handler).chosenResearch.mineralPrice() + ((GameState)this.handler).deltaCash.first) && cash.second >= (((GameState)this.handler).chosenResearch.gasPrice()) + ((GameState)this.handler).deltaCash.second) {
					return State.SUCCESS;
				}
			}
			return State.FAILURE;
		} catch(Exception e) {
			System.err.println(this.getClass().getSimpleName());
			e.printStackTrace();
			return State.ERROR;
		}
	}
}
