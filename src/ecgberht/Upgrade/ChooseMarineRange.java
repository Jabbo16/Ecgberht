package ecgberht.Upgrade;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Unit;
import bwapi.UpgradeType;
import ecgberht.GameState;

public class ChooseMarineRange extends Action {

	public ChooseMarineRange(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			if(((GameState)this.handler).UBs.isEmpty()) {
				return State.FAILURE;
			}
			for(Unit u : ((GameState)this.handler).UBs) {
				if(((GameState)this.handler).getPlayer().getUpgradeLevel(UpgradeType.U_238_Shells) != 1 && u.canUpgrade(UpgradeType.U_238_Shells) && !u.isResearching() && !u.isUpgrading()) {
					((GameState)this.handler).chosenUnitUpgrader = u;
					((GameState)this.handler).chosenUpgrade = UpgradeType.U_238_Shells;
					return State.SUCCESS;
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
