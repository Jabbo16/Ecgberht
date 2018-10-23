package ecgberht.BehaviourTrees.Harass;

import ecgberht.Agents.WorkerScoutAgent;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;

public class Explore extends Conditional {

    public Explore(String name, GameHandler gh) {
        super(name, gh);
    }

    private WorkerScoutAgent scout = null;

    @Override
    public State execute() {
        try {
            if (scout == null && ((GameState) this.handler).chosenHarasser != null) {
                ((GameState) this.handler).agents.put(((GameState) this.handler).chosenHarasser, new WorkerScoutAgent(((GameState) this.handler).chosenHarasser, ((GameState) this.handler).enemyMainBase));
                ((GameState) this.handler).chosenHarasser = null;
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
