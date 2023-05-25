package ecgberht;
import java.util.Map;
import org.openbw.bwapi4j.unit.SiegeTank;
import org.openbw.bwapi4j.unit.Unit;
public class OnUnitMorph extends OnUnitAction{
	public OnUnitMorph(Unit unit, UnitStorage storage) {
		super(unit, storage);
	}
	@Override
	void action() {
		 UnitInfo stored = enemy.get(unit);
	        final boolean isStored = stored != null;
			final boolean unitNotMatched = stored.unitType != unit.getType();
			final boolean enemyNotSiegeTank = !(unit instanceof SiegeTank);
			if (isStored && enemyNotSiegeTank && unitNotMatched) 
				enemy.remove(unit);
	        pushCreatedEnemyUnit(unit);
	}
}
