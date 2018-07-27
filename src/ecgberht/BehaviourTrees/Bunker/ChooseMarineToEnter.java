package ecgberht.BehaviourTrees.Bunker;

import ecgberht.GameState;
import ecgberht.Squad;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.unit.Bunker;
import org.openbw.bwapi4j.unit.Marine;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.util.Pair;

import java.util.Map.Entry;

public class ChooseMarineToEnter extends Action {

    public ChooseMarineToEnter(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).squads.isEmpty()) {
                return State.FAILURE;
            }
            for (Bunker b : ((GameState) this.handler).DBs.keySet()) {
                if (b.getTilePosition().equals(((GameState) this.handler).chosenBunker.getTilePosition())) {
                    Pair<String, Unit> closest = null;
                    for (Entry<String, Squad> s : ((GameState) this.handler).squads.entrySet()) {
                        for (Unit u : s.getValue().members) {
                            if (u instanceof Marine) {
                                if ((closest == null || ((GameState) this.handler).broodWarDistance(b.getPosition(), u.getPosition()) <
                                        ((GameState) this.handler).broodWarDistance(b.getPosition(), closest.second.getPosition()))) {
                                    closest = new Pair<>(s.getKey(), u);
                                }
                            }
                        }
                    }
                    if (closest != null) {
                        ((GameState) this.handler).chosenMarine = closest;
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
