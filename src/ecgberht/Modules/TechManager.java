package ecgberht.Modules;

import ecgberht.UnitInfo;
import org.openbw.bwapi4j.unit.Barracks;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.Bunker;
import org.openbw.bwapi4j.unit.ComsatStation;
import org.openbw.bwapi4j.unit.ExtendibleByAddon;
import org.openbw.bwapi4j.unit.Factory;
import org.openbw.bwapi4j.unit.MissileTurret;
import org.openbw.bwapi4j.unit.ResearchingFacility;
import org.openbw.bwapi4j.unit.Starport;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class TechManager {
    public Set<Barracks> MBs = new TreeSet<>();
    public Set<Factory> Fs = new TreeSet<>();
    public Set<ResearchingFacility> UBs = new TreeSet<>();
    public Set<Starport> Ps = new TreeSet<>();
    public Set<ComsatStation> CSs = new TreeSet<>();
    public Set<MissileTurret> Ts = new TreeSet<>();
    public Map<Bunker, Set<UnitInfo>> DBs = new TreeMap<>();
    
    public Set<Building> buildingLot = new TreeSet<>();
    public Building chosenBuildingLot = null;
    public ExtendibleByAddon chosenBuildingAddon = null;
    public ResearchingFacility chosenUnitUpgrader = null;
    
    public int builtBuildings;
    public int builtRefinery;
}




