package ecgberht.BehaviourTrees.Harass;

import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.UnitInfo;
import ecgberht.Util.UtilMicro;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;
import bwapi.Position;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class CheckHarasserAttacked extends Conditional {
    public CheckHarasserAttacked(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.enemyMainBase == null) {
                gameState.chosenUnitToHarass = null;
                gameState.chosenHarasser = null;
                return State.FAILURE;
            }
            if (gameState.chosenUnitToHarass != null) {
                if (!gameState.chosenUnitToHarass.getPosition().isValid(gameState.bw)) {
                    gameState.chosenUnitToHarass = null;
                }
            }
            UnitInfo attacker = null;
            int workers = 0;
            Set<UnitInfo> attackers = new TreeSet<>();
            //Thanks to @N00byEdge for cleaner code
            for (UnitInfo u : gameState.unitStorage.getAllyUnits().get(gameState.chosenHarasser).attackers) {
                if (u.unit.exists() && !u.unitType.isBuilding() && u.isAttacker()) {
                    if (u.unitType.isWorker()) {
                        workers++;
                        attacker = u;
                    }
                    attackers.add(u);
                }
            }
            if (workers > 1) {
                gameState.learningManager.setHarass(true);
                gameState.chosenUnitToHarass = null;
                return State.FAILURE;
            }
            if (attackers.isEmpty()) {
                if (!gameState.bw.isVisible(gameState.enemyMainBase.getLocation()) &&
                        gameState.chosenUnitToHarass == null) {
                    gameState.chosenHarasser.move(gameState.enemyMainBase.getLocation().toPosition());
                }
                return State.SUCCESS;
            } else {
                boolean winHarass = gameState.sim.simulateHarass(gameState.chosenHarasser, attackers, 70);
                if (winHarass) {
                    if (workers == 1 && !attacker.unit.equals(gameState.chosenUnitToHarass)) {
                        UtilMicro.attack(gameState.chosenHarasser, attacker);
                        gameState.chosenUnitToHarass = attacker.unit;
                        return State.SUCCESS;
                    }
                } else {
                    if (IntelligenceAgency.getEnemyStrat() == IntelligenceAgency.EnemyStrats.Unknown) {
                        gameState.explore = true;
                        gameState.chosenUnitToHarass = null;
                        gameState.chosenHarasser.stop(false);
                        return State.FAILURE;
                    } else if (gameState.chosenHarasser.getHitPoints() <= 15) {
                        gameState.workerIdle.add(gameState.chosenHarasser);
                        gameState.chosenHarasser.stop(false);
                        gameState.chosenHarasser = null;
                        gameState.chosenUnitToHarass = null;
                    } else {
                        //Position kite = UtilMicro.kiteAway(gameState.chosenHarasser, attackers);
                        Optional<UnitInfo> closestUnit = attackers.stream().min(Comparator.comparing(u -> u.getDistance(gameState.chosenHarasser)));
                        Position kite = closestUnit.map(unit1 -> UtilMicro.kiteAwayAlt(gameState.chosenHarasser.getPosition(), unit1.position)).orElse(null);
                        if (kite != null && kite.isValid(gameState.bw)) {
                            UtilMicro.move(gameState.chosenHarasser, kite);
                            gameState.chosenUnitToHarass = null;
                        } else {
                            kite = UtilMicro.kiteAway(gameState.chosenHarasser, attackers);
                            if (kite != null && kite.isValid(gameState.bw)) {
                                UtilMicro.move(gameState.chosenHarasser, kite);
                                gameState.chosenUnitToHarass = null;
                            }
                        }
                    }
                    return State.FAILURE;
                }
            }
            return State.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }

}
