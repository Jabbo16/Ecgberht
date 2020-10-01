package ecgberht.BehaviourTrees.Scouting;

import ecgberht.GameState;
import ecgberht.UnitInfo;
import ecgberht.Util.BaseLocationComparator;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.Worker;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class CheckEnemyBaseVisible extends Action {

    public CheckEnemyBaseVisible(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            List<PlayerUnit> enemies = gameState.getGame().getUnits(gameState.getIH().enemy());
            if (!enemies.isEmpty()) {
                for (UnitInfo u : gameState.unitStorage.getEnemyUnits().values().stream().filter(u -> u.unitType.isBuilding()).collect(Collectors.toSet())) {
                    if (Util.broodWarDistance(gameState.chosenScout.getPosition(), u.lastPosition) <= 500) {
                        gameState.enemyMainBase = Util.getClosestBaseLocation(u.lastPosition);
                        gameState.scoutSLs = new HashSet<>();
                        if (!gameState.getStrat().name.equals("PlasmaWraithHell")) {
                            gameState.chosenHarasser = (Worker) gameState.chosenScout;
                        }
                        gameState.chosenScout = null;
                        Util.sendText("!");
                        gameState.playSound("gearthere.mp3");
                        if (gameState.enemyStartBase == null) {
                            gameState.enemyBLs.clear();
                            gameState.enemyBLs.addAll(gameState.BLs);
                            gameState.enemyBLs.sort(new BaseLocationComparator(gameState.enemyMainBase));
                            if (gameState.firstScout) {
                                gameState.enemyStartBase = gameState.enemyMainBase;
                                gameState.enemyMainArea = gameState.enemyStartBase.getArea();
                                gameState.enemyNaturalBase = gameState.enemyBLs.get(1);
                                gameState.enemyNaturalArea = gameState.enemyNaturalBase.getArea();
                            }
                        }
                        return State.SUCCESS;
                    }
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