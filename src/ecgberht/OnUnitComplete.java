package ecgberht;
import java.util.Map;
import org.openbw.bwapi4j.unit.Unit;
public class OnUnitComplete extends OnUnitAction{
	public OnUnitComplete(Unit unit, UnitStorage storage) {
		super(unit, storage);
	}
	@Override
	void action() {
		if (this.ally.containsKey(unit)) return;
        pushCreatedAllyUnit(unit);
	}
}
