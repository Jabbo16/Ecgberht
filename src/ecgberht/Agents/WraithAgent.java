package ecgberht.Agents;

import ecgberht.EnemyBuilding;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;
import org.openbw.bwapi4j.util.Pair;

import java.util.Objects;

import static ecgberht.Ecgberht.getGs;

public class WraithAgent extends Agent implements Comparable<Wraith> {

    public Wraith unit;
    public String name = "Pepe";

    public WraithAgent(Unit unit) {
        super();
        this.unit = (Wraith) unit;
    }

    public WraithAgent(Unit unit, String name) {
        super();
        this.unit = (Wraith) unit;
        this.name = name;
    }

    @Override
    public boolean runAgent() {
        try {
            boolean remove = false;
            if (unit.getHitPoints() <= 15) {
                Position cc = getGs().MainCC.second.getPosition();
                if (cc != null) unit.move(cc);
                else unit.move(getGs().getPlayer().getStartLocation().toPosition());
                getGs().addToSquad(unit);
                return true;
            }
            actualFrame = getGs().getIH().getFrameCount();
            closeEnemies.clear();
            closeWorkers.clear();
            if (frameLastOrder == actualFrame) return remove;
            Status old = status;
            getNewStatus();
            if (old == status && status != Status.COMBAT && status != Status.ATTACK) return remove;
            if (status != Status.COMBAT) attackUnit = null;
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
            if (u instanceof Worker && !((PlayerUnit) u).isAttacking()) closeWorkers.add(u);
            if (getGs().broodWarDistance(u.getPosition(), myPos) <= 600) closeEnemies.add(u);
        }
        for (EnemyBuilding u : getGs().enemyBuildingMemory.values()) {
            if ((u.unit instanceof AirAttacker || u.type == UnitType.Terran_Bunker) && u.unit.isCompleted()) {
                if (getGs().broodWarDistance(myPos, u.pos.toPosition()) <= 600) closeEnemies.add(u.unit);
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
        Unit CC = getGs().MainCC.second;
        if (CC != null) unit.move(CC.getPosition());
        else unit.move(getGs().getPlayer().getStartLocation().toPosition());
        attackPos = null;
        attackUnit = null;
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
        } else if (attackPos.equals(newAttackPos)) return;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this.unit) return true;
        if (!(o instanceof Wraith) || !(o instanceof WraithAgent)) return false;
        WraithAgent wraith = (WraithAgent) o;
        return unit.equals(wraith.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit);
    }

    @Override
    public int compareTo(Wraith v1) {
        return this.unit.getId() - v1.getId();
    }

}
