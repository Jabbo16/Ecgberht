package ecgberht;
import static ecgberht.Ecgberht.getGs;
import java.util.Map;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.Unit;
public class OnUnitDestroy extends OnUnitAction{
	public OnUnitDestroy(Unit unit, UnitStorage storage) {
		super(unit, storage);
	}
	@Override
	void action() {
		  if (unit instanceof PlayerUnit) {
	            Player player = ((PlayerUnit) unit).getPlayer();
	            final boolean playerEqualsGameState = player.equals(getGs().self);
	            
				if (playerEqualsGameState) 
					this.ally.remove(unit);
	            else 
	            	this.enemy.remove(unit);
	        }
	}
}
