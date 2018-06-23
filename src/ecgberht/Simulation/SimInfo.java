package ecgberht.Simulation;

import jfap.JFAPUnit;
import jfap.Pair;
import org.openbw.bwapi4j.unit.Unit;

import java.util.Set;
import java.util.TreeSet;

public class SimInfo {

    public SimType type = SimType.MIX;
    public Set<Unit> allies = new TreeSet<>();
    public Set<Unit> enemies = new TreeSet<>();
    public Pair<Integer, Integer> preSimScore;
    public Pair<Integer, Integer> postSimScore;
    public Pair<Set<JFAPUnit>, Set<JFAPUnit>> stateBefore = new Pair<>(new TreeSet<>(), new TreeSet<>());
    public Pair<Set<JFAPUnit>, Set<JFAPUnit>> stateAfter = new Pair<>(new TreeSet<>(), new TreeSet<>());
    public boolean lose = false;
    public enum SimType {GROUND, AIR, MIX}
}
