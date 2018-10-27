package ecgberht.BehaviourTrees.Defense;

import ecgberht.GameState;
import ecgberht.IntelligenceAgency;
import ecgberht.Squad;
import ecgberht.Squad.Status;
import ecgberht.Util.MutablePair;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class SendDefenders extends Action {

    public SendDefenders(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            boolean air_only = true;
            boolean cannon_rush = false;
            for (Unit u : this.handler.enemyInBase) {
                if (u.isFlying() || ((PlayerUnit) u).isCloaked()) continue;
                if (!cannon_rush && (u instanceof Pylon || u instanceof PhotonCannon)) cannon_rush = true;
                air_only = false;
            }
            Set<Unit> friends = new TreeSet<>();
            for (Squad s : this.handler.sqManager.squads.values()) friends.addAll(s.members);
            boolean bunker = false;
            if (!this.handler.DBs.isEmpty()) {
                friends.addAll(this.handler.DBs.keySet());
                bunker = true;
            }
            int defenders = 6;
            if (this.handler.enemyInBase.size() == 1 && this.handler.enemyInBase.iterator().next() instanceof Worker) {
                defenders = 1;
            }
            MutablePair<Boolean, Boolean> battleWin = new MutablePair<>(true, false);
            if (defenders != 1 && IntelligenceAgency.enemyIsRushing()) {
                if (this.handler.enemyInBase.size() + friends.size() < 40) {
                    battleWin = this.handler.sim.simulateDefenseBattle(friends, this.handler.enemyInBase, 150, bunker);
                }
                if (this.handler.enemyInBase.size() >= 3 * friends.size()) battleWin.first = false;
            }
            if (cannon_rush) battleWin.first = false;
            int frame = this.handler.frameCount;
            int notFound = 0;
            if (!air_only && ((!battleWin.first || battleWin.second) || defenders == 1)) {
                while (this.handler.workerDefenders.size() + notFound < defenders && !this.handler.workerIdle.isEmpty()) {
                    Worker closestWorker = null;
                    Position chosen = this.handler.attackPosition;
                    for (Worker u : this.handler.workerIdle) {
                        if (u.getLastCommandFrame() == frame) continue;
                        if ((closestWorker == null || u.getDistance(chosen) < closestWorker.getDistance(chosen))) {
                            closestWorker = u;
                        }
                    }
                    if (closestWorker != null) {
                        this.handler.workerDefenders.put(closestWorker, null);
                        this.handler.workerIdle.remove(closestWorker);
                    } else notFound++;
                }
                notFound = 0;
                while (this.handler.workerDefenders.size() + notFound < defenders && !this.handler.workerMining.isEmpty()) {
                    Worker closestWorker = null;
                    Position chosen = this.handler.attackPosition;
                    for (Entry<Worker, MineralPatch> u : this.handler.workerMining.entrySet()) {
                        if (u.getKey().getLastCommandFrame() == frame) continue;
                        if ((closestWorker == null || u.getKey().getDistance(chosen) < closestWorker.getDistance(chosen))) {
                            closestWorker = u.getKey();
                        }
                    }
                    if (closestWorker != null) {
                        if (this.handler.workerMining.containsKey(closestWorker)) {
                            MineralPatch mineral = this.handler.workerMining.get(closestWorker);
                            this.handler.workerDefenders.put(closestWorker, null);
                            if (this.handler.mineralsAssigned.containsKey(mineral)) {
                                this.handler.mining--;
                                this.handler.mineralsAssigned.put(mineral, this.handler.mineralsAssigned.get(mineral) - 1);
                            }
                            this.handler.workerMining.remove(closestWorker);
                        }
                    } else notFound++;
                }
                for (Entry<Worker, Position> u : this.handler.workerDefenders.entrySet()) {
                    if (frame == u.getKey().getLastCommandFrame()) continue;
                    if (this.handler.attackPosition != null) {
                        this.handler.workerDefenders.put(u.getKey(), this.handler.attackPosition);
                        if (this.handler.enemyInBase.size() == 1 && this.handler.enemyInBase.iterator().next() instanceof Worker) {
                            Unit scouter = this.handler.enemyInBase.iterator().next();
                            Unit lastTarget = u.getKey().getOrderTarget();
                            if (lastTarget != null && lastTarget.equals(scouter)) continue;
                            u.getKey().attack(scouter);
                        } else {
                            Position closestDefense = null;
                            if (this.handler.EI.naughty) {
                                if (!this.handler.DBs.isEmpty())
                                    closestDefense = this.handler.DBs.keySet().iterator().next().getPosition();
                                if (closestDefense == null)
                                    closestDefense = this.handler.getNearestCC(u.getKey().getPosition());
                                if (closestDefense != null && u.getKey().getDistance(closestDefense) > UnitType.Terran_Marine.groundWeapon().maxRange() * 0.95) {
                                    u.getKey().move(closestDefense);
                                    continue;
                                }
                            }
                            Unit toAttack = this.handler.getUnitToAttack(u.getKey(), this.handler.enemyInBase);
                            if (toAttack != null) {
                                Unit lastTarget = u.getKey().getOrderTarget();
                                if (lastTarget != null && lastTarget.equals(toAttack)) continue;
                                u.getKey().attack(toAttack);
                            } else {
                                Position lastTargetPosition = u.getKey().getOrderTargetPosition();
                                if (lastTargetPosition != null && lastTargetPosition.equals(this.handler.attackPosition))
                                    continue;
                                u.getKey().attack(this.handler.attackPosition);
                            }
                        }
                    }
                }
            } else if (!this.handler.strat.name.equals("ProxyBBS") && !this.handler.strat.name.equals("EightRax")) {
                for (Entry<Integer, Squad> u : this.handler.sqManager.squads.entrySet()) {
                    if (this.handler.attackPosition != null) {
                        u.getValue().giveAttackOrder(this.handler.attackPosition);
                        u.getValue().status = Status.DEFENSE;
                    } else {
                        u.getValue().status = Status.IDLE;
                        u.getValue().attack = null;
                    }
                }
            }
            this.handler.attackPosition = null;
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}