package ecgberht.Bunker;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.unit.Bunker;
import org.openbw.bwapi4j.unit.Unit;

import java.util.Map.Entry;
import java.util.Set;

public class ChooseBunkerToLoad extends Action {

    public ChooseBunkerToLoad(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            for (Entry<Bunker, Set<Unit>> b : ((GameState) this.handler).DBs.entrySet()) {
                if (b.getValue().size() < 4) {
                    ((GameState) this.handler).chosenBunker = b.getKey();
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
