package ecgberht.BehaviourTrees.Defense;

import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.Squad;
import ecgberht.Squad.Status;
import ecgberht.Util.MutablePair;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class SendDefenders extends Action {

    public SendDefenders(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            boolean air_only = true;
            boolean cannon_rush = false;
            for (Unit u : ((GameState) this.handler).enemyInBase) {
                if (u.isFlying() || ((PlayerUnit) u).isCloaked()) continue;
                if (!cannon_rush && (u instanceof Pylon || u instanceof PhotonCannon)) cannon_rush = true;
                air_only = false;
            }
            Set<Unit> friends = new TreeSet<>();
            for (Squad s : ((GameState) this.handler).sqManager.squads.values()) friends.addAll(s.members);
            boolean bunker = false;
            if (!((GameState) this.handler).DBs.isEmpty()) {
                friends.addAll(((GameState) this.handler).DBs.keySet());
                bunker = true;
            }
            int defenders = 6;
            if (((GameState) this.handler).enemyInBase.size() == 1 && ((GameState) this.handler).enemyInBase.iterator().next() instanceof Worker) {
                defenders = 1;
            }
            MutablePair<Boolean, Boolean> battleWin = new MutablePair<>(true, false);
            if (defenders != 1 && IntelligenceAgency.enemyIsRushing()) {
                if (((GameState) this.handler).enemyInBase.size() + friends.size() < 40) {
                    battleWin = ((GameState) this.handler).sim.simulateDefenseBattle(friends, ((GameState) this.handler).enemyInBase, 150, bunker);
                }
                if (((GameState) this.handler).enemyInBase.size() >= 3 * friends.size()) battleWin.first = false;
            }
            if (cannon_rush) battleWin.first = false;
            int frame = ((GameState) this.handler).frameCount;
            int notFound = 0;
            if (!air_only && ((!battleWin.first || battleWin.second) || defenders == 1)) {
                while (((GameState) this.handler).workerDefenders.size() + notFound < defenders && !((GameState) this.handler).workerIdle.isEmpty()) {
                    Worker closestWorker = null;
                    Position chosen = ((GameState) this.handler).attackPosition;
                    for (Worker u : ((GameState) this.handler).workerIdle) {
                        if (u.getLastCommandFrame() == frame) continue;
                        if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
                            closestWorker = u;
                        }
                    }
                    if (closestWorker != null) {
                        ((GameState) this.handler).workerDefenders.put(closestWorker, null);
                        ((GameState) this.handler).workerIdle.remove(closestWorker);
                    } else notFound++;
                }
                notFound = 0;
                while (((GameState) this.handler).workerDefenders.size() + notFound < defenders && !((GameState) this.handler).workerMining.isEmpty()) {
                    Worker closestWorker = null;
                    Position chosen = ((GameState) this.handler).attackPosition;
                    for (Entry<Worker, MineralPatch> u : ((GameState) this.handler).workerMining.entrySet()) {
                        if (u.getKey().getLastCommandFrame() == frame) continue;
                        if ((closestWorker == null || u.getKey().getDistance(chosen) < closestWorker.getDistance(chosen))) {
                            closestWorker = u.getKey();
                        }
                    }
                    if (closestWorker != null) {
                        if (((GameState) this.handler).workerMining.containsKey(closestWorker)) {
                            MineralPatch mineral = ((GameState) this.handler).workerMining.get(closestWorker);
                            ((GameState) this.handler).workerDefenders.put(closestWorker, null);
                            if (((GameState) this.handler).mineralsAssigned.containsKey(mineral)) {
                                ((GameState) this.handler).mining--;
                                ((GameState) this.handler).mineralsAssigned.put(mineral, ((GameState) this.handler).mineralsAssigned.get(mineral) - 1);
                            }
                            ((GameState) this.handler).workerMining.remove(closestWorker);
                        }
                    } else notFound++;
                }
                for (Entry<Worker, Position> u : ((GameState) this.handler).workerDefenders.entrySet()) {
                    if (frame == u.getKey().getLastCommandFrame()) continue;
                    if (((GameState) this.handler).attackPosition != null) {
                        ((GameState) this.handler).workerDefenders.put(u.getKey(), ((GameState) this.handler).attackPosition);
                        if (((GameState) this.handler).enemyInBase.size() == 1 && ((GameState) this.handler).enemyInBase.iterator().next() instanceof Worker) {
                            Unit scouter = ((GameState) this.handler).enemyInBase.iterator().next();
                            Unit lastTarget = u.getKey().getOrderTarget();
                            if (lastTarget != null && lastTarget.equals(scouter)) continue;
                            u.getKey().attack(scouter);
                        } else {
                            Position closestDefense = null;
                            if (((GameState) this.handler).EI.naughty) {
                                if (!((GameState) this.handler).DBs.isEmpty())
                                    closestDefense = ((GameState) this.handler).DBs.keySet().iterator().next().getPosition();
                                if (closestDefense == null)
                                    closestDefense = ((GameState) this.handler).getNearestCC(u.getKey().getPosition());
                                if (closestDefense != null && u.getKey().getDistance(closestDefense) > UnitType.Terran_Marine.groundWeapon().maxRange() * 0.95) {
                                    u.getKey().move(closestDefense);
                                    continue;
                                }
                            }
                            Unit toAttack = ((GameState) this.handler).getUnitToAttack(u.getKey(), ((GameState) this.handler).enemyInBase);
                            if (toAttack != null) {
                                Unit lastTarget = u.getKey().getOrderTarget();
                                if (lastTarget != null && lastTarget.equals(toAttack)) continue;
                                u.getKey().attack(toAttack);
                            } else {
                                Position lastTargetPosition = u.getKey().getOrderTargetPosition();
                                if (lastTargetPosition != null && lastTargetPosition.equals(((GameState) this.handler).attackPosition))
                                    continue;
                                u.getKey().attack(((GameState) this.handler).attackPosition);
                            }
                        }
                    }
                }
            } else if (!((GameState) this.handler).strat.name.equals("ProxyBBS") && !((GameState) this.handler).strat.name.equals("EightRax")) {
                for (Entry<Integer, Squad> u : ((GameState) this.handler).sqManager.squads.entrySet()) {
                    if (((GameState) this.handler).attackPosition != null) {
                        u.getValue().giveAttackOrder(((GameState) this.handler).attackPosition);
                        u.getValue().status = Status.DEFENSE;
                    } else {
                        u.getValue().status = Status.IDLE;
                        u.getValue().attack = null;
                    }
                }
            }
            ((GameState) this.handler).attackPosition = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}