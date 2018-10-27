package ecgberht.BehaviourTrees.Bunker;

import ecgberht.GameState;
import ecgberht.Squad;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.unit.Bunker;
import org.openbw.bwapi4j.unit.Marine;
import org.openbw.bwapi4j.unit.Unit;

import java.util.Map.Entry;

public class ChooseMarineToEnter extends Action {

    public ChooseMarineToEnter(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (this.handler.sqManager.squads.isEmpty()) {
                return State.FAILURE;
            }
            for (Bunker b : this.handler.DBs.keySet()) {
                if (b.getTilePosition().equals(this.handler.chosenBunker.getTilePosition())) {
                    MutablePair<Integer, Unit> closest = null;
                    for (Entry<Integer, Squad> s : this.handler.sqManager.squads.entrySet()) {
                        for (Unit u : s.getValue().members) {
                            if (u instanceof Marine && (closest == null || Util.broodWarDistance(b.getPosition(), u.getPosition()) <
                                    Util.broodWarDistance(b.getPosition(), closest.second.getPosition()))) {
                                closest = new MutablePair<>(s.getKey(), u);
                            }
                        }
                    }
                    if (closest != null) {
                        this.handler.chosenMarine = closest;
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
