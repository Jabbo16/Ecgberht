package ecgberht;

import ecgberht.Util.Util;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.WalkPosition;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

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
    public Player player = null;
    public PlayerUnit unit;
    public UnitType unitType = UnitType.None;
    public Position position = null;
    public TilePosition tileposition = null;
    public WalkPosition walkposition = null;
    public Position lastPosition = null;
    public TilePosition lastTileposition = null;
    public WalkPosition lastWalkposition = null;
    public Unit target = null;
    public Order currentOrder;

    public UnitInfo(PlayerUnit u) {
        unit = u;
    }

    // Credits to N00byEdge
    private int expectedHealth() {
        if (unitType.getRace() == Race.Zerg && unitType.regeneratesHP())
            return Math.min(((getGs().frameCount - lastVisibleFrame) * ZERGREGEN) / 256 + health, unitType.maxHitPoints());
        return health;
    }

    // Credits to N00byEdge
    private int expectedShields() {
        if (unitType.getRace() == Race.Protoss)
            return Math.min(((getGs().frameCount - lastVisibleFrame) * PROTOSSSHIELDREGEN) / 256 + shields, unitType.maxShields());
        return shields;
    }

    void update() {
        player = unit.getPlayer();
        unitType = unit.getType();
        visible = unit.isVisible();
        position = visible ? unit.getPosition() : position;
        currentOrder = unit.getOrder();
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
        if (unit instanceof GroundAttacker)
            groundRange = player.getUnitStatCalculator().weaponMaxRange(unitType.groundWeapon());
        if (unit instanceof AirAttacker)
            airRange = player.getUnitStatCalculator().weaponMaxRange(unitType.airWeapon());
        health = visible ? unit.getHitPoints() : expectedHealth();
        shields = visible ? unit.getShields() : expectedShields();
        if (unit instanceof SpellCaster) energy = ((SpellCaster) unit).getEnergy();
        percentHealth = unitType.maxHitPoints() > 0 ? (double) health / (double) unitType.maxHitPoints() : 1.0;
        percentShield = unitType.maxShields() > 0 ? (double) shields / (double) unitType.maxShields() : 1.0;
        if (unit instanceof Burrowable && ((Burrowable) unit).isBurrowed()) burrowed = true;
        if (unit instanceof FlyingBuilding || unitType.isFlyer()) flying = true;

        speed = Util.getSpeed(this);
        target = (unit instanceof Attacker) ? ((Attacker) unit).getTargetUnit() : unit.getOrderTarget();
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
        return Objects.hash(unit);
    }

    @Override
    public int compareTo(UnitInfo o) {
        return this.unit.getId() - o.unit.getId();
    }

    public double getDistance(Position pos) {
        if (this.visible) return this.unit.getDistance(pos);
        return this.lastPosition.getDistance(pos);
    }


    public int getDistance(UnitInfo target) {
        if (this.visible)
            return target.visible ? this.unit.getDistance(target.unit) : (int) unit.getDistance(target.lastPosition);
        return target.visible ? (int) target.getDistance(this.lastPosition) : target.lastPosition.getDistance(this.lastPosition);
    }
}