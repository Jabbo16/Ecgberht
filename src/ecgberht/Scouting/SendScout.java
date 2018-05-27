package ecgberht.Scouting;

import bwem.Base;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import java.util.ArrayList;
import java.util.List;

public class SendScout extends Action {

    public SendScout(String name, GameHandler gh) {
        super(name, gh);

    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).enemyBase == null) {
                if (!((GameState) this.handler).ScoutSLs.isEmpty()) {
                    List<Base> aux = new ArrayList<>();
                    for (Base b : ((GameState) this.handler).ScoutSLs) {
                        if (((GameState) this.handler).bwta.isConnected(b.getLocation(), ((GameState) this.handler).chosenScout.getTilePosition())) {
                            if (((GameState) this.handler).chosenScout.move(b.getLocation().toPosition())) {
                                return State.SUCCESS;
                            }
                        } else {
                            aux.add(b);
                        }
                    }
                    ((GameState) this.handler).ScoutSLs.removeAll(aux);
                }
            }
            ((GameState) this.handler).workerIdle.add(((GameState) this.handler).chosenScout);
            ((GameState) this.handler).chosenScout.stop(false);
            ((GameState) this.handler).chosenScout = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
