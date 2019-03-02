package ecgberht;

import ecgberht.Clustering.Cluster;
import ecgberht.Simulation.SimInfo;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.Bunker;
import org.openbw.bwapi4j.unit.Marine;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static ecgberht.Ecgberht.getGs;

public class SquadManager {

    public Map<Integer, Squad> squads = new TreeMap<>();

    public void createSquads(List<Cluster> friendly) {
        squads.clear();
        int counter = 0;
        for (Cluster c : friendly) {
            Squad s = new Squad(counter, new Position((int) c.modeX, (int) c.modeY), getGs().sim.getSimulation(c));
            squads.put(counter, s);
            counter++;
        }
    }

    void updateSquadOrderAndMicro() {
        squads.values().stream().filter(u -> !u.members.isEmpty()).forEach(Squad::updateSquad);
        squads.values().stream().filter(u -> !u.members.isEmpty()).forEach(Squad::runSquad);
    }

    void updateBunkers() { // TODO improve
        for (Map.Entry<Bunker, Set<UnitInfo>> bunker : getGs().DBs.entrySet()) {
            SimInfo bunkerSim = getGs().sim.getSimulation(getGs().unitStorage.getAllyUnits().get(bunker.getKey()), SimInfo.SimType.MIX);
            if (!bunkerSim.enemies.isEmpty()) {
                if (bunker.getValue().size() < 4) {
                    Marine closest = null;
                    double bestDist = Double.MAX_VALUE;
                    for (UnitInfo u : bunkerSim.allies) {
                        if (!(u.unit instanceof Marine)) continue;
                        double dist = u.unit.getDistance(bunker.getKey());
                        if (dist < bestDist) {
                            closest = (Marine) u.unit;
                            bestDist = dist;
                        }
                    }
                    if (closest != null) {
                        UnitInfo closestUI = getGs().unitStorage.getAllyUnits().get(closest);
                        bunker.getValue().add(closestUI);
                        bunkerSim.allies.remove(closestUI);
                        closest.rightClick(bunker.getKey(), false);
                    }
                }
            } else {
                bunker.getKey().unloadAll();
                bunkerSim.allies.addAll(bunker.getValue());
                bunker.getValue().clear();
            }
        }
    }
}