package ecgberht;

import ecgberht.Util.Util;
import ecgberht.Util.UtilMicro;
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
        position = u.getPosition();
        lastPosition = position;
        tileposition = u.getTilePosition();
        lastTileposition = tileposition;
        unitType = u.getType();
    }

    public UnitInfoDistance toUnitInfoDistance(){
        return new UnitInfoDistance(this);
    }

    void update() {
        UnitInfoUpdate.updatePlayer(this);
        UnitInfoUpdate.updateUnitType(this);
        UnitInfoUpdate.updateVisibility(this);
        UnitInfoUpdate.updatePosition(this);
        UnitInfoUpdate.updateOrder(this);
        UnitInfoUpdate.updateCompletion(this);
        UnitInfoUpdate.updateTilePosition(this);
        UnitInfoUpdate.updateWalkPosition(this);
        UnitInfoUpdate.updateLastPositions(this);
        UnitInfoUpdate.updateFrames(this);
        UnitInfoUpdate.updateRanges(this);
        UnitInfoUpdate.updateHealth(this);
        UnitInfoUpdate.updateShields(this);
        UnitInfoUpdate.updateEnergy(this);
        UnitInfoUpdate.updateBurrowed(this);
        UnitInfoUpdate.updateFlying(this);
        UnitInfoUpdate.updateSpeed(this);
        UnitInfoUpdate.updateTarget(this);
        UnitInfoUpdate.clearAttackers(this);
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


}