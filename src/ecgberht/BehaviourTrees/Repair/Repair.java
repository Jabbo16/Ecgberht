package ecgberht.BehaviourTrees.Repair;

import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.MobileUnit;

public class Repair extends Action {

    public Repair(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            boolean cheesed = IntelligenceAgency.getEnemyStrat() == IntelligenceAgency.EnemyStrats.ZealotRush && this.handler.frameCount >= 24 * 60 * 2.2;
            boolean fastExpanding = this.handler.strat.name.contains("GreedyFE") && Util.countBuildingAll(UnitType.Terran_Command_Center) == 2 && this.handler.CCs.size() < 2 && this.handler.firstExpand;
            if (cheesed || fastExpanding) {
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
                    return State.SUCCESS;
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
                return State.SUCCESS;
            }
            this.handler.chosenUnitRepair = null;
            this.handler.chosenRepairer = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
