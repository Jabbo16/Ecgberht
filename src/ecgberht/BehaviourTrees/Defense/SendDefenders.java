package ecgberht.BehaviourTrees.mil.defense;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.Squad;
import ecgberht.Squad.Status;
import ecgberht.UnitInfo;
import ecgberht.Util.MutablePair;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class SendDefenders extends BrainAction {

    public SendDefenders(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            boolean air_only = true;
            boolean cannon_rush = false;
            for (Unit u : gameState.enemyInBase) {
                if (u.isFlying() || ((PlayerUnit) u).isCloaked()) continue;
                if (!cannon_rush && (u instanceof Pylon || u instanceof PhotonCannon)) cannon_rush = true;
                air_only = false;
            }
            Set<UnitInfo> friends = new TreeSet<>();
            for (Squad s : gameState.sqManager.squads.values()) friends.addAll(s.members);
            boolean bunker = false;
            if (!gameState.tech.DBs.isEmpty()) {
                for (Bunker b : gameState.tech.DBs.keySet()) {
                    friends.add(gameState.unitStorage.getAllyUnits().get(b));
                }
                bunker = true;
            }
            int defenders = 6;
            if (gameState.enemyInBase.size() == 1 && gameState.enemyInBase.iterator().next() instanceof Worker) {
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
                while (gameState.eco.workerDefenders.size() + notFound < defenders && !gameState.workerIdle.isEmpty()) {
                    Worker closestWorker = null;
                    Position chosen = gameState.mil.attackPosition;
                    for (Worker u : gameState.workerIdle) {
                        if (u.getLastCommandFrame() == frame) continue;
                        if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
                            closestWorker = u;
                        }
                    }
                    if (closestWorker != null) {
                        gameState.eco.workerDefenders.put(closestWorker, null);
                        gameState.workerIdle.remove(closestWorker);
                    } else notFound++;
                }
                notFound = 0;
                while (gameState.eco.workerDefenders.size() + notFound < defenders && !gameState.eco.workerMining.isEmpty()) {
                    Worker closestWorker = null;
                    Position chosen = gameState.mil.attackPosition;
                    for (Entry<Worker, MineralPatch> u : gameState.eco.workerMining.entrySet()) {
                        if (u.getKey().getLastCommandFrame() == frame) continue;
                        if ((closestWorker == null || u.getKey().getDistance(chosen) < closestWorker.getDistance(chosen))) {
                            closestWorker = u.getKey();
                        }
                    }
                    if (closestWorker != null) {
                        if (gameState.eco.workerMining.containsKey(closestWorker)) {
                            MineralPatch mineral = gameState.eco.workerMining.get(closestWorker);
                            gameState.eco.workerDefenders.put(closestWorker, null);
                            if (gameState.eco.mineralsAssigned.containsKey(mineral)) {
                                gameState.eco.mining--;
                                gameState.eco.mineralsAssigned.put(mineral, gameState.eco.mineralsAssigned.get(mineral) - 1);
                            }
                            gameState.eco.workerMining.remove(closestWorker);
                        }
                    } else notFound++;
                }
                for (Entry<Worker, Position> u : gameState.eco.workerDefenders.entrySet()) {
                    if (frame == u.getKey().getLastCommandFrame()) continue;
                    if (gameState.mil.attackPosition != null) {
                        gameState.eco.workerDefenders.put(u.getKey(), gameState.mil.attackPosition);
                        if (gameState.enemyInBase.size() == 1 && gameState.enemyInBase.iterator().next() instanceof Worker) {
                            Unit scouter = gameState.enemyInBase.iterator().next();
                            Unit lastTarget = u.getKey().getOrderTarget();
                            if (lastTarget != null && lastTarget.equals(scouter)) continue;
                            u.getKey().attack(scouter);
                        } else {
                            Position closestDefense = null;
                            if (gameState.learningManager.isNaughty()) {
                                if (!gameState.tech.DBs.isEmpty())
                                    closestDefense = gameState.tech.DBs.keySet().iterator().next().getPosition();
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
                                if (lastTargetPosition != null && lastTargetPosition.equals(gameState.mil.attackPosition))
                                    continue;
                                u.getKey().attack(gameState.mil.attackPosition);
                            }
                        }
                    }
                }
            } else if (!gameState.getStrat().name.equals("ProxyBBS") && !gameState.getStrat().name.equals("ProxyEightRax")) {
                for (Entry<Integer, Squad> u : gameState.sqManager.squads.entrySet()) {
                    if (gameState.mil.attackPosition != null) {
                        u.getValue().giveAttackOrder(gameState.mil.attackPosition);
                        u.getValue().status = Status.defense;
                    } else {
                        u.getValue().status = Status.IDLE;
                        u.getValue().attack = null;
                    }
                }
            }
            gameState.mil.attackPosition = null;
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}





