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
    public Set<UnitInfo> members = new TreeSet<>();
    public Status status;
    private Position center;
    private Integer id;
    private SimInfo squadSim;
    protected boolean lose;

    Squad(int id, Position center, SimInfo squadSim) {
        this.id = id;
        this.lose = squadSim.lose;
        this.squadSim = squadSim;
        for (UnitInfo m : squadSim.allies) {
            if (isArmyUnit(m.unit) && !getGs().agents.containsKey(m.unit)) this.members.add(m);
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
        for (UnitInfo u : members) {
            count++;
            if (u.unit instanceof Goliath || u.unit instanceof SiegeTank || u.unit instanceof Vulture || u.unit instanceof Wraith)
                count++;
        }
        return count;
    }

    private void setSquadStatus() {
        if (status == Status.DEFENSE) return;
        if (status != Status.IDLE && (squadSim.lose || members.stream().noneMatch(u -> u.unitType != UnitType.Terran_Medic)))
            status = Status.REGROUP;
        else if (status == Status.ATTACK && squadSim.enemies.isEmpty()) status = Status.ADVANCE;
        else if (status == Status.IDLE && !squadSim.enemies.isEmpty() && !IntelligenceAgency.enemyIsRushing() && (getGs().defendPosition == null || getGs().defendPosition.getDistance(center) <= 350))
            if(!squadSim.lose) status = Status.ATTACK;
            else status = Status.REGROUP;
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
            for (UnitInfo u : members) {
                if (u.unit.isLockedDown() || u.unit.isMaelstrommed() || ((MobileUnit) u.unit).isStasised() || ((MobileUnit) u.unit).getTransport() != null)
                    continue;
                if (u.unit instanceof Marine && shouldStim(u)) ((Marine) u.unit).stimPack();
                if (u.unit instanceof Firebat && shouldStim(u)) ((Firebat) u.unit).stimPack();
                if (u.unit instanceof Medic) microMedic((Medic) u.unit, marinesToHeal);
                else if (u.unit instanceof SiegeTank) microTank(u);
                else if (u.unitType.groundWeapon().maxRange() > 32) microRanged(u);
                else microMelee(u);
            }
        } catch (Exception e) {
            System.err.println("microUpdateOrder Error");
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
                Position move = null;
                if (getGs().defendPosition != null) {
                    if (u.currentOrder != Order.Move) move = getGs().defendPosition;
                } else if (!getGs().DBs.isEmpty()) {
                    Unit bunker = getGs().DBs.keySet().iterator().next();
                    if (Util.broodWarDistance(bunker.getPosition(), center) >= 190 &&
                            getGs().getArmySize() < getGs().strat.armyForAttack && !getGs().strat.name.equals("ProxyBBS") && !getGs().strat.name.equals("ProxyEightRax")) {
                        if (u.currentOrder != Order.Move) move = bunker.getPosition();
                    }
                } else if (getGs().mainChoke != null && !getGs().learningManager.isNaughty() && !getGs().strat.name.equals("ProxyBBS") && !getGs().strat.name.equals("ProxyEightRax")) {
                    if (Util.broodWarDistance(getGs().mainChoke.getCenter().toPosition(), center) >= 200 &&
                            getGs().getArmySize() < getGs().strat.armyForAttack) {
                        if (u.currentOrder != Order.Move) move = getGs().mainChoke.getCenter().toPosition();
                    }
                } else if (Util.broodWarDistance(u.position, center) >= 190 && u.currentOrder != Order.Move) {
                    if (getGs().getGame().getBWMap().isWalkable(center.toWalkPosition())) move = center;
                }
                if (move != null) {
                    WeaponType weapon = Util.getWeapon(u.unitType);
                    int range2 = weapon == WeaponType.None ? UnitType.Terran_Marine.groundWeapon().maxRange() : weapon.maxRange();
                    if (u.currentOrder == Order.AttackMove) {
                        if (u.unit.getDistance(move) <= range2 * ((double) (new Random().nextInt((10 + 1) - 4) + 4)) / 10.0 && Util.shouldIStop(u.position)) {
                            UtilMicro.stop((MobileUnit) u.unit);
                            return;
                        }
                    } else if (u.unit.getDistance(move) > range2) {
                        UtilMicro.attack((MobileUnit) u.unit, move);
                        return;
                    }
                }
                break;
            case REGROUP:
                if (((MobileUnit) u.unit).isDefenseMatrixed() || getGs().sim.farFromFight(u, squadSim, false) || squadSim.enemies.stream().noneMatch(e -> Util.getWeapon(e, u).maxRange() > 32)) {
                    executeRangedAttackLogic(u);
                    return;
                }
                Position pos = getGs().getNearestCC(u.position, true);
                if (Util.broodWarDistance(pos, u.position) >= 400) {
                    UtilMicro.move((MobileUnit) u.unit, pos);
                }
                break;
            case ADVANCE:
                double dist = Util.broodWarDistance(u.position, center);
                if (dist >= 240) {
                    UtilMicro.move((MobileUnit) u.unit, center);
                    return;
                } else if (attack != null) UtilMicro.move((MobileUnit) u.unit, attack);
                break;
        }
    }

    private void microMelee(UnitInfo u) {
        switch (status) {
            case ATTACK:
            case DEFENSE:
                if (squadSim.enemies.isEmpty() && attack != null) {
                    UtilMicro.attack((MobileUnit) u.unit, attack);
                    return;
                }
                //Experimental storm dodging?
                if (((MobileUnit) u.unit).isUnderStorm()) {
                    Position closestCC = getGs().getNearestCC(u.position, true);
                    if (closestCC != null) {
                        UtilMicro.move((MobileUnit) u.unit, closestCC);
                        return;
                    }
                }
                if (attack != null && !u.unit.isStartingAttack() && !u.unit.isAttacking()) {
                    UnitInfo target = Util.getRangedTarget(u, squadSim.enemies, attack);
                    if (target != null) UtilMicro.attack((Attacker) u.unit, target);
                    else if (attack != null) UtilMicro.attack((MobileUnit) u.unit, attack);
                }
                break;
            case IDLE:
                Position move = null;
                if (getGs().strat.proxy && !getGs().MBs.isEmpty()) {
                    Position firstRax = getGs().MBs.iterator().next().getPosition();
                    if (firstRax.getDistance(u.position) > 200) {
                        UtilMicro.move((MobileUnit) u.unit, firstRax);
                        return;
                    }
                } else if (getGs().defendPosition != null) {
                    if (u.currentOrder != Order.Move) move = getGs().defendPosition;
                } else if (!getGs().DBs.isEmpty()) {
                    Unit bunker = getGs().DBs.keySet().iterator().next();
                    if (Util.broodWarDistance(bunker.getPosition(), center) >= 180 &&
                            getGs().getArmySize() < getGs().strat.armyForAttack) {
                        if (u.currentOrder != Order.Move) move = bunker.getPosition();
                    }
                } else if (getGs().mainChoke != null && !getGs().learningManager.isNaughty()) {
                    if (Util.broodWarDistance(getGs().mainChoke.getCenter().toPosition(), center) >= 200 &&
                            getGs().getArmySize() < getGs().strat.armyForAttack) {
                        if (u.currentOrder != Order.Move) move = getGs().mainChoke.getCenter().toPosition();
                    }
                } else if (Util.broodWarDistance(u.position, center) >= 180 && u.currentOrder != Order.Move) {
                    if (getGs().getGame().getBWMap().isWalkable(center.toWalkPosition())) move = center;
                }
                if (move != null) {
                    int range2 = UnitType.Terran_Marine.groundWeapon().maxRange();
                    if (u.currentOrder == Order.AttackMove) {
                        if (u.unit.getDistance(move) <= range2 * ((double) (new Random().nextInt((10 + 1) - 4) + 4)) / 10.0 && Util.shouldIStop(u.position)) {
                            UtilMicro.stop((MobileUnit) u.unit);
                            return;
                        }
                    } else if (u.unit.getDistance(move) > range2) {
                        UtilMicro.attack((MobileUnit) u.unit, move);
                        return;
                    }
                }
                break;
            case REGROUP:
                if (((MobileUnit) u.unit).isDefenseMatrixed() || getGs().sim.farFromFight(u, squadSim, true)) return;
                Position pos = getGs().getNearestCC(u.position, true);
                if (Util.broodWarDistance(pos, u.position) >= 400) {
                    UtilMicro.move((MobileUnit) u.unit, pos);
                }
                break;
            case ADVANCE:
                double dist = Util.broodWarDistance(u.position, center);
                if (dist >= 260) {
                    UtilMicro.move((MobileUnit) u.unit, center);
                    return;
                } else if (attack != null) UtilMicro.move((MobileUnit) u.unit, attack);
                break;
        }
    }

    private void microTank(UnitInfo u) {
        SiegeTank st = (SiegeTank) u.unit;
        if (st.isSieged() && u.currentOrder == Order.Unsieging) return;
        if (!st.isSieged() && u.currentOrder == Order.Sieging) return;
        switch (status) {
            case ATTACK:
            case DEFENSE:
                boolean found = false;
                boolean close = false;
                for (UnitInfo e : squadSim.enemies) {
                    if (e.flying || e.unit instanceof Worker || e.unit instanceof Medic || (e.unitType.isBuilding() && !Util.isStaticDefense(e)))
                        continue;
                    double distance = u.getDistance(e);
                    if (!found && distance <= UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange() && (e.health + e.shields >= 60 || squadSim.enemies.size() > 2)) {
                        found = true;
                    }
                    if (!close && distance <= UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().minRange())
                        close = true;
                    if (found && close) break;
                }
                if (found && !close) {
                    if (!st.isSieged() && u.currentOrder != Order.Sieging) {
                        st.siege();
                        return;
                    }

                } else if (st.isSieged() && u.currentOrder != Order.Unsieging && Math.random() * 10 <= 2.5) {
                    st.unsiege();
                    return;
                }
                Set<UnitInfo> tankTargets = squadSim.enemies.stream().filter(e -> !e.flying).collect(Collectors.toSet());
                UnitInfo target = Util.getTankTarget(u, tankTargets);
                UtilMicro.attack(st, target);
                break;
            case IDLE:
                Position move = null;
                if (getGs().defendPosition != null) {
                    move = getGs().defendPosition;
                } else if (!getGs().DBs.isEmpty()) {
                    Unit bunker = getGs().DBs.keySet().iterator().next();
                    if (Util.broodWarDistance(bunker.getPosition(), center) >= 180 &&
                            getGs().getArmySize() < getGs().strat.armyForAttack && !getGs().strat.name.equals("ProxyBBS") && !getGs().strat.name.equals("ProxyEightRax")) {
                        if (u.currentOrder != Order.Move) move = bunker.getPosition();
                    }
                } else if (getGs().mainChoke != null && !getGs().learningManager.isNaughty() && !getGs().strat.name.equals("ProxyBBS") && !getGs().strat.name.equals("ProxyEightRax")) {
                    if (Util.broodWarDistance(getGs().mainChoke.getCenter().toPosition(), center) >= 200 &&
                            getGs().getArmySize() < getGs().strat.armyForAttack) {
                        if (u.currentOrder != Order.Move) move = getGs().mainChoke.getCenter().toPosition();
                    }
                } else if (Util.broodWarDistance(u.position, center) >= 180 && u.currentOrder != Order.Move) {
                    if (getGs().getGame().getBWMap().isWalkable(center.toWalkPosition())) move = center;
                }
                if (move != null) {
                    WeaponType weapon = Util.getWeapon(u.unitType);
                    int range = weapon.maxRange();
                    if (u.currentOrder == Order.AttackMove || u.currentOrder == Order.PlayerGuard) {
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
            Position pos = getGs().getNearestCC(u.getPosition(), true);
            if (Util.broodWarDistance(pos, u.getPosition()) >= 400) {
                UtilMicro.heal(u, pos);
            }
        } else if (status == Status.ADVANCE && attack != null) {
            UtilMicro.heal(u, attack);
            //} else if (attack != null && center.getDistance(u.getPosition()) <= 150) {
            //UtilMicro.heal(u, attack);
        } else UtilMicro.heal(u, center);
    }

    private boolean shouldStim(UnitInfo u) {
        if (u.unitType == UnitType.Terran_Marine && ((Marine) u.unit).isStimmed() || u.health <= 25) return false;
        if (u.unitType == UnitType.Terran_Firebat && ((Firebat) u.unit).isStimmed() || u.health <= 25) return false;
        Unit target = u.target;
        if (target != null) {
            UnitInfo targetUI = getGs().unitStorage.getEnemyUnits().get(target);
            double range = u.groundRange;
            double distToTarget;
            if (targetUI != null) distToTarget = u.getDistance(targetUI);
            else distToTarget = u.unit.getDistance(target);
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
        for (UnitInfo u : this.members) {
            if (u.unit instanceof Marine || u.unit instanceof Firebat) aux.add(u.unit);
        }
        return aux;
    }

    private void executeRangedAttackLogic(UnitInfo u) {
        UnitInfo target = Util.getRangedTarget(u, squadSim.enemies, attack);
        if (target == null) {
            if (attack != null) UtilMicro.attack((MobileUnit) u.unit, attack);
            return;
        }
        double speed = u.speed;
        if (getGs().frameCount - u.unit.getLastCommandFrame() <= 15) return;
        WeaponType w = Util.getWeapon(u, target);
        double range = u.player.getUnitStatCalculator().weaponMaxRange(w);
        double distToTarget = u.getDistance(target);
        int cooldown = (target.flying ? ((AirAttacker) u.unit).getAirWeaponCooldown() : ((GroundAttacker) u.unit).getGroundWeaponCooldown()) - getGs().getIH().getRemainingLatencyFrames() - 2;
        int framesToFiringRange = (int) Math.ceil(Math.max(0, distToTarget - range) / speed);
        if (cooldown <= framesToFiringRange) {
            UtilMicro.attack((Attacker) u.unit, target);
            return;
        }
        Position predictedPosition = null;
        int targetRange = target.player.getUnitStatCalculator().weaponMaxRange(Util.getWeapon(target, u));
        boolean kite = true;
        boolean moveCloser = target.unitType == UnitType.Terran_Siege_Tank_Siege_Mode ||
                (target.unit instanceof SCV && ((SCV) target.unit).isRepairing() && target.unit.getOrderTarget() != null && target.unit.getOrderTarget().getType() == UnitType.Terran_Bunker) ||
                (target.unitType.isBuilding() && !Util.canAttack(target, u));
        if (!moveCloser) {
            predictedPosition = UtilMicro.predictUnitPosition(target.unit, 2);
            if (predictedPosition != null && getGs().getGame().getBWMap().isValidPosition(predictedPosition)) {
                double distPredicted = u.unit.getDistance(predictedPosition);
                double distCurrent = u.unit.getDistance(target.lastPosition);
                if (distPredicted > distCurrent) {
                    kite = false;
                    if (distToTarget > (range - 24)) moveCloser = true;
                } else if (distCurrent == distPredicted && range >= (targetRange + 64) && distToTarget > (range - 48)) {
                    moveCloser = true;
                }
            }
        }
        if (moveCloser) {
            if (distToTarget > 32) {
                if (predictedPosition != null) UtilMicro.move((MobileUnit) u.unit, predictedPosition);
                else UtilMicro.move((MobileUnit) u.unit, target.lastPosition);
            } else UtilMicro.attack((Attacker) u.unit, target);
            return;
        }
        if (kite && target.unitType.topSpeed() > 0) {
            Position kitePos = UtilMicro.kiteAwayAlt(u.position, target.lastPosition);
            if (kitePos != null) UtilMicro.move((MobileUnit) u.unit, kitePos);
            else {
                kitePos = UtilMicro.kiteAway(u.unit, squadSim.enemies);
                if (kitePos != null) UtilMicro.move((MobileUnit) u.unit, kitePos);
            }
        } else UtilMicro.attack((Attacker) u.unit, target);
    }

    public void giveMoveOrder(Position retreat) {
        int frameCount = getGs().frameCount;
        for (UnitInfo u : members) {
            PlayerUnit pU = u.unit;
            if (u.unitType == UnitType.Terran_Siege_Tank_Siege_Mode && pU.getOrder() == Order.Unsieging) {
                continue;
            }
            if (u.unitType == UnitType.Terran_Siege_Tank_Tank_Mode && pU.getOrder() == Order.Sieging) continue;
            Position lastTarget = ((MobileUnit) u.unit).getTargetPosition() == null ? pU.getOrderTargetPosition() :
                    ((MobileUnit) u.unit).getTargetPosition();
            if (lastTarget != null && lastTarget.equals(retreat)) continue;
            if (attack != null && frameCount != pU.getLastCommandFrame()) ((MobileUnit) u.unit).move(retreat);
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
