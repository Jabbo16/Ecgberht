package ecgberht.Simulation;

import ecgberht.Clustering.Cluster;
import ecgberht.Clustering.MeanShift;
import ecgberht.ConfigManager;
import ecgberht.IntelligenceAgency;
import ecgberht.UnitInfo;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.bk.ass.sim.Agent;
import org.bk.ass.sim.BWAPI4JAgentFactory;
import org.bk.ass.sim.Evaluator;
import org.bk.ass.sim.Simulator;
import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.*;
import org.openbw.bwapi4j.unit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import static ecgberht.Ecgberht.getGs;

public class SimulationTheory {

    public long time;
    private List<Cluster> friendly = new ArrayList<>();
    private List<Cluster> enemies = new ArrayList<>();
    private List<SimInfo> simulations = new ArrayList<>();
    private Simulator simulator;
    private BWAPI4JAgentFactory factory;
    private Evaluator evaluator;
    private int radius = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange();
    private int simFrames = 500;
    private int iterations = 10;

    public SimulationTheory(BW bw) {
        simulator = new Simulator.Builder().build();
        evaluator = new Evaluator();
        factory = new BWAPI4JAgentFactory(bw.getBWMap());
        if (ConfigManager.getConfig().ecgConfig.sscait) {
            simFrames = 300;
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
                if (getGs().getStrategyFromManager() != null && getGs().getStrategyFromManager().proxy && radius == UnitType.Terran_Missile_Turret.airWeapon().maxRange()) {
                    radius -= 32;
                }
                if (IntelligenceAgency.enemyHasType(UnitType.Terran_Siege_Tank_Tank_Mode)) {
                    radius = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange();
                    return;
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
        simulations.clear();
    }

    /**
     * Using allied and enemy units create the clusters with them
     */
    private void createClusters() {
        // Friendly Clusters
        List<UnitInfo> myUnits = new ArrayList<>();
        for (UnitInfo u : getGs().myArmy) {
            if (isArmyUnit(u.unit)) myUnits.add(u);
        }
        getGs().DBs.keySet().stream().map(b -> getGs().unitStorage.getAllyUnits().get(b)).forEach(myUnits::add); // Bunkers
        getGs().agents.values().stream().map(g -> g.unitInfo).forEach(myUnits::add); // Agents
        MeanShift clustering = new MeanShift(myUnits, radius);
        friendly = clustering.run(iterations);
        // Enemy Clusters
        List<UnitInfo> enemyUnits = new ArrayList<>();
        for (UnitInfo u : getGs().unitStorage.getEnemyUnits().values()) {
            if (getGs().getStrategyFromManager().proxy && u.unitType.isWorker() && (Util.isInOurBases(u) && !u.unit.isAttacking()))
                continue;
            if (u.unitType == UnitType.Zerg_Larva || (u.unitType == UnitType.Zerg_Egg && !u.player.isNeutral()))
                continue;
            if (Util.isStaticDefense(u.unitType) || u.burrowed || u.unitType == UnitType.Terran_Siege_Tank_Siege_Mode
                    || getGs().frameCount - u.lastVisibleFrame <= 24 * 4)
                enemyUnits.add(u);
        }
        clustering = new MeanShift(enemyUnits, radius);
        enemies = clustering.run(iterations);
    }

    private boolean isArmyUnit(Unit u) {
        try {
            if (u == null || !u.exists()) return false;
            if (u instanceof SCV && (getGs().getStrategyFromManager().name.equals("ProxyBBS") || getGs().getStrategyFromManager().name.equals("ProxyEightRax")))
                return true;
            if (u instanceof MobileUnit && ((MobileUnit) u).getTransport() != null) return false;
            return u instanceof Marine || u instanceof Medic || u instanceof SiegeTank || u instanceof Firebat
                    || u instanceof Vulture || u instanceof Wraith || u instanceof Goliath || u instanceof Dropship;
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
            //getGs().sqManager.createSquads(friendly);
            createSimInfos();
            //if (!noNeedForSim()) {
            //doSimJFAP();
            doSimASS();
            //}
            getGs().sqManager.createSquads(friendly);
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
        for (UnitInfo u : getGs().unitStorage.getEnemyUnits().values().stream().filter(u -> u.unitType.isBuilding()).collect(Collectors.toSet())) {
            if (u.unit instanceof Attacker && u.visible) return false;
        }
        return true;
    }

    private boolean closeClusters(Cluster c1, Cluster c2) {
        return Util.broodWarDistance(c1.mode(), c2.mode()) <= radius + Math.max(c1.maxDistFromCenter, c2.maxDistFromCenter) * 1.3;
    }

    /**
     * Using the clusters creates different SimInfos based on distance between them
     */
    private void createSimInfos() {
        for (Cluster friend : friendly) {
            if (friend.units.isEmpty()) continue;
            SimInfo aux = new SimInfo(friend);
            for (Cluster enemy : enemies) {
                if (enemy.units.isEmpty()) continue;
                if (closeClusters(friend, enemy)) aux.enemies.addAll(enemy.units);
            }
            aux.allies.addAll(friend.units);
            simulations.add(aux);
        }
        List<SimInfo> newSims = new ArrayList<>();
        for (SimInfo s : simulations) {
            SimInfo air = new SimInfo();
            SimInfo ground = new SimInfo();
            for (UnitInfo u : s.allies) {
                if (u.flying) air.allies.add(u);
                else ground.allies.add(u);
            }
            boolean emptyAir = air.allies.isEmpty();
            boolean emptyGround = ground.allies.isEmpty();
            if (emptyAir && emptyGround) continue;
            for (UnitInfo u : s.enemies) {
                if (u.unit instanceof AirAttacker || u.unitType == UnitType.Terran_Bunker) air.enemies.add(u);
                if (u.unit instanceof GroundAttacker || u.unitType == UnitType.Terran_Bunker || u.unitType == UnitType.Zerg_Creep_Colony)
                    ground.enemies.add(u);
            }
            air.type = SimInfo.SimType.AIR;
            newSims.add(air);
            ground.type = SimInfo.SimType.GROUND;
            newSims.add(ground);
        }
        if (!newSims.isEmpty()) simulations.addAll(newSims);
    }

    private MutablePair<Integer, Integer> scores() {
        ToIntFunction<Agent> score = a -> {
            PlayerUnit unit = (PlayerUnit) a.getUserObject();
            if (unit == null) return 0;
            UnitType unitType = unit.getType();
            int result = (unitType.destroyScore() * (a.getHealth() * 3 + a.getShields() + 1)) / (
                    (unitType.maxHitPoints() * 3) + unitType.maxShields());
            if (unitType == UnitType.Terran_Bunker) result += UnitType.Terran_Marine.destroyScore() * 4;
            return result;
        };
        return new MutablePair<>(simulator.getAgentsA().stream().mapToInt(score).sum(),
                simulator.getAgentsB().stream().mapToInt(score).sum());
    }

    /**
     * Updates the SimInfos created with the results of the ASS simulations
     */
    private void doSimASS() {
        int energy = getGs().CSs.stream().filter(s -> s.getOrder() != Order.CastScannerSweep).mapToInt(s -> s.getEnergy() / 50).sum();
        for (SimInfo s : simulations) {
            try {
                simulator.reset();
                if (s.enemies.isEmpty()) continue;
                for (UnitInfo u : s.allies) {
                    Agent jU = factory.of(u.unit);
                    simulator.addAgentA(jU);
                    s.stateBefore.first.add(jU);
                }
                for (UnitInfo u : s.enemies) {
                    if (u.unitType.isWorker() && u.visible && !u.unit.isAttacking()) continue;
                    if (u.unitType.isBuilding() && !u.completed) continue;
                    if (u.unitType == UnitType.Zerg_Creep_Colony || (!Util.isStaticDefense(u) && !u.unitType.canAttack()))
                        continue;
                    if (!u.unit.isDetected() && (u.unit instanceof DarkTemplar || u.burrowed)) {
                        if (energy >= 1) energy -= 1;
                        else {
                            s.lose = true;
                            break;
                        }
                    }
                    Agent jU = factory.of(u.unit);
                    simulator.addAgentB(jU);
                    s.stateBefore.second.add(jU);
                }
                if (s.lose) continue;
                s.preSimScore = scores();
                double estimate = evaluator.evaluate(s.stateBefore.first, s.stateBefore.second);
                if (estimate < 0.1) {
                    s.lose = true;
                    continue;
                }
                if (estimate > 0.6) continue;
                simulator.simulate(simFrames);
                s.postSimScore = scores();
                s.stateAfter = new MutablePair<>(simulator.getAgentsA(), simulator.getAgentsB());
                int ourLosses = s.preSimScore.first - s.postSimScore.first;
                int enemyLosses = s.preSimScore.second - s.postSimScore.second;
                if (s.stateAfter.first.isEmpty()) {
                    s.lose = true;
                    continue;
                }
                if (enemyLosses > ourLosses * 1.35) continue;
                simulator.simulate(simFrames);
                s.postSimScore = scores();
                s.stateAfter = new MutablePair<>(simulator.getAgentsA(), simulator.getAgentsB());
                //Bad lose sim logic, testing
                if (s.stateAfter.first.isEmpty()) s.lose = true;
                else if (getGs().getStrategyFromManager().name.equals("ProxyBBS")) s.lose = !scoreCalcASS(s, 1.2);
                else if (getGs().getStrategyFromManager().name.equals("ProxyEightRax")) s.lose = !scoreCalcASS(s, 1.35);
                else s.lose = !scoreCalcASS(s, 2);
            } catch (Exception e) {
                System.err.println("Simulator ASS exception");
                e.printStackTrace();
            }

        }
    }

    /**
     * Given a SimInfo and a rate deduces if a battle is won by the bot using ASS
     *
     * @param s    SimInfo simulated
     * @param rate Rate or ratio for comparing enemy and ally units score
     * @return True if the battle simulated is advantageous for the bot
     */
    private boolean scoreCalcASS(SimInfo s, double rate) {
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
        getGs().getGame().getMapDrawer().drawCircleMap(centroid, 4, color, true);
        //getGs().getGame().getMapDrawer().drawTextMap(centroid.add(new Position(0, 5)), ColorUtil.formatText(Integer.toString(id), ColorUtil.White));
        for (UnitInfo u : c.units)
            getGs().getGame().getMapDrawer().drawLineMap(u.lastPosition, centroid, color);
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
    public MutablePair<Boolean, Boolean> simulateDefenseBattle(Set<UnitInfo> friends, Set<Unit> enemies, int frames, boolean bunker) {
        simulator.reset();
        MutablePair<Boolean, Boolean> result = new MutablePair<>(true, false);
        for (UnitInfo u : friends) simulator.addAgentA(factory.of(u.unit));
        for (Unit u : enemies) simulator.addAgentB(factory.of((PlayerUnit) u));
        MutablePair<Integer, Integer> presim_scores = scores();
        simulator.simulate(frames);
        MutablePair<Integer, Integer> postsim_scores = scores();
        int my_score_diff = presim_scores.first - postsim_scores.first;
        int enemy_score_diff = presim_scores.second - postsim_scores.second;
        if (enemy_score_diff * 2 < my_score_diff) result.first = false;
        if (bunker) {
            boolean bunkerDead = true;
            for (Agent unit : simulator.getAgentsA()) {
                if (unit == null || unit.getUserObject() == null) continue;
                if (((Unit) unit.getUserObject()).getType() == UnitType.Terran_Bunker) {
                    bunkerDead = false;
                    break;
                }
            }
            if (bunkerDead) result.second = true;
        }
        return result;
    }

    // TODO improve and search for harasser SimInfo
    public boolean simulateHarass(Unit harasser, Set<UnitInfo> enemies, int frames) {
        simulator.reset();
        simulator.addAgentA(factory.of((PlayerUnit) harasser));
        for (UnitInfo u : enemies) simulator.addAgentB(factory.of(u.unit));
        int preSimFriendlyUnitCount = simulator.getAgentsA().size();
        simulator.simulate(frames);
        int postSimFriendlyUnitCount = simulator.getAgentsA().size();
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
    public SimInfo getSimulation(UnitInfo unit, SimInfo.SimType type) {
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
    public SimInfo getSimulation(UnitInfo unit, boolean enemy) {
        for (SimInfo s : simulations) {
            if (!enemy && s.allies.contains(unit)) return s;
            if (enemy && s.enemies.contains(unit)) return s;
        }
        return new SimInfo();
    }

    /**
     * Returns true if the unit is far from the fight and not getting attacked
     *
     * @param u     The unit to check
     * @param s     The SimInfo the unit should belong
     * @param melee If the UnitInfo u its a melee unit or not
     * @return True if the unit is not getting attacked and far from the fight
     */
    public boolean farFromFight(UnitInfo u, SimInfo s, boolean melee) { // TODO test
        if (u == null || !u.unit.exists()) return true;
        if (s.enemies.isEmpty()) return true;
        for (UnitInfo e : s.enemies) {
            boolean isThreat = Util.canAttack(e, u);
            if (isThreat && u.getDistance(e) <= (!melee ? 32 : 96) + Util.getAttackRange(e, u)) return false;
        }
        return true;
    }

    public SimInfo getSimulation(Cluster c) {
        for (SimInfo s : simulations) {
            if (s.allyCluster.equals(c)) return s;
        }
        return new SimInfo();
    }
}
