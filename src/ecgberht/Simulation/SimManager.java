package ecgberht.Simulation;

import ecgberht.Clustering.Cluster;
import ecgberht.Clustering.MeanShift;
import ecgberht.Squad;
import jfap.JFAP;
import jfap.JFAPUnit;
import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.Color;
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

    public long time;
    private List<Cluster> friendly = new ArrayList<>();
    private List<Cluster> enemies = new ArrayList<>();
    private List<SimInfo> simulations = new ArrayList<>();
    private JFAP simulator;
    private MeanShift clustering;
    private double radius = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange();

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
        for (Unit ag : getGs().agents.keySet()) {
            myUnits.add(ag);
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
        createSimInfos();
        doSim();
        time = System.currentTimeMillis() - time;
    }

    private void createSimInfos() {
        for (Cluster friend : friendly) {
            SimInfo aux = new SimInfo();
            for (Cluster enemy : enemies) {
                if (getGs().broodWarDistance(friend.mode(), enemy.mode()) <= radius) {
                    aux.enemies.add(enemy);
                }
            }
            if (!aux.enemies.isEmpty()) {
                aux.allies.add(friend);
                simulations.add(aux);
            }
        }
    }

    private void doSim() {
        for (SimInfo s : simulations) {
            simulator.clear();
            for (Cluster c : s.allies) {
                for (Unit u : c.units) {
                    JFAPUnit jU = new JFAPUnit(u);
                    simulator.addUnitPlayer1(jU);
                    s.stateBefore.first.add(jU);
                }
            }
            for (Cluster c : s.enemies) {
                for (Unit u : c.units) {
                    JFAPUnit jU = new JFAPUnit(u);
                    simulator.addUnitPlayer2(jU);
                    s.stateBefore.second.add(jU);
                }
            }
            s.preSimScore = simulator.playerScores();
            simulator.simulate(90);
            s.postSimScore = simulator.playerScores();
            s.stateAfter = simulator.getState();
            //Bad win sim logic, testing
            s.win = ((s.preSimScore.second - s.postSimScore.second) * 2 < (s.preSimScore.first - s.postSimScore.first));
        }
    }

    private void drawCluster(Cluster c, boolean ally, int id) {
        Color color = Color.RED;
        if (ally) color = Color.GREEN;
        Position centroid = new Position((int) c.modeX, (int) c.modeY);
        getGs().getGame().getMapDrawer().drawCircleMap(centroid, 5, color, true);
        getGs().getGame().getMapDrawer().drawTextMap(centroid.add(new Position(0, 5)), Integer.toString(id));
        for (Unit u : c.units) getGs().getGame().getMapDrawer().drawLineMap(u.getPosition(), centroid, color);
    }

    public void drawClusters() {
        int cluster = 0;
        for (Cluster c : friendly) {
            drawCluster(c, true, cluster);
            cluster++;
        }
        cluster = 0;
        for (Cluster c : enemies) {
            drawCluster(c, false, cluster);
            cluster++;
        }
    }

    public void drawSimInfos() {
        for (SimInfo s : simulations) {
            for (Cluster ally : s.allies) {
                for (Cluster ally2 : s.allies) {
                    if (ally.equals(ally2)) continue;
                    getGs().getGame().getMapDrawer().drawLineMap(new Position((int) ally.modeX, (int) ally.modeY), new Position((int) ally2.modeX, (int) ally2.modeY), Color.GREEN);
                }
                for (Cluster enemy : s.enemies) {
                    getGs().getGame().getMapDrawer().drawLineMap(new Position((int) enemy.modeX, (int) enemy.modeY), new Position((int) ally.modeX, (int) ally.modeY), Color.RED);
                }
            }
            for (Cluster enemy : s.enemies) {
                for (Cluster enemy2 : s.enemies) {
                    if (enemy.equals(enemy2)) continue;
                    getGs().getGame().getMapDrawer().drawLineMap(new Position((int) enemy.modeX, (int) enemy.modeY), new Position((int) enemy2.modeX, (int) enemy2.modeY), Color.RED);
                }
            }
        }
    }

    public Pair<Boolean, Boolean> simulateDefenseBattle(Set<Unit> friends, Set<Unit> enemies, int frames, boolean bunker) {
        simulator.clear();
        org.openbw.bwapi4j.util.Pair<Boolean, Boolean> result = new org.openbw.bwapi4j.util.Pair<>(true, false);
        for (Unit u : friends) simulator.addUnitPlayer1(new JFAPUnit(u));
        for (Unit u : enemies) simulator.addUnitPlayer2(new JFAPUnit(u));
        jfap.Pair<Integer, Integer> presim_scores = simulator.playerScores();
        simulator.simulate(frames);
        jfap.Pair<Integer, Integer> postsim_scores = simulator.playerScores();
        int my_score_diff = presim_scores.first - postsim_scores.first;
        int enemy_score_diff = presim_scores.second - postsim_scores.second;
        if (enemy_score_diff * 2 < my_score_diff) result.first = false;
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
        return new SimInfo();
    }
}
