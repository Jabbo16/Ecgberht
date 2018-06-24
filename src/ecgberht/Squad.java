package ecgberht;

import ecgberht.Simulation.SimInfo;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static ecgberht.Ecgberht.getGame;
import static ecgberht.Ecgberht.getGs;

public class Squad implements Comparable<Squad> {

    public static boolean stimResearched;
    public int lastFrameOrder = 0;
    public Position attack;
    public Set<PlayerUnit> members;
    public Status status;
    public String name;

    public Squad(String name) {
        this.name = name;
        members = new TreeSet<>();
        status = Status.IDLE;
        attack = null;
    }

    public void addToSquad(Unit unit) {
        this.members.add((PlayerUnit) unit);
    }

    public void giveAttackOrder(Position pos) {
        if (!pos.equals(attack)) attack = pos;
    }

    public int getArmyCount() {
        int count = 0;
        for (Unit u : members) {
            count++;
            if (u instanceof SiegeTank || u instanceof Vulture || u instanceof Wraith) count++;
        }
        return count;
    }

    // Kiting broken, improve
    public void microUpdateOrder() {
        try {
            if (members.isEmpty()) return;
            Set<Unit> enemy = getGs().enemyCombatUnitMemory;
            int frameCount = getGs().frameCount;
            Position start = getGs().ih.self().getStartLocation().toPosition();
            Set<Unit> marinesToHeal = new HashSet<>();
            Position sCenter = getGs().getSquadCenter(this);
            if (!stimResearched && getGs().getPlayer().hasResearched(TechType.Stim_Packs)) stimResearched = true;
            for (PlayerUnit u : members) {
                if (u.getInitialType() == UnitType.Terran_Siege_Tank_Siege_Mode) continue;
                if (u.getInitialType() == UnitType.Terran_Siege_Tank_Tank_Mode && u.getOrder() == Order.Sieging) {
                    continue;
                }
                Position lastTarget = u.getOrderTargetPosition() == null ? ((MobileUnit) u).getTargetPosition() :
                        u.getOrderTargetPosition();
                if (stimResearched && (u instanceof Marine || u instanceof Firebat)) {
                    if (u instanceof Marine && !((Marine) u).isStimmed() && u.isAttacking() && u.getHitPoints() >= 25) {
                        ((Marine) u).stimPack();
                    } else if (u instanceof Firebat && !((Firebat) u).isStimmed() && u.isAttacking() && u.getHitPoints() >= 25) {
                        ((Firebat) u).stimPack();
                    }
                }
                if (status == Status.IDLE) {
                    Position move = null;
                    if (getGs().defendPosition != null) {
                        if (u.getOrder() != Order.Move) move = getGs().defendPosition;
                    } else if (!getGs().DBs.isEmpty()) {
                        Unit bunker = getGs().DBs.keySet().iterator().next();
                        if (getGs().broodWarDistance(bunker.getPosition(), sCenter) >= 170 &&
                                getGs().getArmySize() < getGs().strat.armyForAttack && getGs().strat.name != "ProxyBBS") {
                            if (u.getOrder() != Order.Move) move = bunker.getPosition();
                        }
                    } else if (getGs().mainChoke != null && !getGs().EI.naughty && getGs().strat.name != "ProxyBBS") {
                        if (getGs().broodWarDistance(getGs().mainChoke.getCenter().toPosition(), sCenter) >= 200 &&
                                getGs().getArmySize() < getGs().strat.armyForAttack && !getGs().expanding) {
                            if (u.getOrder() != Order.Move) move = getGs().mainChoke.getCenter().toPosition();
                        }
                    } else if (getGs().broodWarDistance(u.getPosition(), sCenter) >= 200 && u.getOrder() != Order.Move) {
                        if (getGame().getBWMap().isWalkable(sCenter.toWalkPosition())) move = sCenter;
                    }
                    if (move != null) {
                        if (u.getOrder() == Order.Move) {
                            if (u.getDistance(move) <= UnitType.Terran_Marine.groundWeapon().maxRange())
                                ((MobileUnit) u).stop(false);
                        } else if (lastTarget == null || (lastTarget != null && !lastTarget.equals(move))) {
                            ((MobileUnit) u).move(move);
                            continue;
                        }
                    }
                }
                boolean retreat = getGs().sim.getSimulation(u, SimInfo.SimType.MIX).lose;
                String retreating = ColorUtil.formatText(retreat ? "Retreating" : "Fighting", ColorUtil.White);
                getGs().getGame().getMapDrawer().drawTextMap(u.getPosition().add(new Position(0, u.getInitialType().tileHeight())), retreating);
                if (retreat && status != Status.DEFENSE) {
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
                    if (dist >= 300) {
                        if (u.getOrderTargetPosition() != null) {
                            if (!u.getOrderTargetPosition().equals(sCenter)) {
                                ((MobileUnit) u).attack(sCenter);
                                continue;
                            }
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

                if (lastTarget != null) {
                    if (lastTarget.equals(attack)) continue;
                }
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
                        if (!e.getInitialType().isFlyer() && e.getInitialType().groundWeapon().maxRange() <= 32
                                && e.getInitialType() != UnitType.Terran_Medic) {
                            if (getGs().broodWarDistance(u.getPosition(), e.getPosition()) <=
                                    u.getInitialType().groundWeapon().maxRange()) {
                                enemyToKite.add(e);
                            }
                        }
                    }
                    for (EnemyBuilding b : getGs().enemyBuildingMemory.values()) {
                        if (b.type.canAttack() || b.type == UnitType.Terran_Bunker) {
                            enemyToAttack.add(b.unit);
                        }
                    }
                    if (u instanceof GroundAttacker && ((GroundAttacker) u).getGroundWeaponCooldown() > 0) {
                        if (!enemyToKite.isEmpty()) {
                            Position run = getGs().kiteAway(u, enemyToKite);
                            if (getGs().getGame().getBWMap().isValidPosition(run)) {
                                ((MobileUnit) u).move(run);
                                continue;
                            } else {
                                ((MobileUnit) u).move(getGs().getPlayer().getStartLocation().toPosition());
                                continue;
                            }
                        }
                    } else if (attack != null && !u.isStartingAttack() && !u.isAttacking()) {
                        if (getGs().strat.name == "ProxyBBS") {
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
                        if (u.getOrder() == Order.Move) {
                            ((MobileUnit) u).attack(attack);
                            continue;
                        }
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
