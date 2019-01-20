package ecgberht.BehaviourTrees.Defense;

import bwapi.Unit;
import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.Squad;
import ecgberht.Squad.Status;
import ecgberht.UnitInfo;
import ecgberht.Util.MutablePair;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.Position;
import bwapi.UnitType;

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class SendDefenders extends Action {

    public SendDefenders(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            boolean air_only = true;
            boolean cannon_rush = false;
            for (Unit u : gameState.enemyInBase) {
                if (u.isFlying() || u.isCloaked()) continue;
                if (!cannon_rush && (u.getType() == UnitType.Protoss_Pylon || u.getType() == UnitType.Protoss_Photon_Cannon)) cannon_rush = true;
                air_only = false;
            }
            Set<UnitInfo> friends = new TreeSet<>();
            for (Squad s : gameState.sqManager.squads.values()) friends.addAll(s.members);
            boolean bunker = false;
            if (!gameState.DBs.isEmpty()) {
                for (Unit b : gameState.DBs.keySet()) {
                    friends.add(gameState.unitStorage.getAllyUnits().get(b));
                }
                bunker = true;
            }
            int defenders = 6;
            if (gameState.enemyInBase.size() == 1 && gameState.enemyInBase.iterator().next().getType().isWorker()) {
                defenders = 1;
            }
            MutablePair<Boolean, Boolean> battleWin = new MutablePair<>(true, false);
            if (defenders != 1 && IntelligenceAgency.enemyIsRushing()) {
                if (gameState.enemyInBase.size() + friends.size() < 40) {
                    battleWin = gameState.sim.simulateDefenseBattle(friends, gameState.enemyInBase, 150, bunker);
                }
                //if (gameState.enemyInBase.size() >= 3 * friends.size()) battleWin.first = false;
            }
            if (cannon_rush) battleWin.first = false;
            int frame = gameState.frameCount;
            int notFound = 0;
            if (!air_only && ((!battleWin.first || battleWin.second) || defenders == 1)) {
                while (gameState.workerDefenders.size() + notFound < defenders && !gameState.workerIdle.isEmpty()) {
                    Unit closestWorker = null;
                    Position chosen = gameState.attackPosition;
                    for (Unit u : gameState.workerIdle) {
                        if (u.getLastCommandFrame() == frame) continue;
                        if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
                            closestWorker = u;
                        }
                    }
                    if (closestWorker != null) {
                        gameState.workerDefenders.put(closestWorker, null);
                        gameState.workerIdle.remove(closestWorker);
                    } else notFound++;
                }
                notFound = 0;
                while (gameState.workerDefenders.size() + notFound < defenders && !gameState.workerMining.isEmpty()) {
                    Unit closestWorker = null;
                    Position chosen = gameState.attackPosition;
                    for (Entry<Unit, Unit> u : gameState.workerMining.entrySet()) {
                        if (u.getKey().getLastCommandFrame() == frame) continue;
                        if ((closestWorker == null || u.getKey().getDistance(chosen) < closestWorker.getDistance(chosen))) {
                            closestWorker = u.getKey();
                        }
                    }
                    if (closestWorker != null) {
                        if (gameState.workerMining.containsKey(closestWorker)) {
                            Unit mineral = gameState.workerMining.get(closestWorker);
                            gameState.workerDefenders.put(closestWorker, null);
                            if (gameState.mineralsAssigned.containsKey(mineral)) {
                                gameState.mining--;
                                gameState.mineralsAssigned.put(mineral, gameState.mineralsAssigned.get(mineral) - 1);
                            }
                            gameState.workerMining.remove(closestWorker);
                        }
                    } else notFound++;
                }
                for (Entry<Unit, Position> u : gameState.workerDefenders.entrySet()) {
                    if (frame == u.getKey().getLastCommandFrame()) continue;
                    if (gameState.attackPosition != null) {
                        gameState.workerDefenders.put(u.getKey(), gameState.attackPosition);
                        if (gameState.enemyInBase.size() == 1 && gameState.enemyInBase.iterator().next().getType().isWorker()) {
                            Unit scouter = gameState.enemyInBase.iterator().next();
                            Unit lastTarget = u.getKey().getOrderTarget();
                            if (lastTarget != null && lastTarget.equals(scouter)) continue;
                            u.getKey().attack(scouter);
                        } else {
                            Position closestDefense = null;
                            if (gameState.learningManager.isNaughty()) {
                                if (!gameState.DBs.isEmpty())
                                    closestDefense = gameState.DBs.keySet().iterator().next().getPosition();
                                if (closestDefense == null)
                                    closestDefense = gameState.getNearestCC(u.getKey().getPosition(), false);
                                if (closestDefense != null && u.getKey().getDistance(closestDefense) > UnitType.Terran_Marine.groundWeapon().maxRange() * 0.95) {
                                    u.getKey().move(closestDefense);
                                    continue;
                                }
                            }
                            Unit toAttack = gameState.getUnitToAttack(u.getKey(), gameState.enemyInBase);
                            if (toAttack != null) {
                                Unit lastTarget = u.getKey().getOrderTarget();
                                if (lastTarget != null && lastTarget.equals(toAttack)) continue;
                                u.getKey().attack(toAttack);
                            } else {
                                Position lastTargetPosition = u.getKey().getOrderTargetPosition();
                                if (lastTargetPosition != null && lastTargetPosition.equals(gameState.attackPosition))
                                    continue;
                                u.getKey().attack(gameState.attackPosition);
                            }
                        }
                    }
                }
            } else if (!gameState.getStrat().name.equals("ProxyBBS") && !gameState.getStrat().name.equals("ProxyEightRax")) {
                for (Entry<Integer, Squad> u : gameState.sqManager.squads.entrySet()) {
                    if (gameState.attackPosition != null) {
                        u.getValue().giveAttackOrder(gameState.attackPosition);
                        u.getValue().status = Status.DEFENSE;
                    } else {
                        u.getValue().status = Status.IDLE;
                        u.getValue().attack = null;
                    }
                }
            }
            gameState.attackPosition = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}