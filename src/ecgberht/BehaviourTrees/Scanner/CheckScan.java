package ecgberht.BehaviourTrees.Scanner;

import bwem.Base;
import ecgberht.GameState;
import ecgberht.UnitInfo;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.*;
import org.openbw.bwapi4j.unit.Attacker;
import org.openbw.bwapi4j.unit.ComsatStation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CheckScan extends BrainAction {

    public CheckScan(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.CSs.isEmpty()) return BrainStatus.FAILURE;
            if (gameState.frameCount - gameState.startCount > 40 + gameState.getIH().getLatency()) {
                for (ComsatStation u : gameState.CSs) {
                    if (u.getEnergy() < 50) continue;
                    for (UnitInfo e : gameState.unitStorage.getEnemyUnits().values()) {
                        if ((e.unit.isCloaked() || e.burrowed) && !e.unit.isDetected() && e.unit instanceof Attacker) {
                            if (gameState.sim.getSimulation(e, true).allies.stream().noneMatch(a -> a.unitType.canAttack()))
                                continue;
                            gameState.checkScan = new MutablePair<>(u, e.lastPosition);
                            return BrainStatus.SUCCESS;
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
            if (valid.isEmpty()) return BrainStatus.FAILURE;
            for (ComsatStation u : gameState.CSs) {
                if (u.getEnergy() == 200) {
                    gameState.checkScan = new MutablePair<>(u,
                            Util.getUnitCenterPosition(valid.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(valid.size())).getLocation().toPosition(), gameState.enemyRace.getCenter()));
                    return BrainStatus.SUCCESS;
                }
            }
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}

