package ecgberht;

import bwapi.*;
import ecgberht.Simulation.SimInfo;
import ecgberht.Util.Util;
import ecgberht.Util.UtilMicro;

import java.util.*;
import java.util.stream.Collectors;

import static ecgberht.Ecgberht.getGs;

public class Squad implements Comparable<Squad> {

    private static boolean stimResearched;
    public Position attack;
    public Set<UnitInfo> members = new TreeSet<>();
    public Status status;
    private Position center;
    private Integer id;
    private SimInfo squadSim;
    private boolean medicOnly = false;
    boolean lose;

    Squad(int id, Position center, SimInfo squadSim) {
        this.id = id;
        this.lose = squadSim.lose;
        this.squadSim = squadSim;
        for (UnitInfo m : squadSim.allies) {
            if (isArmyUnit(m) && !getGs().agents.containsKey(m.unit)) this.members.add(m);
        }
        status = getGs().defense ? Status.DEFENSE : Status.IDLE;
        if (getGs().defendPosition != null) attack = getGs().defendPosition;
        else if (!getGs().DBs.isEmpty()) attack = getGs().DBs.keySet().iterator().next().getPosition();
        else {
            Position closestCC = getGs().getNearestCC(center, false);
            if (closestCC != null) attack = closestCC;
            else attack = center;
        }
        this.center = center;
    }

    private boolean isArmyUnit(UnitInfo u) {
        if (!u.unit.exists()) return false;
        if (u.unitType.isBuilding()) return false;
        if (u.unitType == UnitType.Terran_SCV && (getGs().getStrat().name.equals("ProxyBBS") || getGs().getStrat().name.equals("ProxyEightRax")))
            return true;
        if (u.unit.getTransport() != null) return false;
        return u.unitType == UnitType.Terran_Marine || u.unitType == UnitType.Terran_Medic
                || u.isTank() || u.unitType == UnitType.Terran_Firebat || u.unitType == UnitType.Terran_Vulture
                || u.unitType == UnitType.Terran_Wraith || u.unitType == UnitType.Terran_Goliath;
    }

    public Position getSquadCenter() {
        return center;
    }

    public void giveAttackOrder(Position pos) {
        if (!pos.equals(attack)) attack = pos;
    }

    int getSquadMembersCount() {
        int count = 0;
        for (UnitInfo u : members) {
            count++;
            if (u.isTank() || u.unitType == UnitType.Terran_Vulture || u.unitType == UnitType.Terran_Wraith
                    || u.unitType == UnitType.Terran_Goliath)
                count++;
        }
        return count;
    }

    private void setSquadStatus() {
        medicOnly = members.stream().noneMatch(u -> u.unitType != UnitType.Terran_Medic);
        if (status == Status.DEFENSE) return;
        if (status != Status.IDLE && (squadSim.lose || medicOnly)) status = Status.REGROUP;
        else if (status == Status.ATTACK && squadSim.enemies.isEmpty()) status = Status.ADVANCE;
        else if (status == Status.IDLE && !squadSim.enemies.isEmpty() && !IntelligenceAgency.enemyIsRushing() && (getGs().defendPosition == null || getGs().defendPosition.getDistance(center) <= 350))
            if (!squadSim.lose) status = Status.ATTACK;
            else status = Status.REGROUP;
    }

    void updateSquad() {
        if (members.isEmpty()) return;
        if (!stimResearched && getGs().getPlayer().hasResearched(TechType.Stim_Packs)) stimResearched = true;
        setSquadStatus();
    }

    void runSquad() {
        try {
            Set<Unit> marinesToHeal = new TreeSet<>();
            for (UnitInfo u : members) {
                if (u.unit.isLockedDown() || u.unit.isMaelstrommed() || (u.unit).isStasised() || (u.unit).getTransport() != null)
                    continue;
                if((u.unitType == UnitType.Terran_Marine || u.unitType == UnitType.Terran_Firebat) && shouldStim(u))
                    u.unit.useTech(TechType.Stim_Packs);
                if (u.unitType == UnitType.Terran_Medic) microMedic(u.unit, marinesToHeal);
                else if (u.isTank()) microTank(u);
                else if (u.unitType.groundWeapon().maxRange() > 32) microRanged(u);
                else microMelee(u);
            }
        } catch (Exception e) {
            System.err.println("runSquad Error");
            e.printStackTrace();
        }
    }

    // Based on @Locutus micro logic
    private void microRanged(UnitInfo u) {
        switch (status) {
            case ATTACK:
            case DEFENSE:
                executeRangedAttackLogic(u);
                break;
            case IDLE:
                if (getGs().getStrat().proxy) return;
                Position move = null;
                if (getGs().defendPosition != null) {
                    if (u.currentOrder != Order.Stop) move = getGs().defendPosition;
                } else if (!getGs().DBs.isEmpty()) {
                    Unit bunker = getGs().DBs.keySet().iterator().next();
                    if (u.currentOrder != Order.Stop) move = bunker.getPosition();
                } else if (getGs().mainChoke != null && !getGs().learningManager.isNaughty() && !getGs().getStrat().name.equals("ProxyBBS") && !getGs().getStrat().name.equals("ProxyEightRax")) {
                    if (u.currentOrder != Order.Stop) move = getGs().mainChoke.getCenter().toPosition();

                } else if (Util.broodWarDistance(u.position, center) >= 190 && u.currentOrder != Order.Move) {
                    if (getGs().bw.isWalkable(center.toWalkPosition())) move = center;
                }
                if (move != null) {
                    WeaponType weapon = Util.getWeapon(u.unitType);
                    int range2 = weapon == WeaponType.None ? UnitType.Terran_Marine.groundWeapon().maxRange() : weapon.maxRange();
                    if (u.currentOrder == Order.AttackMove) {
                        if (u.unit.getDistance(move) <= range2 * ((double) (new Random().nextInt((10 + 1) - 4) + 4)) / 10.0 && Util.shouldIStop(u.position)) {
                            UtilMicro.stop(u.unit);
                            return;
                        }
                    } else if (u.unit.getDistance(move) > range2) {
                        UtilMicro.attack(u.unit, move);
                        return;
                    }
                }
                break;
            case REGROUP:
                if ((u.unit).isDefenseMatrixed() || getGs().sim.farFromFight(u, squadSim, false) || squadSim.enemies.stream().noneMatch(e -> Util.getWeapon(e, u).maxRange() > 32)) {
                    executeRangedAttackLogic(u);
                    return;
                }
                Position pos = getGs().getNearestCC(u.position, true);
                if (pos != null && Util.broodWarDistance(pos, u.position) >= 400) {
                    UtilMicro.move(u.unit, pos);
                }
                break;
            case ADVANCE:
                double dist = Util.broodWarDistance(u.position, center);
                if (dist >= 240) {
                    UtilMicro.move(u.unit, center);
                    return;
                } else if (attack != null) UtilMicro.move(u.unit, attack);
                break;
        }
    }

    private void microMelee(UnitInfo u) {
        switch (status) {
            case ATTACK:
            case DEFENSE:
                if (u.unit.isAttackFrame() || u.unit.isStartingAttack()) return;
                if (squadSim.enemies.isEmpty() && attack != null) {
                    UtilMicro.attack(u.unit, attack);
                    return;
                }
                //Experimental storm dodging?
                if ((u.unit).isUnderStorm()) {
                    Position closestCC = getGs().getNearestCC(u.position, true);
                    if (closestCC != null) {
                        UtilMicro.move(u.unit, closestCC);
                        return;
                    }
                }
                if (attack != null && !u.unit.isStartingAttack() && !u.unit.isAttacking()) {
                    UnitInfo target = Util.getRangedTarget(u, squadSim.enemies, attack);
                    if (target != null) UtilMicro.attack(u.unit, target);
                    else if (attack != null) UtilMicro.attack(u.unit, attack);
                }
                break;
            case IDLE:
                if (getGs().getStrat().proxy) return;
                Position move = null;
                if (getGs().defendPosition != null) {
                    if (u.currentOrder != Order.Stop) move = getGs().defendPosition;
                } else if (!getGs().DBs.isEmpty()) {
                    Unit bunker = getGs().DBs.keySet().iterator().next();
                    if (u.currentOrder != Order.Stop) move = bunker.getPosition();
                } else if (getGs().mainChoke != null && !getGs().learningManager.isNaughty() && !getGs().getStrat().name.equals("ProxyBBS") && !getGs().getStrat().name.equals("ProxyEightRax")) {
                    if (u.currentOrder != Order.Stop) move = getGs().mainChoke.getCenter().toPosition();

                } else if (Util.broodWarDistance(u.position, center) >= 190 && u.currentOrder != Order.Move) {
                    if (getGs().getGame().isWalkable(center.toWalkPosition())) move = center;
                }
                if (move != null) {
                    int range2 = UnitType.Terran_Marine.groundWeapon().maxRange();
                    if (u.currentOrder == Order.AttackMove || u.currentOrder == Order.Move || u.currentOrder == Order.PlayerGuard) {
                        if (u.getDistance(move) <= range2 * ((double) (new Random().nextInt((10 + 1) - 4) + 4)) / 10.0 && Util.shouldIStop(u.position)) {
                            UtilMicro.stop(u.unit);
                            return;
                        }
                    } else if (u.getDistance(move) > range2) {
                        UtilMicro.attack(u.unit, move);
                        return;
                    }
                }
                break;
            case REGROUP:
                if ((u.unit).isDefenseMatrixed() || getGs().sim.farFromFight(u, squadSim, true)) return;
                Position pos = getGs().getNearestCC(u.position, true);
                if (Util.broodWarDistance(pos, u.position) >= 400) {
                    UtilMicro.move(u.unit, pos);
                }
                break;
            case ADVANCE:
                double dist = Util.broodWarDistance(u.position, center);
                if (dist >= 260) {
                    UtilMicro.move(u.unit, center);
                    return;
                } else if (attack != null) UtilMicro.move(u.unit, attack);
                break;
        }
    }

    private void microTank(UnitInfo u) {
        Unit st = u.unit;
        if (st.isAttackFrame() || st.isStartingAttack() || st.isSieged() && u.currentOrder == Order.Unsieging) return;
        if (!st.isSieged() && u.currentOrder == Order.Sieging) return;
        if (u.currentOrder == Order.Unsieging || u.currentOrder == Order.Sieging) return;
        switch (status) {
            case ATTACK:
            case DEFENSE:
                boolean found = false;
                boolean close = false;
                int threats = (int) squadSim.enemies.stream().filter(e -> e.unitType.canAttack() || e.unitType.isSpellcaster() || Util.isStaticDefense(e.unitType)).count();
                for (UnitInfo e : squadSim.enemies) {
                    if (e.flying || e.unitType.isWorker() || e.unitType == UnitType.Terran_Medic || (e.unitType.isBuilding() && !Util.isStaticDefense(e)))
                        continue;
                    int distance = u.getDistance(e);
                    if (!found && distance <= UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange() - 8 && (e.health + e.shields >= 60 || threats > 2)) {
                        found = true;
                    }
                    if (!close && distance < UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().minRange())
                        close = true;
                    if (found && close) break;
                }
                if (found && !close) {
                    if (!st.isSieged()) {
                        st.siege();
                        return;
                    }
                }
                if (status == Status.DEFENSE && st.isSieged() && getGs().defendPosition != null) {
                    double range = u.groundRange - 8;
                    if (u.getDistance(getGs().defendPosition) > range) st.unsiege();
                    return;
                }
                Set<UnitInfo> tankTargets = squadSim.enemies.stream().filter(e -> !e.flying).collect(Collectors.toSet());
                if (st.isSieged()) {
                    tankTargets.removeIf(e -> u.getDistance(e) > UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange() - 8 || (e.unitType.isBuilding() && !Util.isStaticDefense(e)) || (!e.unitType.isBuilding() && !e.unitType.canAttack() && !e.unitType.isSpellcaster()));
                    if (tankTargets.isEmpty() && Math.random() * 10 <= 3) {
                        st.unsiege();
                        return;
                    }
                }
                UnitInfo target = Util.getTankTarget(u, tankTargets);
                if (target != null) UtilMicro.attack(u, target);
                else if (attack != null && Math.random() * 10 <= 2.5) {
                    if (st.isSieged()) st.unsiege();
                    else UtilMicro.move(st, attack);
                }
                break;
            case IDLE:
                Position move = null;
                if (getGs().defendPosition != null) {
                    move = getGs().defendPosition;
                } else if (!getGs().DBs.isEmpty()) {
                    Unit bunker = getGs().DBs.keySet().iterator().next();
                    if (Util.broodWarDistance(bunker.getPosition(), center) >= 180 &&
                            getGs().getArmySize() < getGs().getStrat().armyForAttack && !getGs().getStrat().name.equals("ProxyBBS") && !getGs().getStrat().name.equals("ProxyEightRax")) {
                        if (u.currentOrder != Order.Move) move = bunker.getPosition();
                    }
                } else if (getGs().mainChoke != null && !getGs().learningManager.isNaughty() && !getGs().getStrat().name.equals("ProxyBBS") && !getGs().getStrat().name.equals("ProxyEightRax")) {
                    if (Util.broodWarDistance(getGs().mainChoke.getCenter().toPosition(), center) >= 200 &&
                            getGs().getArmySize() < getGs().getStrat().armyForAttack) {
                        if (u.currentOrder != Order.Move) move = getGs().mainChoke.getCenter().toPosition();
                    }
                } else if (Util.broodWarDistance(u.position, center) >= 180 && u.currentOrder != Order.Move) {
                    if (getGs().bw.isWalkable(center.toWalkPosition())) move = center;
                }
                if (move != null) {
                    WeaponType weapon = Util.getWeapon(u.unitType);
                    int range = weapon.maxRange();
                    if (u.currentOrder == Order.AttackMove || u.currentOrder == Order.PlayerGuard || u.currentOrder == Order.Move) {
                        if (u.getDistance(move) <= range * ((double) (new Random().nextInt((10 + 1) - 4) + 4)) / 10.0 && Util.shouldIStop(u.position)) {
                            if (!st.isSieged() && getGs().getPlayer().hasResearched(TechType.Tank_Siege_Mode)) {
                                st.siege();
                            } else UtilMicro.stop(st);
                            return;
                        }
                    } else if (u.getDistance(move) > range) {
                        if (st.isSieged() && !getGs().defense) {
                            st.unsiege();
                        } else UtilMicro.attack(st, move);
                        return;
                    }
                }
                break;
            case REGROUP:
                if (st.isDefenseMatrixed() || getGs().sim.farFromFight(u, squadSim, false)) return;
                Position pos = getGs().getNearestCC(u.position, true);
                if (Util.broodWarDistance(pos, u.position) >= 400) UtilMicro.move(st, pos);
                break;
            case ADVANCE:
                if (st.isSieged() && Math.random() * 10 <= 6) st.unsiege();
                else {
                    double dist = Util.broodWarDistance(u.position, center);
                    if (dist >= 300) {
                        UtilMicro.attack(st, center);
                        return;
                    } else if (attack != null) UtilMicro.move(st, attack);
                }
                break;
        }
    }

    private void microMedic(Unit u, Set<Unit> marinesToHeal) {
        Unit healTarget = getHealTarget(u, marinesToHeal);
        if (healTarget != null) {
            UtilMicro.heal(u, healTarget);
            marinesToHeal.add(healTarget);
        } else if (status == Status.IDLE) {
            if (getGs().defendPosition != null) {
                int range = UnitType.Terran_Marine.groundWeapon().maxRange();
                if (getGs().defendPosition.getDistance(u.getPosition()) <= range * ((double) (new Random().nextInt((10 + 1) - 4) + 4)) / 10.0 && Util.shouldIStop(u.getPosition())) {
                    UtilMicro.stop(u);
                } else if (u.getDistance(getGs().defendPosition) > range) UtilMicro.move(u, getGs().defendPosition);
            }
        } else if (status == Status.REGROUP) {
            if (medicOnly) {
                Optional<Squad> closest = getGs().sqManager.squads.values().stream().
                        filter(s -> !s.medicOnly).
                        min(Comparator.comparingDouble(s -> u.getDistance(s.getSquadCenter())));
                closest.ifPresent(squad -> UtilMicro.move(u, squad.getSquadCenter()));
                return;
            }
            Position pos = getGs().getNearestCC(u.getPosition(), true);
            if (Util.broodWarDistance(pos, u.getPosition()) >= 400) UtilMicro.heal(u, pos);
        } else if (status == Status.ADVANCE && attack != null) UtilMicro.heal(u, attack);
        else if (center.getDistance(u.getPosition()) > 32 * 6) UtilMicro.heal(u, center);
    }

    private boolean shouldStim(UnitInfo u) {
        if(u.unitType != UnitType.Terran_Marine && u.unitType != UnitType.Terran_Firebat) return false;
        if (u.unit.isStimmed() || u.health <= 25) return false;
        Unit target = u.target;
        if (target != null) {
            UnitInfo targetUI = getGs().unitStorage.getEnemyUnits().get(target);
            double range = u.groundRange;
            double distToTarget;
            if (targetUI != null) distToTarget = u.getDistance(targetUI);
            else distToTarget = u.unit.getDistance(target);
            return distToTarget > (range - 32) && (target.canAttack() || target.getType().isSpellcaster());
        }
        return false;
    }

    private Unit getHealTarget(final Unit u, final Set<Unit> marinesToHeal) {
        Set<Unit> healables = getHealable();
        Unit chosen = null;
        double dist = Double.MAX_VALUE;
        for (Unit m : healables) {
            if (m.getHitPoints() == m.getType().maxHitPoints() || marinesToHeal.contains(m)) continue;
            double distA = m.getDistance(u);
            if (chosen == null || distA < dist) {
                chosen = m;
                dist = distA;
            }
        }
        return chosen;
    }

    private Set<Unit> getHealable() {
        Set<Unit> aux = new TreeSet<>();
        for (UnitInfo u : this.members) {
            if (u.unitType.isOrganic()) aux.add(u.unit);
        }
        return aux;
    }

    private void executeRangedAttackLogic(UnitInfo u) {
        if (u.unit.isAttackFrame() || u.unit.isStartingAttack()) return;
        UnitInfo target = Util.getRangedTarget(u, squadSim.enemies, attack);
        if (target == null) {
            if (attack != null) UtilMicro.attack(u.unit, attack);
            return;
        }
        double distToTarget = u.getDistance(target);
        Optional<UnitInfo> bunker = squadSim.allies.stream().filter(ally -> ally.unitType == UnitType.Terran_Bunker).findFirst();
        if (status == Status.DEFENSE && IntelligenceAgency.enemyIsRushing() && bunker.isPresent() && u.getDistance(bunker.get()) > distToTarget) {
            UtilMicro.move(u.unit, bunker.get().lastPosition);
        }
        double speed = u.speed;
        if (getGs().frameCount - u.unit.getLastCommandFrame() <= 10) return;
        WeaponType w = Util.getWeapon(u, target);
        double range = u.player.weaponMaxRange(w);

        int cooldown = (target.flying ? u.unit.getAirWeaponCooldown() :  u.unit.getGroundWeaponCooldown()) - getGs().getGame().getRemainingLatencyFrames() - 2;
        int framesToFiringRange = (int) Math.ceil(Math.max(0, distToTarget - range) / speed);
        if (cooldown <= framesToFiringRange) {
            UtilMicro.attack(u, target);
            return;
        }
        Position predictedPosition = null;
        int targetRange = target.player.weaponMaxRange(Util.getWeapon(target, u));
        boolean kite = true;
        boolean enemyTooClose = distToTarget <= 3 * 32 && targetRange <= 32;
        boolean moveCloser = target.unitType == UnitType.Terran_Siege_Tank_Siege_Mode ||target.currentOrder == Order.Sieging ||
                (target.unitType == UnitType.Terran_SCV && (target.unit.isRepairing() && target.unit.getOrderTarget() != null && target.unit.getOrderTarget().getType() == UnitType.Terran_Bunker)) ||
                (target.unitType.isBuilding() && !Util.canAttack(target, u));
        if (!moveCloser) {
            predictedPosition = UtilMicro.predictUnitPosition(target, 2);
            if (predictedPosition != null && predictedPosition.isValid(getGs().getGame())) {
                double distPredicted = u.getDistance(predictedPosition);
                double distCurrent = u.getDistance(target);
                if (distPredicted > distCurrent) {
                    kite = false;
                    if (distToTarget > (range - 24)) moveCloser = true;
                } else if (distCurrent == distPredicted && range >= (targetRange + 64) && distToTarget > (range - 48)) {
                    moveCloser = true;
                }
            }
        }
        if (moveCloser && !enemyTooClose) {
            if (distToTarget > 32 && !u.unit.isStartingAttack() && !u.unit.isAttackFrame()) {
                if (predictedPosition != null) UtilMicro.move(u.unit, predictedPosition);
                else UtilMicro.move(u.unit, target.lastPosition);
            } else UtilMicro.attack(u.unit, target);
            return;
        }
        if ((kite || enemyTooClose) && target.unitType.topSpeed() > 0) {
            Position kitePos = UtilMicro.kiteAwayAlt(u.position, target.lastPosition);
            if (kitePos != null) UtilMicro.move(u.unit, kitePos);
            else {
                kitePos = UtilMicro.kiteAway(u.unit, squadSim.enemies);
                if (kitePos != null) UtilMicro.move(u.unit, kitePos);
            }
        } else UtilMicro.attack(u.unit, target);
    }

    public void giveMoveOrder(Position retreat) {
        int frameCount = getGs().frameCount;
        for (UnitInfo u : members) {
            Unit pU = u.unit;
            if (u.unitType == UnitType.Terran_Siege_Tank_Siege_Mode && pU.getOrder() == Order.Unsieging) continue;
            if (u.unitType == UnitType.Terran_Siege_Tank_Tank_Mode && pU.getOrder() == Order.Sieging) continue;
            Position lastTarget = u.unit.getTargetPosition() == null ? pU.getOrderTargetPosition() : u.unit.getTargetPosition();
            if (lastTarget != null && lastTarget.equals(retreat)) continue;
            if (attack != null && frameCount != pU.getLastCommandFrame()) u.unit.move(retreat);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Squad)) return false;
        Squad s = (Squad) o;
        return id.equals(s.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Squad o) {
        return this.id.compareTo(o.id);
    }

    public enum Status {
        ATTACK, IDLE, REGROUP, ADVANCE, DEFENSE
    }
}
