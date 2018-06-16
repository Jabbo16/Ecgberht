package ecgberht.Simulation;

import ecgberht.Clustering.Cluster;
import jfap.JFAPUnit;
import jfap.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SimInfo {

    public List<Cluster> allies = new ArrayList<>();
    ;
    public List<Cluster> enemies = new ArrayList<>();
    ;
    public Pair<Integer, Integer> preSimScore;
    public Pair<Integer, Integer> postSimScore;
    public Pair<Set<JFAPUnit>, Set<JFAPUnit>> stateBefore = new Pair<>(new TreeSet<>(), new TreeSet<>());
    public Pair<Set<JFAPUnit>, Set<JFAPUnit>> stateAfter = new Pair<>(new TreeSet<>(), new TreeSet<>());
    public boolean win;
}
