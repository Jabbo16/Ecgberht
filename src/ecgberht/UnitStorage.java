package ecgberht;

import ecgberht.Util.Util;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.WalkPosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

import java.util.*;

import static ecgberht.Ecgberht.getGs;

public class UnitStorage {

    private Map<Unit, UnitInfo> ally = new TreeMap<>();
    private Map<Unit, UnitInfo> enemy = new TreeMap<>();

    public Map<Unit, UnitInfo> getAllyUnits(){
        return this.ally;
    }

    public Map<Unit, UnitInfo> getEnemyUnits(){
        return this.enemy;
    }

    void onFrame(){
        Iterator<Map.Entry<Unit, UnitInfo>> allyIT = this.ally.entrySet().iterator();
        while(allyIT.hasNext()){
            Map.Entry<Unit, UnitInfo> ally = allyIT.next();
            if(ally.getKey() == null || !ally.getKey().exists()){
                allyIT.remove();
                continue;
            }
            ally.getValue().update();
        }
        Iterator<Map.Entry<Unit, UnitInfo>> enemyIT = this.enemy.entrySet().iterator();
        while(enemyIT.hasNext()){
            Map.Entry<Unit, UnitInfo> enemy = enemyIT.next();
            if(enemy.getKey().exists() && enemy.getKey().getType() != enemy.getValue().unitType){
                enemyIT.remove();
                continue;
            }
            enemy.getValue().update();
        }
        updateAttackers();
    }

    void onUnitCreate(Unit unit){
        if(!unit.getType().isBuilding()) return;
        UnitInfo u = new UnitInfo((PlayerUnit) unit);
        u.update();
        ally.put(unit, u);
    }

    void onUnitComplete(Unit unit){
        UnitInfo u = new UnitInfo((PlayerUnit) unit);
        u.update();
        ally.put(unit, u);
    }

    void onUnitShow(Unit unit){
        UnitInfo stored = enemy.get(unit);
        if(stored != null && stored.unitType != unit.getType()) enemy.remove(unit);
        UnitInfo u = new UnitInfo((PlayerUnit) unit);
        u.update();
        enemy.put(unit, u);
    }

    void onUnitMorph(Unit unit){
        UnitInfo stored = enemy.get(unit);
        if(stored != null && stored.unitType != unit.getType()) enemy.remove(unit);
        UnitInfo u = new UnitInfo((PlayerUnit) unit);
        u.update();
        enemy.put(unit, u);
    }

    void onUnitDestroy(Unit unit){
        if(unit instanceof PlayerUnit){
            Player p = ((PlayerUnit) unit).getPlayer();
            if (p.equals(getGs().self)) this.ally.remove(unit);
            else this.enemy.remove(unit);
        }
    }

    private void updateAttackers(){
        for(UnitInfo u : this.enemy.values()){
            if(u.target == null || !u.target.exists()) continue;
            UnitInfo ally = this.ally.get(u.target);
            if(ally != null) ally.attackers.add(u);
        }
    }

    public class UnitInfo implements Comparable<UnitInfo>{
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
        public Player player = null;
        public PlayerUnit unit;
        public UnitType unitType = UnitType.None;
        public Position position = null;
        public TilePosition tileposition = null;
        public WalkPosition walkposition = null;
        public Position lastPosition = null;
        public Unit target = null;

        public UnitInfo(PlayerUnit u){
            unit = u;
            update();
        }

        void update(){
            player = unit.getPlayer();
            unitType = unit.getType();
            if(unit instanceof GroundAttacker) groundRange = player.getUnitStatCalculator().weaponMaxRange(unitType.groundWeapon());
            if(unit instanceof AirAttacker) airRange = player.getUnitStatCalculator().weaponMaxRange(unitType.airWeapon());
            position = unit.getPosition();
            tileposition = unit.getTilePosition();
            if (!unitType.isBuilding()) walkposition = new Position(unit.getLeft(), unit.getTop()).toWalkPosition();
            else walkposition = tileposition.toWalkPosition();
            lastPosition = unit.getLastKnownPosition();
            health = unit.getHitPoints();
            shields = unit.getShields();
            if(unit instanceof SpellCaster) energy = ((SpellCaster) unit).getEnergy();
            percentHealth = unitType.maxHitPoints() > 0 ? (double)health / (double)unitType.maxHitPoints() : 1.0;
            percentShield = unitType.maxShields() > 0 ? (double)shields / (double)unitType.maxShields() : 1.0;
            if(unit instanceof Burrowable && ((Burrowable) unit).isBurrowed()) burrowed = true;
            if(unit instanceof FlyingBuilding || unitType.isFlyer()) flying = true;
            lastVisibleFrame = unit.isVisible() ? getGs().frameCount : unit.getLastSpotted();
            lastAttackFrame = unit.isStartingAttack() ? getGs().frameCount : unit.getLastSpotted();
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
    }
}
