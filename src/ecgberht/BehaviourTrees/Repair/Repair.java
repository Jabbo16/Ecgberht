package ecgberht.BehaviourTrees.Repair;

import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.MobileUnit;

public class Repair extends Action {

    public Repair(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            if (IntelligenceAgency.getEnemyStrat() == IntelligenceAgency.EnemyStrats.ZealotRush) {
                if (this.handler.chosenRepairer.move(this.handler.chosenUnitRepair.getPosition())) {
                    if (this.handler.workerIdle.contains(this.handler.chosenRepairer)) {
                        this.handler.workerIdle.remove(this.handler.chosenRepairer);
                    } else {
                        if (this.handler.workerMining.containsKey(this.handler.chosenRepairer)) {
                            MineralPatch mineral = this.handler.workerMining.get(this.handler.chosenRepairer);
                            this.handler.workerMining.remove(this.handler.chosenRepairer);
                            if (this.handler.mineralsAssigned.containsKey(mineral)) {
                                this.handler.mining--;
                                this.handler.mineralsAssigned.put(mineral, this.handler.mineralsAssigned.get(mineral) - 1);
                            }
                        }
                    }
                    this.handler.repairerTask.put(this.handler.chosenRepairer, this.handler.chosenUnitRepair);
                    this.handler.chosenUnitRepair = null;
                    this.handler.chosenRepairer = null;
                    return BehavioralTree.State.SUCCESS;
                }
            } else if (this.handler.chosenRepairer.repair(this.handler.chosenUnitRepair)) {
                if (this.handler.workerIdle.contains(this.handler.chosenRepairer)) {
                    this.handler.workerIdle.remove(this.handler.chosenRepairer);
                } else {
                    if (this.handler.workerMining.containsKey(this.handler.chosenRepairer)) {
                        MineralPatch mineral = this.handler.workerMining.get(this.handler.chosenRepairer);
                        this.handler.workerMining.remove(this.handler.chosenRepairer);
                        if (this.handler.mineralsAssigned.containsKey(mineral)) {
                            this.handler.mining--;
                            this.handler.mineralsAssigned.put(mineral, this.handler.mineralsAssigned.get(mineral) - 1);
                        }
                    }
                }
                this.handler.repairerTask.put(this.handler.chosenRepairer, this.handler.chosenUnitRepair);
                if (this.handler.chosenUnitRepair instanceof MobileUnit) {
                    ((MobileUnit) this.handler.chosenUnitRepair).move(this.handler.chosenRepairer.getPosition());
                }
                this.handler.chosenUnitRepair = null;
                this.handler.chosenRepairer = null;
                return BehavioralTree.State.SUCCESS;
            }
            this.handler.chosenUnitRepair = null;
            this.handler.chosenRepairer = null;
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}
