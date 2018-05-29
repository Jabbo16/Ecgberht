package ecgberht.Simulation;

import ecgberht.Clustering.Cluster;
import jfap.JFAPUnit;
import org.openbw.bwapi4j.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SimInfo {
    public List<Cluster> allies = new ArrayList<>();
    ;
    public List<Cluster> enemies = new ArrayList<>();
    ;
    public Pair<Integer, Integer> preSimScore;
    public Pair<Integer, Integer> postSimScore;
    public Pair<Set<JFAPUnit>, Set<JFAPUnit>> stateBefore;
    public Pair<Set<JFAPUnit>, Set<JFAPUnit>> stateAfter;
}
