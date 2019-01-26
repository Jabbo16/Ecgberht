package ecgberht;

import bwem.BWEM;
import bwem.Base;
import bwem.area.Area;
import ecgberht.Util.Util;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.ResourceDepot;
import org.openbw.bwapi4j.unit.Unit;

import java.util.*;
import java.util.stream.Collectors;

import static ecgberht.Ecgberht.getGs;

public class BaseManager {

    private Map<Base, Garrison> garrisons = new HashMap<>();

    public BaseManager(BWEM bwem) {
        bwem.getMap().getBases().forEach(b -> garrisons.put(b, new Garrison(b)));
        updateGarrisons();
    }

    public void updateGarrisons() {
        for (Garrison g : garrisons.values()) {
            if (getGs().getGame().getBWMap().isVisible(g.tile)) g.lastFrameVisible = getGs().frameCount;
        }
    }

    public void onCreate(ResourceDepot depot) {
        Base b = Util.getClosestBaseLocation(depot.getPosition());
        Garrison g = garrisons.get(b);
        g.lastFrameVisible = getGs().frameCount;
        g.player = depot.getPlayer();
        g.depot = depot;

    }

    public void onDestroy(ResourceDepot depot) {
        Base b = Util.getClosestBaseLocation(depot.getPosition());
        Garrison g = garrisons.get(b);
        g.lastFrameVisible = getGs().frameCount;
        g.player = null;
        g.depot = null;
    }

    public List<Garrison> getMyBases() {
        return garrisons.values().stream().filter(u -> getGs().self.equals(u.player)).collect(Collectors.toList());
    }

    public List<Garrison> getEnemyBases() {
        return garrisons.values().stream().filter(u -> getGs().getIH().enemy().equals(u.player)).collect(Collectors.toList());
    }

    public List<Garrison> getEnemyBasesSorted() {
        return garrisons.values().stream().filter(u -> getGs().getIH().enemy().equals(u.player))
                .sorted(Comparator.comparing(Garrison::frameVisibleDiff).reversed()).collect(Collectors.toList());
    }

    public List<Garrison> getScoutingBasesSorted() {
        return garrisons.values().stream().filter(u -> u.player == null)
                .sorted(Comparator.comparing(Garrison::frameVisibleDiff).reversed()).collect(Collectors.toList());
    }

    public class Garrison {
        public Base base;
        public TilePosition tile;
        public Set<MineralPatch> minerals = new TreeSet<>();
        public Map<Unit, Boolean> geysers = new TreeMap<>();
        public Area area;
        public Player player;
        public boolean starting;
        public boolean island;
        public int lastFrameVisible = 0;
        public ResourceDepot depot = null;

        Garrison(Base bwemBase) {
            base = bwemBase;
            tile = base.getLocation();
            base.getMinerals().forEach(t -> minerals.add((MineralPatch) t.getUnit()));
            base.getGeysers().forEach(t -> geysers.put(t.getUnit(), false));
            area = base.getArea();
            starting = base.isStartingLocation();
            island = area.getAccessibleNeighbors().isEmpty();
            if (getGs().self.getStartLocation().equals(tile)) player = getGs().self;
            lastFrameVisible = getGs().frameCount;
        }

        int frameVisibleDiff() {
            return getGs().frameCount - lastFrameVisible;
        }
    }
}
