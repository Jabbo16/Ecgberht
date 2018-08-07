package ecgberht;

import ecgberht.Agents.VesselAgent;
import ecgberht.Simulation.SimInfo;
import ecgberht.Util.Util;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.type.WeaponType;
import org.openbw.bwapi4j.unit.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static ecgberht.Ecgberht.getGame;
import static ecgberht.Ecgberht.getGs;

public class Squad implements Comparable<Squad> {

    private static boolean stimResearched;
    public int lastFrameOrder = 0;
    public Position attack;
    public Set<PlayerUnit> members;
    public Status status;
    public String name;
    VesselAgent detector;

    public Squad(String name) {
        this.name = name;
        members = new TreeSet<>();
        status = Status.IDLE;
        attack = null;
    }

    void addToSquad(Unit unit) {
        this.members.add((PlayerUnit) unit);
    }

    public void giveAttackOrder(Position pos) {
        if (!pos.equals(attack)) attack = pos;
    }

    int getArmyCount() {
        int count = 0;
        for (Unit u : members) {
            count++;
            if (u instanceof SiegeTank || u instanceof Vulture || u instanceof Wraith) count++;
        }
        return count;
    }

    void microUpdateOrder() {
        try {
            if (members.isEmpty()) return;
            Set<Unit> enemy = getGs().enemyCombatUnitMemory;
            int frameCount = getGs().frameCount;
            Position start = getGs().ih.self().getStartLocation().toPosition();
            Set<Unit> marinesToHeal = new TreeSet<>();
            Position sCenter = getGs().getSquadCenter(this);
            if (!stimResearched && getGs().getPlayer().hasResearched(TechType.Stim_Packs)) stimResearched = true;
            for (EnemyBuilding b : getGs().enemyBuildingMemory.values()) {
                if (!b.unit.isVisible()) continue;
                if (b.type.canAttack() || b.type == UnitType.Terran_Bunker) enemy.add(b.unit);
            }
            for (PlayerUnit u : members) {
                if (u.isLockedDown() || u.isMaelstrommed() || ((MobileUnit) u).isStasised() || ((MobileUnit) u).getTransport() != null)
                    continue;
                Position lastTarget = u.getOrderTargetPosition() == null ? ((MobileUnit) u).getTargetPosition() :
                        u.getOrderTargetPosition();
                if (stimResearched && (u instanceof Marine || u instanceof Firebat)) {
                    if (u instanceof Marine && !((Marine) u).isStimmed() && u.isAttacking() && u.getHitPoints() >= 25) {
                        ((Marine) u).stimPack();
                    } else if (u instanceof Firebat && !((Firebat) u).isStimmed() && u.isAttacking() && u.getHitPoints() >= 25) {
                        ((Firebat) u).stimPack();
                    }
                }
                if (u instanceof SiegeTank) {
                    SiegeTank t = (SiegeTank) u;
                    if (t.isSieged() && u.getOrder() == Order.Unsieging) continue;
                    if (!t.isSieged() && u.getOrder() == Order.Sieging) continue;
                    if (status == Status.IDLE && t.isSieged()) return;
                    boolean found = false;
                    for (Unit e : enemy) {
                        if (e.isFlying() || e instanceof Worker) continue;
                        double distance = u.getDistance(e);
                        if (distance <= UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange()) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        if (!t.isSieged() && t.getOrder() != Order.Sieging) {
                            t.siege();
                            continue;
                        }
                    } else if (t.isSieged() && t.getOrder() != Order.Unsieging && Math.random() * 10 <= 2) {
                        t.unsiege();
                        continue;
                    }
                }
                if (status == Status.IDLE) {
                    Position move = null;
                    if (getGs().defendPosition != null) {
                        if (u.getOrder() != Order.Move) move = getGs().defendPosition;
                    } else if (!getGs().DBs.isEmpty()) {
                        Unit bunker = getGs().DBs.keySet().iterator().next();
                        if (getGs().broodWarDistance(bunker.getPosition(), sCenter) >= 180 &&
                                getGs().getArmySize() < getGs().strat.armyForAttack && !getGs().strat.name.equals("ProxyBBS")) {
                            if (u.getOrder() != Order.Move) move = bunker.getPosition();
                        }
                    } else if (getGs().mainChoke != null && !getGs().EI.naughty && !getGs().strat.name.equals("ProxyBBS")) {
                        if (getGs().broodWarDistance(getGs().mainChoke.getCenter().toPosition(), sCenter) >= 200 &&
                                getGs().getArmySize() < getGs().strat.armyForAttack && !getGs().expanding) {
                            if (u.getOrder() != Order.Move) move = getGs().mainChoke.getCenter().toPosition();
                        }
                    } else if (getGs().broodWarDistance(u.getPosition(), sCenter) >= 180 && u.getOrder() != Order.Move) {
                        if (getGame().getBWMap().isWalkable(sCenter.toWalkPosition())) move = sCenter;
                    }
                    if (move != null) {
                        WeaponType weapon = Util.getWeapon(u.getInitialType());
                        int range = weapon == WeaponType.None ? UnitType.Terran_Marine.groundWeapon().maxRange() : (weapon.maxRange() > 32 ? weapon.maxRange() : UnitType.Terran_Marine.groundWeapon().maxRange());
                        if (u.getOrder() == Order.AttackMove || u.getOrder() == Order.HealMove) {
                            if (u.getDistance(move) <= range)
                                if (u instanceof SiegeTank && !((SiegeTank) u).isSieged() && getGs().getPlayer().hasResearched(TechType.Tank_Siege_Mode)) {
                                    ((SiegeTank) u).siege();
                                } else ((MobileUnit) u).stop(false);
                        } else if (u.getDistance(move) > range && (lastTarget == null || (lastTarget != null && !lastTarget.equals(move)))) {
                            ((MobileUnit) u).attack(move);
                            continue;
                        }
                    }
                }
                SimInfo s = getGs().sim.getSimulation(u, SimInfo.SimType.MIX);
                boolean retreat = s.lose;
                if (!retreat || status == Status.DEFENSE || ((MobileUnit) u).isDefenseMatrixed() || getGs().sim.farFromFight(u, s)) {
                    retreat = false;
                }
                String retreating = ColorUtil.formatText(retreat ? "Retreating" : "Fighting", ColorUtil.White);
                getGs().getGame().getMapDrawer().drawTextMap(u.getPosition().add(new Position(0, u.getInitialType().tileHeight())), retreating);
                if (retreat) {
                    Position pos = getGs().getNearestCC(u.getPosition());
                    if (getGs().broodWarDistance(pos, u.getPosition()) >= 400 && (lastTarget == null ||
                            (lastTarget != null && !lastTarget.equals(pos)))) {
                        ((MobileUnit) u).move(pos);
                        continue;
                    }
                }
                // Experimental
                if (status == Status.ATTACK && getGs().getGame().getBWMap().isWalkable(sCenter.toWalkPosition()) && getGs().supplyMan.getSupplyUsed() < 240) {
                    if (members.size() == 1) continue;
                    double dist = getGs().broodWarDistance(u.getPosition(), sCenter);
                    if (dist >= 300 && u.getOrderTargetPosition() != null) {
                        if (!u.getOrderTargetPosition().equals(sCenter)) {
                            ((MobileUnit) u).attack(sCenter);
                            continue;
                        }
                    }
                }
                if (u instanceof Medic && u.getOrder() != Order.MedicHeal) {
                    PlayerUnit chosen = getHealTarget(u, marinesToHeal);
                    if (chosen != null && u.getOrderTarget() != chosen) {
                        ((Medic) u).heal(chosen);
                        marinesToHeal.add(chosen);
                        continue;
                    }
                }
                if (u.isIdle() && attack != null && frameCount != u.getLastCommandFrame() &&
                        getGs().broodWarDistance(attack, u.getPosition()) >= 400) {
                    ((MobileUnit) u).attack(attack);
                    continue;
                }
                if (u.isAttacking() && attack == null && frameCount != u.getLastCommandFrame() &&
                        getGs().broodWarDistance(sCenter, u.getPosition()) >= 500) {
                    ((MobileUnit) u).move(sCenter);
                    continue;
                }
                if (!getGs().strat.name.equals("PlasmaWraithHell") &&
                        getGs().getGame().getBWMap().mapHash().equals("6f5295624a7e3887470f3f2e14727b1411321a67")) {
                    if (!(u instanceof Attacker)) continue;
                    Unit target = Util.getTarget(u, getGs().enemyCombatUnitMemory);
                    Unit lastTargetUnit = (((Attacker) u).getTargetUnit() == null ? u.getOrderTarget() :
                            ((Attacker) u).getTargetUnit());
                    if (lastTargetUnit != null) {
                        if (!lastTargetUnit.equals(target)) {
                            ((Attacker) u).attack(target);
                            continue;
                        }
                    }
                }
                if (lastTarget != null && lastTarget.equals(attack)) continue;
                if ((status == Status.ATTACK) && u.getOrder() != null && u.getOrder() == Order.AttackMove &&
                        !u.getOrderTargetPosition().equals(attack)) {
                    if (u instanceof MobileUnit) {
                        ((MobileUnit) u).attack(attack);
                        continue;
                    }
                }
                int framesToOrder = 18;
                if (u.getInitialType() == UnitType.Terran_Vulture) framesToOrder = 12;
                if (frameCount - u.getLastCommandFrame() >= framesToOrder) {
                    if (u.isIdle() && attack != null && status != Status.IDLE) {
                        lastTarget = (((MobileUnit) u).getTargetPosition() == null ? u.getOrderTargetPosition() :
                                ((MobileUnit) u).getTargetPosition());
                        if (lastTarget != null) {
                            if (!lastTarget.equals(attack)) {
                                ((MobileUnit) u).attack(attack);
                                continue;
                            }
                        }
                    }
                    //Experimental storm dodging?
                    if (((MobileUnit) u).isUnderStorm()) {
                        ((MobileUnit) u).move(start);
                        continue;
                    }
                    Set<Unit> enemyToKite = new TreeSet<>();
                    Set<Unit> enemyToAttack = new TreeSet<>();
                    for (Unit e : enemy) {
                        UnitType eType = e.getInitialType();
                        if (eType == UnitType.Zerg_Larva || eType == UnitType.Zerg_Overlord) continue;
                        enemyToAttack.add(e);
                        if (!(e instanceof Building) && !e.getInitialType().isFlyer() && e.getInitialType().groundWeapon().maxRange() <= 32
                                && e.getInitialType() != UnitType.Terran_Medic) {
                            if (getGs().broodWarDistance(u.getPosition(), e.getPosition()) <=
                                    u.getInitialType().groundWeapon().maxRange()) {
                                enemyToKite.add(e);
                            }
                        }
                    }
                    if (u instanceof GroundAttacker && ((GroundAttacker) u).getGroundWeaponCooldown() > 0) {
                        if (!enemyToKite.isEmpty()) {
                            Position run = getGs().kiteAway(u, enemyToKite);
                            if (getGs().getGame().getBWMap().isValidPosition(run)) {
                                ((MobileUnit) u).move(run);
                            } else ((MobileUnit) u).move(getGs().getPlayer().getStartLocation().toPosition());
                        }
                    } else if (attack != null && !u.isStartingAttack() && !u.isAttacking()) {
                        if (getGs().strat.name.equals("ProxyBBS")) {
                            if (!enemyToAttack.isEmpty() && u instanceof Attacker) {
                                Unit target = Util.getTarget(u, enemyToAttack);
                                Unit lastTargetUnit = (((Attacker) u).getTargetUnit() == null ? u.getOrderTarget() :
                                        ((Attacker) u).getTargetUnit());
                                if (lastTargetUnit != null) {
                                    if (!lastTargetUnit.equals(target)) {
                                        ((Attacker) u).attack(target);
                                        continue;
                                    }
                                }
                            }
                        }
                        if (u.getOrder() == Order.Move) ((MobileUnit) u).attack(attack);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("microUpdateOrder Error");
            e.printStackTrace();
        }
    }

    private PlayerUnit getHealTarget(final Unit u, final Set<Unit> marinesToHeal) {
        Set<Unit> marines = getMarines();
        PlayerUnit chosen = null;
        double dist = Double.MAX_VALUE;
        for (Unit m : marines) {
            if (((PlayerUnit) m).getHitPoints() == m.getInitialType().maxHitPoints() || marinesToHeal.contains(m)) {
                continue;
            }
            double distA = getGs().broodWarDistance(m.getPosition(), u.getPosition());
            if (chosen == null || distA < dist) {
                chosen = (PlayerUnit) m;
                dist = distA;
            }
        }
        return chosen;
    }

    public Set<SiegeTank> getTanks() {
        Set<SiegeTank> aux = new HashSet<>();
        for (Unit u : members) {
            if (u instanceof SiegeTank) aux.add((SiegeTank) u);
        }
        return aux;
    }

    public Set<Unit> getMarines() {
        Set<Unit> aux = new TreeSet<>();
        for (Unit u : this.members) {
            if (u instanceof Marine) aux.add(u);
        }
        return aux;
    }

    public Set<Unit> getMedics() {
        Set<Unit> aux = new TreeSet<>();
        for (Unit u : this.members) {
            if (u instanceof Medic) aux.add(u);
        }
        return aux;
    }

    public void giveMoveOrder(Position retreat) {
        int frameCount = getGs().frameCount;
        for (PlayerUnit u : members) {
            if (u.getInitialType() == UnitType.Terran_Siege_Tank_Siege_Mode && u.getOrder() == Order.Unsieging) {
                continue;
            }
            if (u.getInitialType() == UnitType.Terran_Siege_Tank_Tank_Mode && u.getOrder() == Order.Sieging) continue;
            Position lastTarget = ((MobileUnit) u).getTargetPosition() == null ? u.getOrderTargetPosition() :
                    ((MobileUnit) u).getTargetPosition();
            if (lastTarget != null && lastTarget.equals(retreat)) continue;
            if (attack != null && frameCount != u.getLastCommandFrame()) ((MobileUnit) u).move(retreat);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Squad)) return false;
        Squad s = (Squad) o;
        return name.equals(s.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(Squad o) {
        return this.name.compareTo(o.name);
    }

    public enum Status {
        ATTACK, IDLE, DEFENSE
    }
}
