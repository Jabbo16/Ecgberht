package ecgberht;

import ecgberht.Clustering.Cluster;
import org.openbw.bwapi4j.Position;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SquadManager {
    public Map<Integer, Squad> squads = new TreeMap<>();

    public void createSquads(List<Cluster> friendly) {
        squads.clear();
        int counter = 0;
        for(Cluster c : friendly){
            Squad s = new Squad(counter, c.units, new Position((int)c.modeX, (int)c.modeY));
            squads.put(counter, s);
            counter++;
        }
    }
}
