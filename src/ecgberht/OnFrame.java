package ecgberht;
import static ecgberht.Ecgberht.getGs;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.SiegeTank;
import org.openbw.bwapi4j.unit.Unit;
public class OnFrame {
	private Map<Unit, UnitInfo> ally = new TreeMap<>();
    private Map<Unit, UnitInfo> enemy = new TreeMap<>();
	public OnFrame(UnitStorage storage) {
		this.ally = storage.getAllyUnits();
		this.enemy = storage.getEnemyUnits();
	}
	void action() {
		 Iterator<Map.Entry<Unit, UnitInfo>> allyIterator = this.ally.entrySet().iterator();
	        while (allyIterator.hasNext()) {
	            Map.Entry<Unit, UnitInfo> ally = allyIterator.next();
	            final boolean allyUnitNotExist = !ally.getValue().unit.exists();
				final boolean nullAllyUnit = ally.getValue().unit == null;
				final boolean allyKeyNotExist = !ally.getKey().exists();
				if (allyKeyNotExist || nullAllyUnit || allyUnitNotExist) {
	                allyIterator.remove();
	                continue;
	            }
	            ally.getValue().update(); 
	        }
	        Iterator<Map.Entry<Unit, UnitInfo>> enemyIterator = this.enemy.entrySet().iterator();
	        while (enemyIterator.hasNext()) {
	            Map.Entry<Unit, UnitInfo> enemy = enemyIterator.next();
	            final boolean enemyKeyExist = enemy.getKey().exists();
				final boolean enemyKeyNotExist = !enemyKeyExist;
				final boolean enemyVisable = getGs().getGame().getBWMap().isVisible(enemy.getValue().lastTileposition);
				final boolean enemyNoneValue = enemy.getValue().unitType != UnitType.None;
				final boolean enemyNotSiegeTank = !(enemy.getKey() instanceof SiegeTank);
				final boolean enemyKeyValueNotMatched = enemy.getKey().getType() != enemy.getValue().unitType;
				if ((enemyKeyNotExist && enemyVisable) || (enemyKeyExist && enemyNoneValue && enemyNotSiegeTank && enemyKeyValueNotMatched)) {
	                enemyIterator.remove();
	                continue;
	            }
	            enemy.getValue().update();
	        }
	        updateAttackers();
	}
	protected void updateAttackers() {
        for (UnitInfo u : this.enemy.values()) {
            if (u.target == null || !u.target.exists()) 
            	continue;
            UnitInfo ally = this.ally.get(u.target);
            if (ally != null) 
            	ally.attackers.add(u);
        }
    }
}
