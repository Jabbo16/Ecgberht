package ecgberht;
import java.util.Map;
import org.openbw.bwapi4j.unit.Unit;
public class OnUnitShow extends OnUnitAction{
	public OnUnitShow(Unit unit, UnitStorage storage) {
		super(unit, storage);
	}
	@Override
	void action() {
		  UnitInfo stored = enemy.get(unit);
	      final boolean unitNotMatched = stored.unitType != unit.getType();
		final boolean isStored = stored != null;
		if (isStored && unitNotMatched) 
	    	  enemy.remove(unit);
	      pushCreatedEnemyUnit(unit);
	}
}
