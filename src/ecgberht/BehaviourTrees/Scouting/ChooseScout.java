package ecgberht.BehaviourTrees.Scouting;

import ecgberht.GameState;
import ecgberht.Squad;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.unit.Worker;
import org.openbw.bwapi4j.unit.Wraith;

public class ChooseScout extends Action {

    public ChooseScout(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            if (this.handler.strat.name.equals("PlasmaWraithHell")) {
                for (Squad s : this.handler.sqManager.squads.values()) {
                    for (Unit u : s.members) {
                        if (u instanceof Wraith) {
                            this.handler.chosenScout = u;
                            s.members.remove(u);
                            return BehavioralTree.State.SUCCESS;
                        }
                    }
                }
            }
            if (!this.handler.workerIdle.isEmpty()) {
                Worker chosen = this.handler.workerIdle.iterator().next();
                this.handler.chosenScout = chosen;
                this.handler.workerIdle.remove(chosen);
            }
            if (this.handler.chosenScout == null) {
                for (Worker u : this.handler.workerMining.keySet()) {
                    if (!u.isCarryingMinerals()) {
                        this.handler.chosenScout = u;
                        MineralPatch mineral = this.handler.workerMining.get(u);
                        if (this.handler.mineralsAssigned.containsKey(mineral)) {
                            this.handler.mining--;
                            this.handler.mineralsAssigned.put(mineral, this.handler.mineralsAssigned.get(mineral) - 1);
                        }
                        this.handler.workerMining.remove(u);
                        break;
                    }
                }
            }
            if (this.handler.chosenScout != null) return BehavioralTree.State.SUCCESS;
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}
