package ecgberht.Attack;

import ecgberht.GameState;
import ecgberht.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.UnitType;

public class CheckArmy extends Conditional {

    public CheckArmy(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).strat.name == "ProxyBBS") {

                //if (((GameState) this.handler).getArmySize() >= ((GameState) this.handler).strat.armyForAttack && Util.countUnitTypeSelf(UnitType.Terran_Marine) > 4 && ((GameState) this.handler).armyGroupedBBS()) {
                if (((GameState) this.handler).getArmySize() >= ((GameState) this.handler).strat.armyForAttack && Util.countUnitTypeSelf(UnitType.Terran_Marine) > 4) {
//				if(((GameState)this.handler).getArmySize() >= ((GameState)this.handler).strat.armyForAttack && ((GameState)this.handler).getPlayer().allUnitCount(UnitType.Terran_Marine) > 4) {
                    return State.SUCCESS;
                }
            }
            if (((GameState) this.handler).getArmySize() >= ((GameState) this.handler).strat.armyForAttack && !((GameState) this.handler).defense) {
                return State.SUCCESS;
            } else if (((GameState) this.handler).defense) {
                if (!((GameState) this.handler).enemyInBase.isEmpty()) {
                    if ((((GameState) this.handler).getArmySize() > 50 && ((GameState) this.handler).getArmySize() / ((GameState) this.handler).enemyInBase.size() > 10)) {
                        return State.SUCCESS;
                    }
                }
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
