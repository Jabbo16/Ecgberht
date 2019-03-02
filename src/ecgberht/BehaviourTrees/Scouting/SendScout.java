package ecgberht.BehaviourTrees.Scouting;

import bwem.Base;
import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
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
    public State execute() {
        try {
            if (gameState.enemyMainBase == null) {
                if (!gameState.scoutSLs.isEmpty()) {
                    List<Base> aux = new ArrayList<>();
                    for (Base b : gameState.scoutSLs) {
                        if (gameState.fortressSpecialBLs.containsKey(b)) continue;
                        if (gameState.getStrat().name.equals("PlasmaWraithHell")) {
                            if (((MobileUnit) gameState.chosenScout).move(b.getLocation().toPosition())) {
                                return State.SUCCESS;
                            }
                        } else if (Util.isConnected(b.getLocation(), gameState.chosenScout.getTilePosition())) {
                            if (((MobileUnit) gameState.chosenScout).move(b.getLocation().toPosition())) {
                                return State.SUCCESS;
                            }
                        } else aux.add(b);
                    }
                    gameState.scoutSLs.removeAll(aux);
                }
            }
            if (gameState.getStrat().name.equals("PlasmaWraithHell")) {
                ((MobileUnit) gameState.chosenScout).stop(false);
                gameState.chosenScout = null;
                return State.FAILURE;
            }
            gameState.workerIdle.add((Worker) gameState.chosenScout);
            ((MobileUnit) gameState.chosenScout).stop(false);
            gameState.chosenScout = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
