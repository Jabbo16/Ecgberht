package ecgberht.Modules;

import ecgberht.Agents.Agent;
import ecgberht.Agents.DropShipAgent;
import ecgberht.UnitInfo;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.Unit;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class MilitaryManager {
    public Set<UnitInfo> myArmy = new TreeSet<>();
    public Map<Unit, Agent> agents = new TreeMap<>();
    
    public Position attackPosition;
    public Position defendPosition = null;
    
    public DropShipAgent chosenDropShip;
    
    public boolean defense = false;
    
    public int vulturesTrained = 0;
    public int wraithsTrained = 0;
    public int tanksTrained = 0;
}




