package ecgberht;

import ecgberht.Simulation.SimInfo;
import ecgberht.Util.Util;
import ecgberht.Util.UtilMicro;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.type.WeaponType;
import org.openbw.bwapi4j.unit.*;

import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static ecgberht.Ecgberht.getGs;

public class Squad implements Comparable<Squad> {

    private static boolean stimResearched;
    public Position attack;
    public Set<PlayerUnit> members = new TreeSet<>();
    public Status status;
    private Position center;
    private Integer id;
    private SimInfo squadSim;

    Squad(int id, Position center, SimInfo squadSim) {
        this.id = id;
        this.squadSim = squadSim;
        for (Unit m : squadSim.allies) {
            if (isArmyUnit(m) && !getGs().agents.containsKey(m)) this.members.add((PlayerUnit) m);
        }
        status = getGs().defense ? Status.DEFENSE : Status.IDLE;
        if (getGs().defendPosition != null) attack = getGs().defendPosition;
        else if (!getGs().DBs.isEmpty()) attack = getGs().DBs.keySet().iterator().next().getPosition();
        else {
            Position closestCC = getGs().getNearestCC(center);
            if (closestCC != null) attack = closestCC;
            else attack = center;
        }
        this.center = center;
    }

    private boolean isArmyUnit(Unit u) {
        if (!u.exists()) return false;
        if (u instanceof Building) return false;
        if (u instanceof SCV && (getGs().strat.name.equals("ProxyBBS") || getGs().strat.name.equals("ProxyEightRax")))
            return true;
        if (u instanceof MobileUnit && ((MobileUnit) u).getTransport() != null) return false;
        return u instanceof Marine || u instanceof Medic || u instanceof SiegeTank || u instanceof Firebat
                || u instanceof Vulture || u instanceof Wraith || u instanceof Goliath;
    }

    public Position getSquadCenter() {
        return center;
    }

    public void giveAttackOrder(Position pos) {
        if (!pos.equals(attack)) attack = pos;
    }

    int getSquadMembersCount() {
        int count = 0;
        for (Unit u : members) {
            count++;
            if (u instanceof Goliath || u instanceof SiegeTank || u instanceof Vulture || u instanceof Wraith) count++;
        }
        return count;
    }

    private void setSquadStatus() {
        if (status == Status.DEFENSE) return;
        if (squadSim.lose) status = Status.REGROUP;
        else if (status == Status.ATTACK && squadSim.enemies.isEmpty()) status = Status.ADVANCE;
    }

    void updateSquad() {
        if (members.isEmpty()) return;
        if (!stimResearched && getGs().getPlayer().hasResearched(TechType.Stim_Packs)) stimResearched = true;
        setSquadStatus();
        microUpdateOrder();
    }

    private void microUpdateOrder() {
        try {
            Set<Unit> marinesToHeal = new TreeSet<>();
            for (PlayerUnit u : members) {
                if (u.isLockedDown() || u.isMaelstrommed() || ((MobileUnit) u).isStasised() || ((MobileUnit) u).getTransport() != null)
                    continue;
                if (u instanceof Marine && shouldStim(((Marine) u))) ((Marine) u).stimPack();
                if (u instanceof Firebat && shouldStim(((Firebat) u))) ((Firebat) u).stimPack();
                if (u instanceof Medic) microMedic((Medic) u, marinesToHeal);
                else if (u instanceof SiegeTank) microTank((SiegeTank) u);
                else if (u.getType().groundWeapon().maxRange() > 32) microRanged((MobileUnit) u);
                else microMelee((MobileUnit) u);
            }
        } catch (Exception e) {
            System.err.println("microUpdateOrder Error");
            e.printStackTrace();
        }
    }

    // Based on @Locutus micro logic
    private void microRanged(MobileUnit u) {
        switch (status) {
            case ATTACK:
            case DEFENSE:
                executeRangedAttackLogic(u);
                break;
            case IDLE:
                Position move = null;
                if (getGs().defendPosition != null) {
                    if (u.getOrder() != Order.Move) move = getGs().defendPosition;
                } else if (!getGs().DBs.isEmpty()) {
                    Unit bunker = getGs().DBs.keySet().iterator().next();
                    if (Util.broodWarDistance(bunker.getPosition(), center) >= 180 &&
                            getGs().getArmySize() < getGs().strat.armyForAttack && !getGs().strat.name.equals("ProxyBBS") && !getGs().strat.name.equals("ProxyEightRax")) {
                        if (u.getOrder() != Order.Move) move = bunker.getPosition();
                    }
                } else if (getGs().mainChoke != null && !getGs().learningManager.isNaughty() && !getGs().strat.name.equals("ProxyBBS") && !getGs().strat.name.equals("ProxyEightRax")) {
                    if (Util.broodWarDistance(getGs().mainChoke.getCenter().toPosition(), center) >= 200 &&
                            getGs().getArmySize() < getGs().strat.armyForAttack) {
                        if (u.getOrder() != Order.Move) move = getGs().mainChoke.getCenter().toPosition();
                    }
                } else if (Util.broodWarDistance(u.getPosition(), center) >= 180 && u.getOrder() != Order.Move) {
                    if (getGs().getGame().getBWMap().isWalkable(center.toWalkPosition())) move = center;
                }
                if (move != null) {
                    WeaponType weapon = Util.getWeapon(u.getType());
                    int range2 = weapon == WeaponType.None ? UnitType.Terran_Marine.groundWeapon().maxRange() : weapon.maxRange();
                    if (u.getOrder() == Order.AttackMove) {
                        if (u.getDistance(move) <= range2 * ((double) (new Random().nextInt((10 + 1) - 4) + 4)) / 10.0 && Util.shouldIStop(u.getPosition())) {
                            UtilMicro.stop(u);
                            return;
                        }
                    } else if (u.getDistance(move) > range2) {
                        UtilMicro.attack(u, move);
                        return;
                    }
                }
                break;
            case REGROUP:
                if (u.isDefenseMatrixed() || getGs().sim.farFromFight(u, squadSim) || squadSim.enemies.stream().noneMatch(e -> Util.getWeapon(e, u).maxRange() > 32)) {
                    executeRangedAttackLogic(u);
                    return;
                }
                Position pos = getGs().getNearestCC(u.getPosition());
                if (Util.broodWarDistance(pos, u.getPosition()) >= 400) {
                    UtilMicro.move(u, pos);
                }
                break;
            case ADVANCE:
                double dist = Util.broodWarDistance(u.getPosition(), center);
                if (dist >= 240) {
                    UtilMicro.move(u, center);
                    return;
                } else if (attack != null) UtilMicro.move(u, attack);
                break;
        }
    }

    private void microMelee(MobileUnit u) {
        switch (status) {
            case ATTACK:
            case DEFENSE:
                if (squadSim.enemies.isEmpty() && attack != null) {
                    UtilMicro.attack(u, attack);
                    return;
                }
                //Experimental storm dodging?
                if (u.isUnderStorm()) {
                    Position closestCC = getGs().getNearestCC(u.getPosition());
                    if (closestCC != null) {
                        UtilMicro.move(u, closestCC);
                        return;
                    }
                }
                if (attack != null && !u.isStartingAttack() && !u.isAttacking()) {
                    Unit target = Util.getRangedTarget(u, squadSim.enemies, attack);
                    UtilMicro.attack((Attacker) u, target);
                }
                break;
            case IDLE:
                Position move = null;
                if (getGs().strat.proxy && !getGs().MBs.isEmpty()) {
                    Position firstRax = getGs().MBs.iterator().next().getPosition();
                    if (firstRax.getDistance(u.getPosition()) > 200) {
                        UtilMicro.move(u, firstRax);
                        return;
                    }
                } else if (getGs().defendPosition != null) {
                    if (u.getOrder() != Order.Move) move = getGs().defendPosition;
                } else if (!getGs().DBs.isEmpty()) {
                    Unit bunker = getGs().DBs.keySet().iterator().next();
                    if (Util.broodWarDistance(bunker.getPosition(), center) >= 180 &&
                            getGs().getArmySize() < getGs().strat.armyForAttack) {
                        if (u.getOrder() != Order.Move) move = bunker.getPosition();
                    }
                } else if (getGs().mainChoke != null && !getGs().learningManager.isNaughty()) {
                    if (Util.broodWarDistance(getGs().mainChoke.getCenter().toPosition(), center) >= 200 &&
                            getGs().getArmySize() < getGs().strat.armyForAttack) {
                        if (u.getOrder() != Order.Move) move = getGs().mainChoke.getCenter().toPosition();
                    }
                } else if (Util.broodWarDistance(u.getPosition(), center) >= 180 && u.getOrder() != Order.Move) {
                    if (getGs().getGame().getBWMap().isWalkable(center.toWalkPosition())) move = center;
                }
                if (move != null) {
                    int range2 = UnitType.Terran_Marine.groundWeapon().maxRange();
                    if (u.getOrder() == Order.AttackMove) {
                        if (u.getDistance(move) <= range2 * ((double) (new Random().nextInt((10 + 1) - 4) + 4)) / 10.0 && Util.shouldIStop(u.getPosition())) {
                            UtilMicro.stop(u);
                            return;
                        }
                    } else if (u.getDistance(move) > range2) {
                        UtilMicro.attack(u, move);
                        return;
                    }
                }
                break;
            case REGROUP:
                if (u.isDefenseMatrixed() || getGs().sim.farFromFight(u, squadSim)) return;
                Position pos = getGs().getNearestCC(u.getPosition());
                if (Util.broodWarDistance(pos, u.getPosition()) >= 400) {
                    UtilMicro.move(u, pos);
                }
                break;
            case ADVANCE:
                double dist = Util.broodWarDistance(u.getPosition(), center);
                if (dist >= 260) {
                    UtilMicro.move(u, center);
                    return;
                } else if (attack != null) UtilMicro.move(u, attack);
                break;
        }
    }

    private void microTank(SiegeTank u) {
        if (u.isSieged() && u.getOrder() == Order.Unsieging) return;
        if (!u.isSieged() && u.getOrder() == Order.Sieging) return;
        switch (status) {
            case ATTACK:
            case DEFENSE:
                boolean found = false;
                boolean close = false;
                for (Unit e : squadSim.enemies) {
                    if (e.isFlying() || e instanceof Worker || (e instanceof Building && !Util.isStaticDefense(e)))
                        continue;
                    double distance = u.getDistance(e);
                    if (!found && distance <= UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange() && ((PlayerUnit) e).getHitPoints() + ((PlayerUnit) e).getShields() > 60) {
                        found = true;
                    }
                    if (distance <= UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().minRange()) close = true;
                    if (found && close) break;
                }
                if (found && !close) {
                    if (!u.isSieged() && u.getOrder() != Order.Sieging) {
                        u.siege();
                        return;
                    }

                } else if (u.isSieged() && u.getOrder() != Order.Unsieging && Math.random() * 10 <= 4) {
                    u.unsiege();
                    return;
                }
                Set<Unit> tankTargets = squadSim.enemies.stream().filter(e -> !e.isFlying()).collect(Collectors.toSet());
                Unit target = Util.getTankTarget(u, tankTargets);
                UtilMicro.attack(u, target);
                break;
            case IDLE:
                Position move = null;
                if (getGs().defendPosition != null) {
                    if (u.getOrder() != Order.Move) move = getGs().defendPosition;
                } else if (!getGs().DBs.isEmpty()) {
                    Unit bunker = getGs().DBs.keySet().iterator().next();
                    if (Util.broodWarDistance(bunker.getPosition(), center) >= 180 &&
                            getGs().getArmySize() < getGs().strat.armyForAttack && !getGs().strat.name.equals("ProxyBBS") && !getGs().strat.name.equals("ProxyEightRax")) {
                        if (u.getOrder() != Order.Move) move = bunker.getPosition();
                    }
                } else if (getGs().mainChoke != null && !getGs().learningManager.isNaughty() && !getGs().strat.name.equals("ProxyBBS") && !getGs().strat.name.equals("ProxyEightRax")) {
                    if (Util.broodWarDistance(getGs().mainChoke.getCenter().toPosition(), center) >= 200 &&
                            getGs().getArmySize() < getGs().strat.armyForAttack) {
                        if (u.getOrder() != Order.Move) move = getGs().mainChoke.getCenter().toPosition();
                    }
                } else if (Util.broodWarDistance(u.getPosition(), center) >= 180 && u.getOrder() != Order.Move) {
                    if (getGs().getGame().getBWMap().isWalkable(center.toWalkPosition())) move = center;
                }
                if (move != null) {
                    WeaponType weapon = Util.getWeapon(u.getType());
                    int range = weapon.maxRange();
                    if (u.getOrder() == Order.AttackMove) {
                        if (u.getDistance(move) <= range * ((double) (new Random().nextInt((10 + 1) - 4) + 4)) / 10.0 && Util.shouldIStop(u.getPosition())) {
                            if (!u.isSieged() && getGs().getPlayer().hasResearched(TechType.Tank_Siege_Mode)) {
                                u.siege();
                            } else UtilMicro.stop(u);
                            return;
                        }
                    } else if (u.getDistance(move) > range) {
                        if (u.isSieged() && !getGs().defense) {
                            u.unsiege();
                        } else UtilMicro.attack(u, move);
                        return;
                    }
                }
                break;
            case REGROUP:
                if (u.isDefenseMatrixed() || getGs().sim.farFromFight(u, squadSim)) return;
                Position pos = getGs().getNearestCC(u.getPosition());
                if (Util.broodWarDistance(pos, u.getPosition()) >= 400) UtilMicro.move(u, pos);
                break;
            case ADVANCE:
                if (u.isSieged() && Math.random() * 10 <= 6) u.unsiege();
                else {
                    double dist = Util.broodWarDistance(u.getPosition(), center);
                    if (dist >= 300) {
                        UtilMicro.attack(u, center);
                        return;
                    } else if (attack != null) UtilMicro.move(u, attack);
                }
                break;
        }
    }

    private void microMedic(Medic u, Set<Unit> marinesToHeal) {
        PlayerUnit healTarget = getHealTarget(u, marinesToHeal);
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
            Position pos = getGs().getNearestCC(u.getPosition());
            if (Util.broodWarDistance(pos, u.getPosition()) >= 400) {
                UtilMicro.heal(u, pos);
            }
        } else if (status == Status.ADVANCE && attack != null) {
            UtilMicro.heal(u, attack);
        } else if (attack != null && center.getDistance(u.getPosition()) <= 150) {
            UtilMicro.heal(u, attack);
        } else UtilMicro.heal(u, center);
    }

    private boolean shouldStim(Marine u) {
        if (u.isStimmed() || u.getHitPoints() < 25) return false;
        Unit target = u.getTargetUnit();
        if (target != null) {
            double range = u.getPlayer().getUnitStatCalculator().weaponMaxRange(WeaponType.Gauss_Rifle);
            double distToTarget = u.getDistance(target);
            return distToTarget > (range - 32) && (target instanceof Attacker || target instanceof SpellCaster);
        }
        return false;
    }

    private boolean shouldStim(Firebat u) {
        if (u.isStimmed() || u.getHitPoints() < 25) return false;
        Unit target = u.getTargetUnit();
        if (target != null) {
            double range = u.getPlayer().getUnitStatCalculator().weaponMaxRange(WeaponType.Flame_Thrower);
            double distToTarget = u.getDistance(target);
            return distToTarget > (range - 32) && (target instanceof Attacker || target instanceof SpellCaster);
        }
        return false;
    }

    private PlayerUnit getHealTarget(final Unit u, final Set<Unit> marinesToHeal) {
        Set<PlayerUnit> healables = getHealable();
        PlayerUnit chosen = null;
        double dist = Double.MAX_VALUE;
        for (PlayerUnit m : healables) {
            if (m.getHitPoints() == m.getType().maxHitPoints() || marinesToHeal.contains(m)) continue;
            double distA = m.getDistance(u);
            if (chosen == null || distA < dist) {
                chosen = m;
                dist = distA;
            }
        }
        return chosen;
    }

    private Set<PlayerUnit> getHealable() {
        Set<PlayerUnit> aux = new TreeSet<>();
        for (PlayerUnit u : this.members) {
            if (u instanceof Marine || u instanceof Firebat) aux.add(u);
        }
        return aux;
    }

    private void executeRangedAttackLogic(MobileUnit u) {
        double speed = u.getType().topSpeed();
        Unit target = Util.getRangedTarget(u, squadSim.enemies, attack);
        if (target == null) {
            if (attack != null) UtilMicro.attack(u, attack);
            return;
        }
        if (speed < 0.001 && (target.isFlying() ? ((AirAttacker) u).getAirWeaponCooldown() == 0 : ((GroundAttacker) u).getGroundWeaponCooldown() == 0)) {
            UtilMicro.attack((Attacker) u, target);
            return;
        }
        if (getGs().frameCount - u.getLastCommandFrame() <= 15) return;
        WeaponType w = Util.getWeapon(u, target);
        double range = u.getPlayer().getUnitStatCalculator().weaponMaxRange(w);
        double distToTarget = u.getDistance(target);
        int cooldown = (target.isFlying() ? ((AirAttacker) u).getAirWeaponCooldown() : ((GroundAttacker) u).getGroundWeaponCooldown()) - getGs().getIH().getRemainingLatencyFrames() - 2;
        int framesToFiringRange = (int) Math.ceil(Math.max(0, distToTarget - range) / speed);
        if (cooldown <= framesToFiringRange) {
            UtilMicro.attack((Attacker) u, target);
            return;
        }
        int targetRange = ((PlayerUnit) target).getPlayer().getUnitStatCalculator().weaponMaxRange(Util.getWeapon(target, u));
        boolean kite = true;
        boolean moveCloser = target.getType() == UnitType.Terran_Siege_Tank_Siege_Mode ||
                (target instanceof SCV && ((SCV) target).isRepairing() && ((SCV) target).getOrderTarget() != null && ((SCV) target).getOrderTarget().getType() == UnitType.Terran_Bunker) ||
                (target.getType().isBuilding() && !Util.canAttack((PlayerUnit) target, u));
        if (!moveCloser) {
            Position predictedPosition = UtilMicro.predictUnitPosition(target, 1);
            if (predictedPosition != null && getGs().getGame().getBWMap().isValidPosition(predictedPosition)) {
                double distPredicted = u.getDistance(predictedPosition);
                double distCurrent = u.getDistance(target.getPosition());
                if (distPredicted > distCurrent) {
                    kite = false;
                    if (distToTarget > (range - 24)) moveCloser = true;
                } else if (distCurrent == distPredicted && range >= (targetRange + 64) && distToTarget > (range - 48)) {
                    moveCloser = true;
                }
            }
        }
        if (moveCloser) {
            if (distToTarget > 16) UtilMicro.move(u, target.getPosition());
            else UtilMicro.attack((Attacker) u, target);
            return;
        }
        if (kite) {
            //Position kitePos = UtilMicro.kiteAway(u, squadSim.enemies);
            Position kitePos = UtilMicro.kiteAwayAlt(u.getPosition(), target.getPosition());
            if (kitePos != null) UtilMicro.move(u, kitePos);
        } else UtilMicro.attack((Attacker) u, target);
    }

    public void giveMoveOrder(Position retreat) {
        int frameCount = getGs().frameCount;
        for (Unit u : members) {
            PlayerUnit pU = (PlayerUnit) u;
            if (u.getType() == UnitType.Terran_Siege_Tank_Siege_Mode && pU.getOrder() == Order.Unsieging) {
                continue;
            }
            if (u.getType() == UnitType.Terran_Siege_Tank_Tank_Mode && pU.getOrder() == Order.Sieging) continue;
            Position lastTarget = ((MobileUnit) u).getTargetPosition() == null ? pU.getOrderTargetPosition() :
                    ((MobileUnit) u).getTargetPosition();
            if (lastTarget != null && lastTarget.equals(retreat)) continue;
            if (attack != null && frameCount != pU.getLastCommandFrame()) ((MobileUnit) u).move(retreat);
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
