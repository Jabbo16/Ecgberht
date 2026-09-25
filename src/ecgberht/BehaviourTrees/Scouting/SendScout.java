package ecgberht.BehaviourTrees.Scouting;
import ecgberht.brain.*;

import bwem.Base;
import ecgberht.GameState;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.Worker;

import java.util.ArrayList;
import java.util.List;

public class SendScout extends BrainAction {

    public SendScout(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.enemyMainBase == null) {
                if (!gameState.scoutSLs.isEmpty()) {
                    List<Base> aux = new ArrayList<>();
                    for (Base b : gameState.scoutSLs) {
                        if (gameState.fortressSpecialBLs.containsKey(b)) continue;
                        if (gameState.getStrat().name.equals("PlasmaWraithHell")) {
                            if (((MobileUnit) gameState.chosenScout).move(b.getLocation().toPosition())) {
                                return BrainStatus.SUCCESS;
                            }
                        } else if (Util.isConnected(b.getLocation(), gameState.chosenScout.getTilePosition())) {
                            if (((MobileUnit) gameState.chosenScout).move(b.getLocation().toPosition())) {
                                return BrainStatus.SUCCESS;
                            }
                        } else aux.add(b);
                    }
                    gameState.scoutSLs.removeAll(aux);
                }
            }
            if (gameState.getStrat().name.equals("PlasmaWraithHell")) {
                ((MobileUnit) gameState.chosenScout).stop(false);
                gameState.chosenScout = null;
                return BrainStatus.FAILURE;
            }
            gameState.workerIdle.add((Worker) gameState.chosenScout);
            ((MobileUnit) gameState.chosenScout).stop(false);
            gameState.chosenScout = null;
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}

