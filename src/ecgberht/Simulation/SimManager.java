package ecgberht.Simulation;

import ecgberht.Agents.VultureAgent;
import ecgberht.Agents.WraithAgent;
import ecgberht.Clustering.Cluster;
import ecgberht.Clustering.MeanShift;
import ecgberht.Squad;
import jfap.JFAP;
import jfap.JFAPUnit;
import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Attacker;
import org.openbw.bwapi4j.unit.Bunker;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static ecgberht.Ecgberht.getGs;

public class SimManager {

    private List<Cluster> friendly = new ArrayList<>();
    private List<Cluster> enemies = new ArrayList<>();
    private List<SimInfo> simulations = new ArrayList<>();
    private JFAP simulator;
    private MeanShift clustering;
    public long time;

    public SimManager(BW bw) {
        simulator = new JFAP(bw);
    }

    private void reset() {
        friendly.clear();
        enemies.clear();
        simulator.clear();
        simulations.clear();
    }

    private void createClusters() {
        // Friendly Clusters
        List<Unit> myUnits = new ArrayList<>();
        for (Squad s : getGs().squads.values()) {
            myUnits.addAll(s.members);
        }
        for (VultureAgent v : getGs().agents) {
            myUnits.add(v.unit);
        }
        for (WraithAgent w : getGs().spectres.values()) {
            myUnits.add(w.unit);
        }
        clustering = new MeanShift(myUnits);
        friendly = clustering.run();

        // Enemy Clusters
        List<Unit> enemyUnits = new ArrayList<>(getGs().enemyCombatUnitMemory);
        for (Unit u : getGs().enemyBuildingMemory.keySet()) {
            if (u instanceof Attacker || u instanceof Bunker) enemyUnits.add(u);
        }
        clustering = new MeanShift(enemyUnits);
        enemies = clustering.run();
    }

    public void onFrameSim() {
        time = System.currentTimeMillis();
        reset();
        createClusters();

        time = System.currentTimeMillis() - time;
    }

    private void simClusters() {

    }

    public Pair<Boolean, Boolean> simulateDefenseBattle(Set<Unit> friends, Set<Unit> enemies, int frames, boolean bunker) {
        simulator.clear();
        org.openbw.bwapi4j.util.Pair<Boolean, Boolean> result = new org.openbw.bwapi4j.util.Pair<>(true, false);
        for (Unit u : friends) {
            simulator.addUnitPlayer1(new JFAPUnit(u));
        }
        for (Unit u : enemies) {
            simulator.addUnitPlayer2(new JFAPUnit(u));
        }
        jfap.Pair<Integer, Integer> presim_scores = simulator.playerScores();
        simulator.simulate(frames);
        jfap.Pair<Integer, Integer> postsim_scores = simulator.playerScores();
        int my_score_diff = presim_scores.first - postsim_scores.first;
        int enemy_score_diff = presim_scores.second - postsim_scores.second;

        if (enemy_score_diff * 2 < my_score_diff) {
            result.first = false;
        }
        if (bunker) {
            boolean bunkerDead = true;
            for (JFAPUnit unit : simulator.getState().first) {
                if (unit.unit == null) continue;
                if (unit.unit.getInitialType() == UnitType.Terran_Bunker) {
                    bunkerDead = false;
                    break;
                }
            }
            if (bunkerDead) {
                result.second = true;
            }
        }

        return result;
    }

    public boolean simulateHarass(Unit harasser, Collection<Unit> enemies, int frames) {
        simulator.clear();
        simulator.addUnitPlayer1(new JFAPUnit(harasser));
        for (Unit u : enemies) {
            simulator.addUnitPlayer2(new JFAPUnit(u));
        }
        int preSimFriendlyUnitCount = simulator.getState().first.size();
        simulator.simulate(frames);
        int postSimFriendlyUnitCount = simulator.getState().first.size();
        int myLosses = preSimFriendlyUnitCount - postSimFriendlyUnitCount;
        if (myLosses > 0) {
            return false;
        }
        return true;
    }

    public SimInfo getSimulation(Unit unit) { // TODO think how to deal with multiple cluster sims
        JFAPUnit junit = new JFAPUnit(unit);
        for (SimInfo s : simulations) {
            if (s.stateAfter.first.contains(junit)) {
                return s;
            }
        }
        return null;
    }
}
