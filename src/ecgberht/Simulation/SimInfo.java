package ecgberht.Simulation;

import ecgberht.Clustering.Cluster;
import ecgberht.UnitStorage;
import jfap.JFAPUnit;
import jfap.MutablePair;
import org.bk.ass.Agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class SimInfo {

    public SimType type = SimType.MIX;
    public Set<UnitStorage.UnitInfo> allies = new TreeSet<>();
    public Set<UnitStorage.UnitInfo> enemies = new TreeSet<>();
    public Cluster allyCluster;
    MutablePair<Integer, Integer> preSimScore;
    MutablePair<Integer, Integer> postSimScore;
    ecgberht.Util.MutablePair<Integer, Integer> preSimScoreASS;
    ecgberht.Util.MutablePair<Integer, Integer> postSimScoreASS;
    MutablePair<Set<JFAPUnit>, Set<JFAPUnit>> stateBeforeJFAP = new MutablePair<>(new TreeSet<>(), new TreeSet<>());
    MutablePair<Set<JFAPUnit>, Set<JFAPUnit>> stateAfterJFAP = new MutablePair<>(new TreeSet<>(), new TreeSet<>());
    ecgberht.Util.MutablePair<Collection<Agent>, Collection<Agent>> stateBeforeASS = new ecgberht.Util.MutablePair<>(new ArrayList<>(), new ArrayList<>());
    ecgberht.Util.MutablePair<Collection<Agent>, Collection<Agent>> stateAfterASS = new ecgberht.Util.MutablePair<>(new ArrayList<>(), new ArrayList<>());
    public boolean lose = false;

    SimInfo(Cluster friend) {
        this.allyCluster = friend;
    }

    SimInfo() {
    }

    public enum SimType {GROUND, AIR, MIX}
}
