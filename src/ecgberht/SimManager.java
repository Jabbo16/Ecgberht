package ecgberht;

import ecgberht.Agents.VultureAgent;
import ecgberht.Agents.WraithAgent;
import ecgberht.Clustering.Cluster;

import java.util.*;

import ecgberht.Clustering.MeanShift;
import org.openbw.bwapi4j.util.Pair;
import jfap.*;
import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Unit;

import static ecgberht.Ecgberht.getGs;

public class SimManager {

    private List<Cluster> friendly = new ArrayList<>();
    private List<Cluster> enemies = new ArrayList<>();
    private JFAP simulator;
    private MeanShift clustering;

    public SimManager(BW bw){
        simulator = new JFAP(bw);
    }

    private void resetClusters(){
        friendly.clear();
        enemies.clear();
    }

    private void createClusters(){
        // Friendly Clusters
        List<Unit> myUnits = new ArrayList<>();
        for(Squad s : getGs().squads.values()){
            myUnits.addAll(s.members);
        }
        for(VultureAgent v : getGs().agents){
            myUnits.add(v.unit);
        }
        for(WraithAgent w : getGs().spectres.values()){
            myUnits.add(w.unit);
        }
        clustering = new MeanShift(myUnits);
        friendly = clustering.run();

        // Enemy Clusters
        clustering = new MeanShift(getGs().enemyCombatUnitMemory);
        enemies = clustering.run();
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
}
