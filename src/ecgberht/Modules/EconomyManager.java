package ecgberht.Modules;

import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.GasMiningFacility;
import org.openbw.bwapi4j.unit.Mechanical;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.Worker;
import ecgberht.Util.MutablePair;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class EconomyManager {
    public Map<GasMiningFacility, Integer> refineriesAssigned = new TreeMap<>();
    public Map<MineralPatch, Integer> mineralsAssigned = new TreeMap<>();
    public Map<Position, MineralPatch> blockingMinerals = new LinkedHashMap<>();
    
    public Map<SCV, Mechanical> repairerTask = new TreeMap<>();
    public Map<SCV, Building> workerTask = new TreeMap<>();
    public Map<SCV, MutablePair<UnitType, TilePosition>> workerBuild = new HashMap<>();
    public Map<Worker, GasMiningFacility> workerGas = new TreeMap<>();
    public Map<Worker, MineralPatch> workerMining = new TreeMap<>();
    public Map<Worker, Position> workerDefenders = new TreeMap<>();
    
    public int mining;
    public int workerCountToSustain = 0;
    public MutablePair<Integer, Integer> deltaCash = new MutablePair<>(0, 0);
}




