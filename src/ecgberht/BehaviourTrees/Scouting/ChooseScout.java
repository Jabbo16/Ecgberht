package ecgberht.BehaviourTrees.Scouting;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.Squad;
import ecgberht.UnitInfo;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.Worker;
import org.openbw.bwapi4j.unit.Wraith;

public class ChooseScout extends BrainAction {

    public ChooseScout(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.getStrat().name.equals("PlasmaWraithHell")) {
                for (Squad s : gameState.sqManager.squads.values()) {
                    for (UnitInfo u : s.members) {
                        if (u.unit instanceof Wraith) {
                            gameState.chosenScout = (MobileUnit) u.unit;
                            s.members.remove(u);
                            return BrainStatus.SUCCESS;
                        }
                    }
                }
            }
            if (!gameState.workerIdle.isEmpty()) {
                Worker chosen = gameState.workerIdle.iterator().next();
                gameState.chosenScout = chosen;
                gameState.workerIdle.remove(chosen);
            }
            if (gameState.chosenScout == null) {
                for (Worker u : gameState.eco.workerMining.keySet()) {
                    if (!u.isCarryingMinerals()) {
                        gameState.chosenScout = u;
                        MineralPatch mineral = gameState.eco.workerMining.get(u);
                        if (gameState.eco.mineralsAssigned.containsKey(mineral)) {
                            gameState.eco.mining--;
                            gameState.eco.mineralsAssigned.put(mineral, gameState.eco.mineralsAssigned.get(mineral) - 1);
                        }
                        gameState.eco.workerMining.remove(u);
                        break;
                    }
                }
            }
            if (gameState.chosenScout != null) return BrainStatus.SUCCESS;
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}






