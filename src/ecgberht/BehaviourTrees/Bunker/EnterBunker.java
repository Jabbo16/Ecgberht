package ecgberht.BehaviourTrees.Bunker;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.unit.Bunker;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.Unit;

import java.util.Map.Entry;
import java.util.Set;

public class EnterBunker extends Action {

    public EnterBunker(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            MutablePair<Integer, Unit> m = this.handler.chosenMarine;
            for (Entry<Bunker, Set<Unit>> b : this.handler.DBs.entrySet()) {
                if (b.getKey().equals(this.handler.chosenBunker)) {
                    if (b.getKey().load((MobileUnit) m.second)) {
                        b.getValue().add(m.second);
                        this.handler.sqManager.squads.get(m.first).members.remove(m.second);
                        return BehavioralTree.State.SUCCESS;
                    }
                }
                return BehavioralTree.State.SUCCESS;
            }
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}
