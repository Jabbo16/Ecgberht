package ecgberht.Simulation;

import ecgberht.Clustering.Cluster;
import ecgberht.UnitInfo;
import ecgberht.Util.MutablePair;
import org.bk.ass.sim.Agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class SimInfo {

    public SimType type = SimType.MIX;
    public Set<UnitInfo> allies = new TreeSet<>();
    public Set<UnitInfo> enemies = new TreeSet<>();
    public Cluster allyCluster;
    MutablePair<Integer, Integer> preSimScore;
    MutablePair<Integer, Integer> postSimScore;
    MutablePair<Collection<Agent>, Collection<Agent>> stateBefore = new MutablePair<>(new ArrayList<>(), new ArrayList<>());
    MutablePair<Collection<Agent>, Collection<Agent>> stateAfter = new MutablePair<>(new ArrayList<>(), new ArrayList<>());
    public boolean lose = false;

    SimInfo(Cluster friend) {
        this.allyCluster = friend;
    }

    SimInfo() {
    }

    public enum SimType {GROUND, AIR, MIX}
}
