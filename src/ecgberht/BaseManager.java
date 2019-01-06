package ecgberht;

import bwem.BWEM;
import bwem.Base;
import bwem.area.Area;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.unit.ResourceDepot;
import org.openbw.bwapi4j.unit.Unit;

import java.util.*;
import java.util.stream.Collectors;

import static ecgberht.Ecgberht.getGs;

public class BaseManager {


    private Set<Garrison> garrisons = new HashSet<>();

    public BaseManager(BWEM bwem) {
        bwem.getMap().getBases().forEach(b -> garrisons.add(new Garrison(b)));
        updateGarrisons();
    }

    private void updateGarrisons() {

    }

    public List<Garrison> getMyBases() {
        return garrisons.stream().filter(u -> u.player.equals(getGs().self)).collect(Collectors.toList());
    }

    public List<Garrison> getEnemyBases() {
        return garrisons.stream().filter(u -> u.player.equals(getGs().getIH().enemy())).collect(Collectors.toList());
    }

    public class Garrison {
        Base base;
        TilePosition tile;
        Set<Unit> minerals = new TreeSet<>();
        Map<Unit, Boolean> geysers = new TreeMap<>();
        Area area;
        Player player;
        boolean starting;
        boolean island;
        int lastFrameVisible = 0;
        ResourceDepot depot = null;

        public Garrison(Base bwemBase) {
            base = bwemBase;
            tile = base.getLocation();
            base.getMinerals().forEach(t -> minerals.add(t.getUnit()));
            base.getGeysers().forEach(t -> geysers.put(t.getUnit(), false));
            area = base.getArea();
            starting = base.isStartingLocation();
            island = area.getAccessibleNeighbors().isEmpty();
            if (getGs().self.getStartLocation().equals(tile)) {
                player = getGs().self;
                depot = getGs().CCs.values().iterator().next();
            }
        }
    }
}
