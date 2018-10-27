package ecgberht.BehaviourTrees.Scouting;

import bwem.Base;
import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.Worker;

import java.util.ArrayList;
import java.util.List;

public class SendScout extends Action {

    public SendScout(String name, GameState gh) {
        super(name, gh);

    }

    @Override
    public BehavioralTree.State execute() {
        try {
            if (this.handler.enemyMainBase == null) {
                if (!this.handler.scoutSLs.isEmpty()) {
                    List<Base> aux = new ArrayList<>();
                    for (Base b : this.handler.scoutSLs) {
                        if (this.handler.fortressSpecialBLs.containsKey(b)) continue;
                        if (this.handler.strat.name.equals("PlasmaWraithHell")) {
                            if (((MobileUnit) this.handler.chosenScout).move(b.getLocation().toPosition())) {
                                return BehavioralTree.State.SUCCESS;
                            }
                        } else if (Util.isConnected(b.getLocation(), this.handler.chosenScout.getTilePosition())) {
                            if (((MobileUnit) this.handler.chosenScout).move(b.getLocation().toPosition())) {
                                return BehavioralTree.State.SUCCESS;
                            }
                        } else aux.add(b);
                    }
                    this.handler.scoutSLs.removeAll(aux);
                }
            }
            if (this.handler.strat.name.equals("PlasmaWraithHell")) {
                ((MobileUnit) this.handler.chosenScout).stop(false);
                this.handler.chosenScout = null;
                return BehavioralTree.State.FAILURE;
            }
            this.handler.workerIdle.add((Worker) this.handler.chosenScout);
            ((MobileUnit) this.handler.chosenScout).stop(false);
            this.handler.chosenScout = null;
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}
