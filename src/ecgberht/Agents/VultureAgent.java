package ecgberht.Agents;

import ecgberht.EnemyBuilding;
import ecgberht.Util;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;
import org.openbw.bwapi4j.util.Pair;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static ecgberht.Ecgberht.getGs;

public class VultureAgent implements Comparable<VultureAgent> {

    public Vulture unit = null;
    UnitType type = UnitType.Terran_Vulture;
    boolean minesResearched = false;
    int mines = 3;
    Position attackPos = null;
    Unit attackUnit = null;
    Status status = Status.IDLE;
    int frameLastOrder = 0;
    int actualFrame = 0;
    Set<Unit> closeEnemies = new HashSet<>();
    Set<Unit> closeWorkers = new HashSet<>();

    public VultureAgent(Unit unit) {
        this.unit = (Vulture) unit;
    }

    public void placeMine(Position pos) {
        unit.spiderMine(pos);
    }

    public String statusToString() {
        if (status == Status.ATTACK) {
            return "Attack";
        }
        if (status == Status.KITE) {
            return "Kite";
        }
        if (status == Status.COMBAT) {
            return "Combat";
        }
        if (status == Status.RETREAT) {
            return "Retreat";
        }
        if (status == Status.IDLE) {
            return "Idle";
        }
        return "None";
    }

    public boolean runAgent() {
        try {
            boolean remove = false;
            if (unit.getHitPoints() <= 15) {
                Position cc = getGs().MainCC.second.getPosition();
                if (cc != null) {
                    unit.move(cc);

                } else {
                    unit.move(getGs().getPlayer().getStartLocation().toPosition());
                }
                getGs().addToSquad(unit);
                return true;
            }
            actualFrame = getGs().getIH().getFrameCount();
            closeEnemies.clear();
            closeWorkers.clear();
            if (frameLastOrder == actualFrame) {
                return remove;
            }
            //if (actualFrame % getGs().getIH().getLatencyFrames() == 0) {
            //return remove;
            //}
            Status old = status;
            getNewStatus();
            if (old == status && status != Status.COMBAT && status != Status.ATTACK) {
                return remove;
            }
            if (status != Status.COMBAT) {
                attackUnit = null;
            }
            if (status == Status.ATTACK && unit.isIdle()) {
                Pair<Integer, Integer> pos = getGs().inMap.getPosition(unit.getTilePosition(), true);
                if (pos != null) {
                    if (pos.first != null && pos.second != null) {
                        Position newPos = new Position(pos.first, pos.second);
                        if (getGs().bw.getBWMap().isValidPosition(newPos)) {
                            unit.attack(newPos);
                            return remove;
                        }
                    }
                }
            }
            switch (status) {
                case ATTACK:
                    attack();
                    break;

                case COMBAT:
                    combat();
                    break;

                case KITE:
                    kite();
                    break;

                case RETREAT:
                    retreat();
                    break;

                default:
                    break;

            }
            return remove;
        } catch (Exception e) {
            System.err.println("Exception Vulture");
            e.printStackTrace();
        }
        return false;
    }

    private void combat() {
        Unit toAttack = getUnitToAttack(unit, closeEnemies);
        if (toAttack != null) {
            if (attackUnit != null) {
                if (attackUnit.equals(toAttack)) {
                    return;
                }
            }
            unit.attack(toAttack);
            attackUnit = toAttack;
        } else {
            if (!closeWorkers.isEmpty()) {
                toAttack = getUnitToAttack(unit, closeWorkers);
                if (toAttack != null) {
                    if (attackUnit != null) {
                        if (attackUnit.equals(toAttack)) {
                            return;
                        } else {
                            unit.attack(toAttack);
                            attackUnit = toAttack;
                            attackPos = null;
                        }
                    }
                }
            }
        }
    }

    private void getNewStatus() {
        Position myPos = unit.getPosition();
        if (getGs().enemyCombatUnitMemory.isEmpty()) {
            status = Status.ATTACK;
            return;
        }
        for (Unit u : getGs().enemyCombatUnitMemory) {
            if (u instanceof Worker && !((PlayerUnit) u).isAttacking()) {
                closeWorkers.add(u);
            }
            if (getGs().broodWarDistance(u.getPosition(), myPos) < 600) {
                closeEnemies.add(u);
            }
        }
        for (EnemyBuilding u : getGs().enemyBuildingMemory.values()) {
            if ((u.type.canAttack() || u.type == UnitType.Terran_Bunker) && u.unit.isCompleted()) {
                if (getGs().broodWarDistance(myPos, u.pos.toPosition()) <= 600) {
                    closeEnemies.add(u.unit);
                }
            }
        }
        if (closeEnemies.isEmpty()) {
            status = Status.ATTACK;
            return;
        } else {
            boolean meleeOnly = checkOnlyMelees();
            int sim = 80;
            if (meleeOnly) {
                sim = 5;
            }
            if (!getGs().sim.simulateHarass(unit, closeEnemies, sim)) {
                status = Status.RETREAT;
                return;
            }
            int cd = unit.getGroundWeaponCooldown();
            if (status == Status.COMBAT || status == Status.ATTACK) {
                if (attackUnit != null) {
                    int weaponRange = attackUnit instanceof GroundAttacker ? ((GroundAttacker) attackUnit).getGroundWeaponMaxRange() : 0;
                    if (weaponRange > type.groundWeapon().maxRange()) {
                        return;
                    }
                }
                if (cd > 0) {
                    status = Status.KITE;
                    return;
                }
            }
            if (status == Status.KITE) {
                if (attackUnit == null) {
                    Unit closest = getUnitToAttack(unit, closeEnemies);
                    if (closest != null) {
                        double dist = getGs().broodWarDistance(unit.getPosition(), closest.getPosition());
                        double speed = type.topSpeed();
                        double timeToEnter = 0.0;
                        if (speed > .00001) {
                            timeToEnter = Math.max(0.0, dist - type.groundWeapon().maxRange()) / speed;
                        }
                        if (timeToEnter >= cd) {
                            status = Status.COMBAT;
                            return;
                        }
                    }
                } else {
                    double dist = getGs().broodWarDistance(unit.getPosition(), attackUnit.getPosition());
                    double speed = type.topSpeed();
                    double timeToEnter = 0.0;
                    if (speed > .00001) {
                        timeToEnter = Math.max(0.0, dist - type.groundWeapon().maxRange()) / speed;
                    }
                    if (timeToEnter >= cd) {
                        status = Status.COMBAT;
                        return;
                    }
                }
                if (cd == 0) {
                    status = Status.COMBAT;
                    return;
                }
            }
        }

    }

    private boolean checkOnlyMelees() {
        for (Unit e : closeEnemies) {
            int weaponRange = e instanceof GroundAttacker ? ((GroundAttacker) e).getGroundWeaponMaxRange() : 0;
            if (weaponRange > 32 || e instanceof Bunker) {
                return false;
            }
        }
        return true;
    }

    private void retreat() {
        Unit CC = getGs().MainCC.second;
        if (CC != null) {
            unit.move(CC.getPosition());
        } else {
            unit.move(getGs().getPlayer().getStartLocation().toPosition());
        }
        attackPos = null;
        attackUnit = null;
    }

    private void kite() {
        Position kite = getGs().kiteAway(unit, closeEnemies);
        unit.move(kite);
        attackPos = null;
    }

    private void attack() {
        Position newAttackPos = selectNewAttack();
        if (attackPos == null) {
            attackPos = newAttackPos;
            if (attackPos == null || !getGs().bw.getBWMap().isValidPosition(attackPos)) {
                attackUnit = null;
                attackPos = null;
                return;
            }
            if (getGs().bw.getBWMap().isValidPosition(attackPos)) {
                unit.attack(newAttackPos);
                attackUnit = null;
            }
            return;
        } else if (attackPos.equals(newAttackPos)) {
            return;
        }

    }

    private Position selectNewAttack() {
        if (getGs().enemyBase != null) {
            return getGs().enemyBase.getLocation().toPosition();
        } else {
            return getGs().EnemyBLs.get(1).getLocation().toPosition();
        }
    }

    private Unit getUnitToAttack(Unit myUnit, Set<Unit> enemies) {
        Unit chosen = null;
        double distB = Double.MAX_VALUE;
        for (Unit u : enemies) {
            if (Util.getType((PlayerUnit) u).isFlyer() || ((PlayerUnit) u).isCloaked()) {
                continue;
            }
            double distA = getGs().broodWarDistance(myUnit.getPosition(), u.getPosition());
            if (chosen == null || distA < distB) {
                chosen = u;
                distB = distA;
            }
        }
        if (chosen != null) {
            return chosen;
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof Vulture) || !(o instanceof VultureAgent)) {
            return false;
        }
        VultureAgent vulture = (VultureAgent) o;
        return unit.equals(vulture.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit);
    }

    @Override
    public int compareTo(VultureAgent v1) {
        return this.unit.getId() - v1.unit.getId();
    }

    enum Status {
        ATTACK, KITE, COMBAT, IDLE, RETREAT
    }
}
