package ecgberht.Simulation;

import ecgberht.Clustering.Cluster;
import ecgberht.Clustering.MeanShift;
import ecgberht.ConfigManager;
import ecgberht.EnemyBuilding;
import ecgberht.IntelligenceAgency;
import ecgberht.Util.ColorUtil;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import jfap.JFAP;
import jfap.JFAPUnit;
import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.*;
import org.openbw.bwapi4j.unit.*;

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
    private double radius = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange();
    private int shortSimFrames = 90;
    private int longSimFrames = 300;
    private int iterations = 10;

    public SimManager(BW bw) {
        simulator = new JFAP(bw);
        if (ConfigManager.getConfig().ecgConfig.sscait) {
            shortSimFrames = 45;
            longSimFrames = 170;
            iterations = 0;
        }
        switch (bw.getInteractionHandler().enemy().getRace()) {
            case Zerg:
                radius = UnitType.Zerg_Sunken_Colony.groundWeapon().maxRange();
                break;
            case Terran:
                radius = UnitType.Terran_Missile_Turret.airWeapon().maxRange();
                break;
            case Protoss:
                radius = UnitType.Protoss_Photon_Cannon.groundWeapon().maxRange();
                break;
            case Unknown:
                radius = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange();
                break;
        }
    }

    private void updateRadius() {
        if (radius == UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange()) return;
        if (Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Tank_Mode) > 0) {
            if (getGs().getPlayer().hasResearched(TechType.Tank_Siege_Mode)) {
                radius = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange();
                return;
            }
        }
        switch (getGs().enemyRace) {
            case Zerg:
                if (getGs().getPlayer().hasResearched(TechType.Irradiate)) {
                    radius = WeaponType.Irradiate.maxRange();
                    return;
                }
                break;
            case Terran:
                if (IntelligenceAgency.enemyHasType(UnitType.Terran_Siege_Tank_Tank_Mode)) {
                    radius = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange();
                }
                break;
            case Protoss:
                if (getGs().getPlayer().hasResearched(TechType.EMP_Shockwave)) {
                    radius = WeaponType.EMP_Shockwave.maxRange();
                    return;
                }
                break;
        }
    }

    /**
     * Clears all the info, clusters, SimInfos and the simulator
     */
    private void reset() {
        friendly.clear();
        enemies.clear();
        simulator.clear();
        simulations.clear();
    }

    /**
     * Using allied and enemy units create the clusters with them
     */
    private void createClusters() {
        // Friendly Clusters
        List<Unit> myUnits = new ArrayList<>();
        for (Unit u : getGs().myArmy) {
            if (isArmyUnit(u)) myUnits.add(u);
        }
        myUnits.addAll(getGs().DBs.keySet()); // Bunkers
        myUnits.addAll(getGs().agents.keySet()); // Agents
        MeanShift clustering = new MeanShift(myUnits, radius);
        friendly = clustering.run(iterations);
        // Enemy Clusters
        List<Unit> enemyUnits = new ArrayList<>();
        for (Unit u : getGs().enemyCombatUnitMemory) {
            if (u instanceof Egg && !Util.isEnemy(((PlayerUnit) u).getPlayer())) continue;
            enemyUnits.add(u);
        }
        for (Unit u : getGs().enemyBuildingMemory.keySet()) {
            if (u instanceof Attacker || u instanceof Bunker) enemyUnits.add(u);
        }
        clustering = new MeanShift(enemyUnits, radius);
        enemies = clustering.run(iterations);
    }

    private boolean isArmyUnit(Unit u) {
        try {
            if (!u.exists()) return false;
            if (u instanceof SCV && (getGs().strat.name.equals("ProxyBBS") || getGs().strat.name.equals("EightRax")))
                return true;
            if (u instanceof MobileUnit && ((MobileUnit) u).getTransport() != null) return false;
            return u instanceof Marine || u instanceof Medic || u instanceof SiegeTank || u instanceof Firebat
                    || u instanceof Vulture || u instanceof Wraith || u instanceof Goliath;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Main method that runs every frame
     * If needed creates the clusters and SimInfos and run the simulations on them
     */
    public void onFrameSim() {
        time = System.currentTimeMillis();
        updateRadius();
        reset();
        createClusters();
        if (!friendly.isEmpty()) {
            getGs().sqManager.createSquads(friendly);
            createSimInfos();
            if (!noNeedForSim()) doSim();
        }
        time = System.currentTimeMillis() - time;
    }

    /**
     * Checks if there is no need to do extra work simulating, useful to save cpu power
     *
     * @return True if there is no need for running {@link #onFrameSim()}, else returns false
     */
    private boolean noNeedForSim() {
        int workerThreats = 0;
        if ((friendly.isEmpty() || enemies.isEmpty()) && getGs().agents.isEmpty()) return true;
        for (Unit u : getGs().enemyCombatUnitMemory) {
            if (u instanceof Attacker && !(u instanceof Worker) || workerThreats > 1) return false;
            if (u instanceof Worker && ((Worker) u).isAttacking()) workerThreats++;
        }
        for (EnemyBuilding u : getGs().enemyBuildingMemory.values()) {
            if (u.unit instanceof Attacker && getGs().getGame().getBWMap().isVisible(u.pos)) return false;
        }
        return true;
    }

    private boolean closeClusters(Cluster c1, Cluster c2) {
        return Util.broodWarDistance(c1.mode(), c2.mode()) <= radius + (Math.max(c1.maxDistFromCenter, c2.maxDistFromCenter));
    }

    /**
     * Using the clusters creates different SimInfos based on distance between them
     */
    private void createSimInfos() {
        for (Cluster friend : friendly) {
            if (friend.units.isEmpty()) continue;
            SimInfo aux = new SimInfo();
            for (Cluster enemy : enemies) {
                if (enemy.units.isEmpty()) continue;
                if (closeClusters(friend, enemy)) aux.enemies.addAll(enemy.units);
            }
            if (!aux.enemies.isEmpty()) {
                aux.allies.addAll(friend.units);
                simulations.add(aux);
            }
        }
        List<SimInfo> newSims = new ArrayList<>();
        for (SimInfo s : simulations) {
            SimInfo air = new SimInfo();
            SimInfo ground = new SimInfo();
            for (Unit u : s.allies) {
                if (u.isFlying()) air.allies.add(u);
                if (u instanceof GroundAttacker) ground.allies.add(u);
            }
            boolean emptyAir = air.allies.isEmpty();
            boolean emptyGround = ground.allies.isEmpty();
            if (emptyAir && emptyGround) continue;
            for (Unit u : s.enemies) {
                if (u instanceof AirAttacker) air.enemies.add(u);
                if (u instanceof GroundAttacker) ground.enemies.add(u);
            }
            if (!emptyAir && !air.enemies.isEmpty()) {
                air.type = SimInfo.SimType.AIR;
                newSims.add(air);
            }
            if (!emptyGround && !ground.enemies.isEmpty()) {
                ground.type = SimInfo.SimType.GROUND;
                newSims.add(ground);
            }
        }
        if (!newSims.isEmpty()) simulations.addAll(newSims);
    }

    /**
     * Updates the SimInfos created with the results of the simulations
     */
    private void doSim() {
        int energy = 0;
        for (ComsatStation s : getGs().CSs) {
            if (s.getOrder() != Order.CastScannerSweep) energy += s.getEnergy() / 50;
        }
        for (SimInfo s : simulations) {
            simulator.clear();
            for (Unit u : s.allies) {
                JFAPUnit jU = new JFAPUnit(u);
                simulator.addUnitPlayer1(jU);
                s.stateBefore.first.add(jU);
            }
            for (Unit u : s.enemies) {
                if (u instanceof Worker && !((Worker) u).isAttacking()) continue;
                if (!u.getType().canAttack()) continue;
                if (!((PlayerUnit) u).isDetected() && (u instanceof DarkTemplar || (u instanceof Lurker && ((Lurker) u).isBurrowed()))) {
                    if (energy >= 1) energy -= 1;
                    else {
                        s.lose = true;
                        break;
                    }
                }
                JFAPUnit jU = new JFAPUnit(u);
                simulator.addUnitPlayer2(jU);
                s.stateBefore.second.add(jU);
            }
            if (s.lose) continue;
            if (getGs().getArmySize(s.allies) >= s.enemies.size() * 5) return;
            s.preSimScore = simulator.playerScores();
            simulator.simulate(shortSimFrames);
            s.postSimScore = simulator.playerScores();
            s.stateAfter = simulator.getState();
            int ourLosses = s.preSimScore.first - s.postSimScore.first;
            int enemyLosses = s.preSimScore.second - s.postSimScore.second;
            if (s.stateAfter.first.isEmpty()) {
                s.lose = true;
                continue;
            }
            if (enemyLosses > ourLosses * 1.5) continue;
            simulator.simulate(longSimFrames);
            s.postSimScore = simulator.playerScores();
            s.stateAfter = simulator.getState();
            //Bad lose sim logic, testing
            if (s.stateAfter.first.isEmpty()) s.lose = true;
            else if (getGs().strat.name.equals("ProxyBBS")) s.lose = !scoreCalc(s, 2);
            else if (getGs().strat.name.equals("EightRax")) s.lose = !scoreCalc(s, 1.5);
            else s.lose = !scoreCalc(s, 2.5);
        }
    }

    /**
     * Given a SimInfo and a rate deduces if a battle is won by the bot
     *
     * @param s    SimInfo simulated
     * @param rate Rate or ratio for comparing enemy and ally units score
     * @return True if the battle simulated is advantageous for the bot
     */
    private boolean scoreCalc(SimInfo s, double rate) {
        return ((s.preSimScore.second - s.postSimScore.second) * rate <= (s.preSimScore.first - s.postSimScore.first));
    }

    /**
     * Draws a cluster on the screen
     *
     * @param c    The cluster to be drawn
     * @param ally If true draws the cluster using green lines otherwise red lines
     * @param id   Cluster identifier
     */
    private void drawCluster(Cluster c, boolean ally, int id) {
        Color color = Color.RED;
        if (ally) color = Color.GREEN;
        Position centroid = new Position((int) c.modeX, (int) c.modeY);
        getGs().getGame().getMapDrawer().drawCircleMap(centroid, 5, color, true);
        getGs().getGame().getMapDrawer().drawTextMap(centroid.add(new Position(0, 5)), ColorUtil.formatText(Integer.toString(id), ColorUtil.White));
        for (Unit u : c.units) getGs().getGame().getMapDrawer().drawLineMap(u.getPosition(), centroid, color);
    }

    /**
     * Draws the clusters created on the screen
     */
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

    // TODO improve and search for bunkers SimInfos
    public MutablePair<Boolean, Boolean> simulateDefenseBattle(Set<Unit> friends, Set<Unit> enemies, int frames, boolean bunker) {
        simulator.clear();
        MutablePair<Boolean, Boolean> result = new MutablePair<>(true, false);
        for (Unit u : friends) simulator.addUnitPlayer1(new JFAPUnit(u));
        for (Unit u : enemies) simulator.addUnitPlayer2(new JFAPUnit(u));
        jfap.MutablePair<Integer, Integer> presim_scores = simulator.playerScores();
        simulator.simulate(frames);
        jfap.MutablePair<Integer, Integer> postsim_scores = simulator.playerScores();
        int my_score_diff = presim_scores.first - postsim_scores.first;
        int enemy_score_diff = presim_scores.second - postsim_scores.second;
        if (enemy_score_diff * 2 < my_score_diff) result.first = false;
        if (bunker) {
            boolean bunkerDead = true;
            for (JFAPUnit unit : simulator.getState().first) {
                if (unit.unit == null) continue;
                if (unit.unit.getType() == UnitType.Terran_Bunker) {
                    bunkerDead = false;
                    break;
                }
            }
            if (bunkerDead) result.second = true;
        }

        return result;
    }

    // TODO improve and search for harasser SimInfo
    public boolean simulateHarass(Unit harasser, Collection<Unit> enemies, int frames) {
        simulator.clear();
        simulator.addUnitPlayer1(new JFAPUnit(harasser));
        for (Unit u : enemies) simulator.addUnitPlayer2(new JFAPUnit(u));
        int preSimFriendlyUnitCount = simulator.getState().first.size();
        simulator.simulate(frames);
        int postSimFriendlyUnitCount = simulator.getState().first.size();
        int myLosses = preSimFriendlyUnitCount - postSimFriendlyUnitCount;
        return myLosses <= 0;
    }

    /**
     * Returns the SimInfo that an allied Unit belongs to
     *
     * @param unit The unit to check
     * @param type Type of simulation to search, ground, air or mix
     * @return SimInfo that contains the unit given by parameter and matches SimType
     */
    public SimInfo getSimulation(Unit unit, SimInfo.SimType type) {
        for (SimInfo s : simulations) {
            if (s.type == type && s.allies.contains(unit)) return s;
        }
        return new SimInfo();
    }

    /**
     * Returns the SimInfo that an allied Unit belongs to
     *
     * @param unit  The unit to check
     * @param enemy Where to look at
     * @return First SimInfo found that contains the unit given by parameter
     */
    public SimInfo getSimulation(Unit unit, boolean enemy) {
        for (SimInfo s : simulations) {
            if (!enemy && s.allies.contains(unit)) return s;
            if (enemy && s.enemies.contains(unit)) return s;
        }
        return new SimInfo();
    }

    /**
     * Returns true if the unit is far from the fight and not getting attacked
     *
     * @param u The unit to check
     * @param s The SimInfo the unit should belong
     * @return True if the unit is not getting attacked and far from the fight
     */
    public boolean farFromFight(Unit u, SimInfo s) { // TODO test
        if (!s.allies.contains(u)) return true;
        WeaponType weapon = Util.getWeapon(u.getType());
        int range = weapon == WeaponType.None ? UnitType.Terran_Marine.groundWeapon().maxRange() : (weapon.maxRange() > 32 ? weapon.maxRange() : UnitType.Terran_Marine.groundWeapon().maxRange());
        return !((PlayerUnit) u).isUnderAttack() && u.getDistance(Util.getClosestUnit(u, s.enemies)) > range * 1.5;
    }
}
