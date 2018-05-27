package ecgberht.Agents;

import ecgberht.EnemyBuilding;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;
import org.openbw.bwapi4j.util.Pair;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static ecgberht.Ecgberht.getGs;

public class WraithAgent implements Comparable<WraithAgent> {

    public WraithAgent(Unit unit) {
        this.unit = (Wraith) unit;
    }

    public WraithAgent(Unit unit, String name) {
        this.unit = (Wraith) unit;
        this.name = name;
    }

    enum Status {
        ATTACK, COMBAT, IDLE, RETREAT
    }

    public Wraith unit;
    public String name = "Pepe";
    UnitType type = UnitType.Terran_Wraith;
    boolean cloackResearched = false;
    Position attackPos = null;
    Unit attackUnit = null;
    Status status = Status.IDLE;
    int frameLastOrder = 0;
    int actualFrame = 0;
    Set<Unit> closeEnemies = new TreeSet<>();
    Set<Unit> closeWorkers = new TreeSet<>();

    public String statusToString() {
        if (status == Status.ATTACK) {
            return "Attack";
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
                Position cc = getGs().MainCC.getPosition();
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
                case RETREAT:
                    retreat();
                    break;
                default:
                    break;
            }
            return remove;
        } catch (Exception e) {
            System.err.println("Exception Wraith");
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
            if (getGs().broodWarDistance(u.getPosition(), myPos) <= 600) {
                closeEnemies.add(u);
            }
        }
        for (EnemyBuilding u : getGs().enemyBuildingMemory.values()) {
            if ((u.unit instanceof AirAttacker || u.type == UnitType.Terran_Bunker) && u.unit.isCompleted()) {
                if (getGs().broodWarDistance(myPos, u.pos.toPosition()) <= 600) {
                    closeEnemies.add(u.unit);
                }
            }
        }
        if (closeEnemies.isEmpty()) {
            status = Status.ATTACK;
            return;
        } else {
            int sim = 150;
            if (!getGs().sim.simulateHarass(unit, closeEnemies, sim)) {
                status = Status.RETREAT;
                return;
            }
        }
    }

    private void retreat() {
        Unit CC = getGs().MainCC;
        if (CC != null) {
            unit.move(CC.getPosition());
        } else {
            unit.move(getGs().getPlayer().getStartLocation().toPosition());
        }
        attackPos = null;
        attackUnit = null;
    }

    private void attack() {
        Position newAttackPos = null;
        if (attackPos == null) {
            newAttackPos = selectNewAttack();
            attackPos = newAttackPos;
            if (attackPos == null || !getGs().bw.getBWMap().isValidPosition(attackPos)) {
                attackUnit = null;
                attackPos = null;
                return;
            }
            unit.attack(newAttackPos);
            attackUnit = null;
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
            if (((PlayerUnit) u).isCloaked()) continue;
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
        if (o == this.unit) return true;
        if (!(o instanceof Wraith) || !(o instanceof WraithAgent)) {
            return false;
        }
        WraithAgent wraith = (WraithAgent) o;
        return unit.equals(wraith.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit);
    }

    @Override
    public int compareTo(WraithAgent v1) {
        return this.unit.getId() - v1.unit.getId();
    }
}
