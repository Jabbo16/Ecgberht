package ecgberht.BehaviourTrees.Bunker;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.unit.Bunker;
import org.openbw.bwapi4j.unit.Unit;

import java.util.Map.Entry;
import java.util.Set;

public class ChooseBunkerToLoad extends Action {

    public ChooseBunkerToLoad(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            for (Entry<Bunker, Set<Unit>> b : this.handler.DBs.entrySet()) {
                if (b.getValue().size() < 4) {
                    this.handler.chosenBunker = b.getKey();
                    return BehavioralTree.State.SUCCESS;
                }
            }
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}
