package ecgberht;

import bwapi.*;
import ecgberht.Util.Util;
import ecgberht.Util.UtilMicro;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static ecgberht.Ecgberht.getGs;

public class UnitInfo implements Comparable<UnitInfo> {

    private static final int PROTOSSSHIELDREGEN = 7;
    private static final int ZERGREGEN = 4;
    public double groundRange = 0.0;
    public double airRange = 0.0;
    public double speed = 0.0;
    public int lastAttackFrame = 0;
    public int lastVisibleFrame = 0;
    public int shields = 0;
    public int health = 0;
    public double percentHealth = 0.0;
    public double percentShield = 0.0;
    public int energy = 0;
    public Set<UnitInfo> attackers = new TreeSet<>();
    public boolean burrowed = false;
    public boolean flying = false;
    public boolean visible = false;
    public boolean completed = false;
    public Player player = null;
    public Unit unit;
    public UnitType unitType = UnitType.None;
    public Position position = null;
    public TilePosition tileposition = null;
    public WalkPosition walkposition = null;
    public Position lastPosition = null;
    public TilePosition lastTileposition = null;
    public WalkPosition lastWalkposition = null;
    public Unit target = null;
    public Order currentOrder;

    public UnitInfo(Unit u) {
        unit = u;
    }

    // Credits to N00byEdge
    private int expectedHealth() {
        if (unitType.getRace() == Race.Zerg && unitType.regeneratesHP())
            return Math.min(((getGs().frameCount - lastVisibleFrame) * ZERGREGEN >> 8) + health, unitType.maxHitPoints());
        return health;
    }

    // Credits to N00byEdge
    private int expectedShields() {
        if (unitType.getRace() == Race.Protoss)
            return Math.min(((getGs().frameCount - lastVisibleFrame) * PROTOSSSHIELDREGEN >> 8) + shields, unitType.maxShields());
        return shields;
    }

    // TODO completion frames
    void update() {
        player = unit.getPlayer();
        unitType = unit.getType();
        visible = unit.isVisible();
        position = visible ? unit.getPosition() : position;
        currentOrder = unit.getOrder();
        completed = !completed && visible ? unit.isCompleted() : completed;
        tileposition = visible ? unit.getTilePosition() : tileposition;
        if (!unitType.isBuilding()) walkposition = new Position(unit.getLeft(), unit.getTop()).toWalkPosition();
        else walkposition = tileposition.toWalkPosition();
        if (visible) {
            lastPosition = position;
            lastTileposition = tileposition;
            lastWalkposition = walkposition;
        }
        lastVisibleFrame = visible ? getGs().frameCount : lastVisibleFrame;
        lastAttackFrame = unit.isStartingAttack() ? getGs().frameCount : lastVisibleFrame;
        if (unitType.groundWeapon() != WeaponType.None) // TODO upgrades
            groundRange = player.weaponMaxRange(unitType.groundWeapon());
        if (unitType.airWeapon() != WeaponType.None) // TODO upgrades
            airRange = player.weaponMaxRange(unitType.airWeapon());
        health = visible ? unit.getHitPoints() : expectedHealth();
        shields = visible ? unit.getShields() : expectedShields();
        energy = unit.getEnergy();
        percentHealth = unitType.maxHitPoints() > 0 ? (double) health / (double) unitType.maxHitPoints() : 1.0;
        percentShield = unitType.maxShields() > 0 ? (double) shields / (double) unitType.maxShields() : 1.0;
        if (visible) burrowed = currentOrder == Order.Burrowing || unit.isBurrowed();
        if (visible) flying = unit.isFlying();
        speed = Util.getSpeed(this);
        target = unit.getTarget() != null ? unit.getTarget() : unit.getOrderTarget();
        attackers.clear();
    }

    public double getDistance(Position pos) {
        if (this.visible) return this.unit.getDistance(pos);
        return this.lastPosition.getDistance(pos);
    }

    public int getDistance(UnitInfo target) {
        if (this.visible)
            return target.visible ? this.unit.getDistance(target.unit) : unit.getDistance(target.lastPosition);
        return target.visible ? (int) target.getDistance(this.lastPosition) : (int) target.lastPosition.getDistance(this.lastPosition);
    }

    public double getPredictedDistance(UnitInfo target) {
        Position nextPosition = UtilMicro.predictUnitPosition(target, 1);
        if(nextPosition == null) return 0;
        return this.unit.getDistance(nextPosition);
    }

    public double getPredictedDistance(UnitInfo target, int frames) {
        Position nextPosition = UtilMicro.predictUnitPosition(target, frames);
        if(nextPosition == null) return 0;
        return this.unit.getDistance(nextPosition);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof UnitInfo)) {
            return false;
        }
        UnitInfo ui2 = (UnitInfo) o;
        return unit.equals(ui2.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit.getID());
    }

    @Override
    public int compareTo(UnitInfo o) {
        return this.unit.getID() - o.unit.getID();
    }

    public boolean isTank() {
        return unitType == UnitType.Terran_Siege_Tank_Siege_Mode || unitType == UnitType.Terran_Siege_Tank_Tank_Mode;
    }

    public boolean isEgg() {
        return unitType == UnitType.Zerg_Egg || unitType == UnitType.Zerg_Lurker_Egg;
    }

    public boolean isAttacker(){
      return isGroundAttacker() || isAirAttacker();
    }

    public boolean isAirAttacker() { // TODO implement
        return true;
    }

    public boolean isGroundAttacker() { // TODO implement
        return true;
    }
}