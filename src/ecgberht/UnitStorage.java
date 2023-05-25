package ecgberht;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.SiegeTank;
import org.openbw.bwapi4j.unit.Unit;
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

}