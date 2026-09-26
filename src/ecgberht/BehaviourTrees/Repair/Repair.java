package ecgberht.BehaviourTrees.Repair;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.MobileUnit;

public class Repair extends BrainAction {

    public Repair(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            boolean cheesed = IntelligenceAgency.getEnemyStrat() == IntelligenceAgency.EnemyStrats.ZealotRush && gameState.frameCount >= 24 * 60 * 2.2;
            boolean fastExpanding = gameState.getStrat().name.contains("GreedyFE") && Util.countBuildingAll(UnitType.Terran_Command_Center) == 2 && gameState.CCs.size() < 2 && gameState.firstExpand;
            if (cheesed || fastExpanding) {
                if (gameState.chosenRepairer.move(gameState.chosenUnitRepair.getPosition())) {
                    if (gameState.workerIdle.contains(gameState.chosenRepairer)) {
                        gameState.workerIdle.remove(gameState.chosenRepairer);
                    } else {
                        if (gameState.eco.workerMining.containsKey(gameState.chosenRepairer)) {
                            MineralPatch mineral = gameState.eco.workerMining.get(gameState.chosenRepairer);
                            gameState.eco.workerMining.remove(gameState.chosenRepairer);
                            if (gameState.eco.mineralsAssigned.containsKey(mineral)) {
                                gameState.eco.mining--;
                                gameState.eco.mineralsAssigned.put(mineral, gameState.eco.mineralsAssigned.get(mineral) - 1);
                            }
                        }
                    }
                    gameState.eco.repairerTask.put(gameState.chosenRepairer, gameState.chosenUnitRepair);
                    gameState.chosenUnitRepair = null;
                    gameState.chosenRepairer = null;
                    return BrainStatus.SUCCESS;
                }
            } else if (gameState.chosenRepairer.repair(gameState.chosenUnitRepair)) {
                if (gameState.workerIdle.contains(gameState.chosenRepairer)) {
                    gameState.workerIdle.remove(gameState.chosenRepairer);
                } else {
                    if (gameState.eco.workerMining.containsKey(gameState.chosenRepairer)) {
                        MineralPatch mineral = gameState.eco.workerMining.get(gameState.chosenRepairer);
                        gameState.eco.workerMining.remove(gameState.chosenRepairer);
                        if (gameState.eco.mineralsAssigned.containsKey(mineral)) {
                            gameState.eco.mining--;
                            gameState.eco.mineralsAssigned.put(mineral, gameState.eco.mineralsAssigned.get(mineral) - 1);
                        }
                    }
                }
                gameState.eco.repairerTask.put(gameState.chosenRepairer, gameState.chosenUnitRepair);
                if (gameState.chosenUnitRepair instanceof MobileUnit) {
                    ((MobileUnit) gameState.chosenUnitRepair).move(gameState.chosenRepairer.getPosition());
                }
                gameState.chosenUnitRepair = null;
                gameState.chosenRepairer = null;
                return BrainStatus.SUCCESS;
            }
            gameState.chosenUnitRepair = null;
            gameState.chosenRepairer = null;
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}






