package ecgberht;

import bwapi.Player;
import bwapi.UnitType;

import bwapi.Unit;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import static ecgberht.Ecgberht.getGs;

public class UnitStorage {

    private Map<Unit, UnitInfo> ally = new TreeMap<>();
    private Map<Unit, UnitInfo> enemy = new TreeMap<>();

    public Map<Unit, UnitInfo> getAllyUnits() {
        return this.ally;
    }

    public Map<Unit, UnitInfo> getEnemyUnits() {
        return this.enemy;
    }

    void onFrame() {
        Iterator<Map.Entry<Unit, UnitInfo>> allyIT = this.ally.entrySet().iterator();
        while (allyIT.hasNext()) {
            Map.Entry<Unit, UnitInfo> ally = allyIT.next();
            if (!ally.getKey().exists() || ally.getValue().unit == null || !ally.getValue().unit.exists()) {
                allyIT.remove();
                continue;
            }
            ally.getValue().update();
        }
        Iterator<Map.Entry<Unit, UnitInfo>> enemyIT = this.enemy.entrySet().iterator();
        while (enemyIT.hasNext()) {
            Map.Entry<Unit, UnitInfo> enemy = enemyIT.next();
            if ((!enemy.getKey().exists() && getGs().bw.isVisible(enemy.getValue().lastTileposition))) {
                enemyIT.remove();
                continue;
            }
            enemy.getValue().update();
        }
        updateAttackers();
    }

    void onUnitCreate(Unit unit) {
        if (!unit.getType().isBuilding()) return;
        UnitInfo u = new UnitInfo(unit);
        ally.put(unit, u);
    }

    void onUnitComplete(Unit unit) {
        if (this.ally.containsKey(unit)) return;
        UnitInfo u = new UnitInfo(unit);
        ally.put(unit, u);
    }

    void onUnitShow(Unit unit) {
        UnitInfo stored = enemy.get(unit);
        if (stored != null && stored.unitType != unit.getType()) enemy.remove(unit);
        UnitInfo u = new UnitInfo(unit);
        enemy.put(unit, u);
    }

    void onUnitMorph(Unit unit) {
        UnitInfo u = new UnitInfo(unit);
        enemy.put(unit, u);
    }

    void onUnitDestroy(Unit unit) {
        if (!unit.getPlayer().isNeutral() && !unit.getType().isSpecialBuilding()) {
            Player p = unit.getPlayer();
            if (p.equals(getGs().self)) this.ally.remove(unit);
            else this.enemy.remove(unit);
        }
    }

    private void updateAttackers() {
        for (UnitInfo u : this.enemy.values()) {
            if (u.target == null || !u.target.exists()) continue;
            UnitInfo ally = this.ally.get(u.target);
            if (ally != null) ally.attackers.add(u);
        }
    }
}
