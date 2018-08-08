package ecgberht.BehaviourTrees.Bunker;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.unit.Bunker;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.Unit;

import java.util.Map.Entry;
import java.util.Set;

public class EnterBunker extends Action {

    public EnterBunker(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            MutablePair<String, Unit> m = ((GameState) this.handler).chosenMarine;
            for (Entry<Bunker, Set<Unit>> b : ((GameState) this.handler).DBs.entrySet()) {
                if (b.getKey().equals(((GameState) this.handler).chosenBunker)) {
                    if (b.getKey().load((MobileUnit) m.second)) {
                        b.getValue().add(m.second);
                        ((GameState) this.handler).squads.get(m.first).members.remove(m.second);
                        return State.SUCCESS;
                    }
                }
                return State.SUCCESS;
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
