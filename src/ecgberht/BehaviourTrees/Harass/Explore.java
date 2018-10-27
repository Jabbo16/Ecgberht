package ecgberht.BehaviourTrees.Harass;

import ecgberht.Agents.WorkerScoutAgent;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Conditional;

public class Explore extends Conditional {

    public Explore(String name, GameState gh) {
        super(name, gh);
    }

    private WorkerScoutAgent scout = null;

    @Override
    public BehavioralTree.State execute() {
        try {
            if (scout == null && this.handler.chosenHarasser != null) {
                this.handler.agents.put(this.handler.chosenHarasser, new WorkerScoutAgent(this.handler.chosenHarasser, this.handler.enemyMainBase));
                this.handler.chosenHarasser = null;
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
