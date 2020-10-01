package ecgberht.BehaviourTrees.Scouting;

import ecgberht.GameState;
import ecgberht.Squad;
import ecgberht.UnitInfo;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.Worker;
import org.openbw.bwapi4j.unit.Wraith;

public class ChooseScout extends Action {

    public ChooseScout(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.getStrat().name.equals("PlasmaWraithHell")) {
                for (Squad s : gameState.sqManager.squads.values()) {
                    for (UnitInfo u : s.members) {
                        if (u.unit instanceof Wraith) {
                            gameState.chosenScout = (MobileUnit) u.unit;
                            s.members.remove(u);
                            return State.SUCCESS;
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
                for (Worker u : gameState.workerMining.keySet()) {
                    if (!u.isCarryingMinerals()) {
                        gameState.chosenScout = u;
                        MineralPatch mineral = gameState.workerMining.get(u);
                        if (gameState.mineralsAssigned.containsKey(mineral)) {
                            gameState.mining--;
                            gameState.mineralsAssigned.put(mineral, gameState.mineralsAssigned.get(mineral) - 1);
                        }
                        gameState.workerMining.remove(u);
                        break;
                    }
                }
            }
            if (gameState.chosenScout != null) return State.SUCCESS;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
