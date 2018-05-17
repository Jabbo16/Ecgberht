package ecgberht.Defense;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.util.Pair;

public class ChooseDefensePosition extends Conditional {

    public ChooseDefensePosition(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).defense) {
                for (Unit u : ((GameState) this.handler).enemyInBase) {
                    ((GameState) this.handler).inMapUnits.updateMap(u, false);
                }
                Pair<Integer, Integer> p = ((GameState) this.handler).inMapUnits.getPosition(((GameState) this.handler).initDefensePosition, false);
                if (p.first != null && p.second != null) {
                    ((GameState) this.handler).attackPosition = new TilePosition(p.second, p.first).toPosition();
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
