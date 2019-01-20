package ecgberht.BehaviourTrees.Scanner;

import bwem.Base;
import ecgberht.GameState;
import ecgberht.UnitInfo;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;
import org.openbw.bwapi4j.unit.Attacker;
import org.openbw.bwapi4j.unit.Burrowable;
import org.openbw.bwapi4j.unit.ComsatStation;
import org.openbw.bwapi4j.unit.PlayerUnit;

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
            if (gameState.frameCount - gameState.startCount > 40 + gameState.getIH().getLatency()) {
                for (ComsatStation u : gameState.CSs) {
                    if(u.getEnergy() < 50) continue;
                    for (UnitInfo e : gameState.unitStorage.getEnemyUnits().values()) {
                        if ((e.unit.isCloaked() || e.burrowed) && !e.unit.isDetected() && e.unit instanceof Attacker) {
                            if (gameState.sim.getSimulation(e, true).allies.stream().noneMatch(a -> a.unitType.canAttack())) continue;
                            gameState.checkScan = new MutablePair<>(u, e.lastPosition);
                            return State.SUCCESS;
                        }
                    }
                }

            }
            List<Base> valid = new ArrayList<>();
            for (Base b : gameState.enemyBLs) {
                if (gameState.getGame().getBWMap().isVisible(b.getLocation()) || b.getArea().getAccessibleNeighbors().isEmpty()) {
                    continue;
                }
                if (gameState.enemyMainBase != null && gameState.enemyMainBase.getLocation().equals(b.getLocation())) {
                    continue;
                }
                valid.add(b);
            }
            if (valid.isEmpty()) return State.FAILURE;
            for (ComsatStation u : gameState.CSs) {
                if (u.getEnergy() == 200) {
                    Random random = new Random();
                    gameState.checkScan = new MutablePair<>(u,
                            Util.getUnitCenterPosition(valid.get(random.nextInt(valid.size())).getLocation().toPosition(), gameState.enemyRace.getCenter()));
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
