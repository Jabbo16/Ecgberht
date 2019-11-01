package ecgberht;

import bwapi.Player;
import bwapi.TilePosition;
import bwem.BWEM;
import bwem.Base;
import bwem.Area;
import bwapi.Unit;
import ecgberht.Util.Util;

import java.util.*;
import java.util.stream.Collectors;

import static ecgberht.Ecgberht.getGs;

public class BaseManager {

    private Map<Base, Garrison> garrisons = new HashMap<>();

    BaseManager(BWEM bwem) {
        bwem.getMap().getBases().forEach(b -> garrisons.put(b, new Garrison(b)));
        updateGarrisons();
    }

    void updateGarrisons() {
        for (Garrison g : garrisons.values()) {
            if (getGs().getGame().isVisible(g.tile)) g.lastFrameVisible = getGs().frameCount;
        }
    }

    void onCreate(Unit depot) {
        Base b = Util.getClosestBaseLocation(depot.getPosition());
        Garrison g = garrisons.get(b);
        g.lastFrameVisible = getGs().frameCount;
        g.player = depot.getPlayer();
        g.depot = depot;

    }

    void onDestroy(Unit depot) {
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
        return garrisons.values().stream().filter(u -> u.player.equals(getGs().bw.enemy())).collect(Collectors.toList());
    }

    public List<Garrison> getEnemyBasesSorted() {
        return garrisons.values().stream().filter(u -> getGs().getGame().enemy().equals(u.player))
                .sorted(Comparator.comparing(Garrison::frameVisibleDiff).reversed()).collect(Collectors.toList());
    }

    public List<Garrison> getScoutingBasesSorted() {
        return garrisons.values().stream().filter(u -> u.player == null)
                .sorted(Comparator.comparing(Garrison::frameVisibleDiff).reversed()).collect(Collectors.toList());
    }

    public class Garrison {
        Base base;
        public TilePosition tile;
        Set<Unit> minerals = new TreeSet<>();
        Map<Unit, Boolean> geysers = new TreeMap<>();
        Area area;
        Player player;
        boolean starting;
        public boolean island;
        int lastFrameVisible = 0;
        Unit depot = null;

        Garrison(Base bwemBase) {
            base = bwemBase;
            tile = base.getLocation();
            base.getMinerals().forEach(t -> minerals.add(t.getUnit()));
            base.getGeysers().forEach(t -> geysers.put(t.getUnit(), false));
            area = base.getArea();
            starting = base.isStartingLocation() || getGs().getGame().getStartLocations().contains(tile);
            island = area.getAccessibleNeighbors().isEmpty();
            if (getGs().self.getStartLocation().equals(tile)) player = getGs().self;
            lastFrameVisible = getGs().frameCount;
        }

        int frameVisibleDiff() {
            return getGs().frameCount - lastFrameVisible;
        }
    }
}
