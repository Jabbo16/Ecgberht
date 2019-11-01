package ecgberht.BehaviourTrees.Scanner;

import bwapi.Unit;
import bwem.Base;
import ecgberht.GameState;
import ecgberht.UnitInfo;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CheckScan extends Conditional {

    public CheckScan(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.CSs.isEmpty()) return State.FAILURE;
            if (gameState.frameCount - gameState.startCount > 40 + gameState.getGame().getLatencyFrames()) {
                for (Unit u : gameState.CSs) {
                    if (u.getEnergy() < 50) continue;
                    for (UnitInfo e : gameState.unitStorage.getEnemyUnits().values()) {
                        if ((e.unit.isCloaked() || e.burrowed) && !e.unit.isDetected() && e.unitType.canAttack()) {
                            if (gameState.sim.getSimulation(e, true).allies.stream().noneMatch(a -> a.unitType.canAttack()))
                                continue;
                            gameState.checkScan = new MutablePair<>(u, e.lastPosition);
                            return State.SUCCESS;
                        }
                    }
                }
            }
            List<Base> valid = new ArrayList<>();
            for (Base b : gameState.enemyBLs) {
                if (gameState.bw.isVisible(b.getLocation()) || b.getArea().getAccessibleNeighbors().isEmpty()) {
                    continue;
                }
                if (gameState.enemyMainBase != null && gameState.enemyMainBase.getLocation().equals(b.getLocation())) {
                    continue;
                }
                valid.add(b);
            }
            if (valid.isEmpty()) return State.FAILURE;
            for (Unit u : gameState.CSs) {
                if (u.getEnergy() == 200) {
                    Random random = new Random();
                    // What ??
                    //gameState.checkScan = new MutablePair<>(u,Util.getUnitCenterPosition(valid.get(random.nextInt(valid.size())).getLocation().toPosition(), gameState.enemyRace.getCenter()));
                    gameState.checkScan = new MutablePair<>(u, valid.get(random.nextInt(valid.size())).getLocation().toPosition());
                    return State.SUCCESS;
                }
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
