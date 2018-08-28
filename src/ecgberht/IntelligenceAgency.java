package ecgberht;

import bwem.unit.Mineral;
import ecgberht.Strategies.BioMechFE;
import ecgberht.Strategies.FullBio;
import ecgberht.Strategies.FullBioFE;
import org.openbw.bwapi4j.Bullet;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

import java.util.*;
import java.util.Map.Entry;

import static ecgberht.Ecgberht.getGs;

public class IntelligenceAgency {

    static Map<String, TreeSet<Unit>> enemyBases = new TreeMap<>();
    static Map<String, HashSet<UnitType>> enemyTypes = new TreeMap<>();
    private static Player mainEnemy;
    private static Set<Unit> enemyWorkers = new TreeSet<>();
    private static List<Bullet> enemyBullets = new ArrayList<>();
    private static List<Bullet> allyBullets = new ArrayList<>();
    private static EnemyStrats enemyStrat = EnemyStrats.Unknown;
    private static String startStrat = null;
    private static boolean exploredMinerals = false;

    private static int getNumEnemyWorkers() {
        return enemyWorkers.size();
    }

    static String getStartStrat() {
        return startStrat;
    }

    static void setStartStrat(String strat) {
        startStrat = strat;
    }

    public static void onStartIntelligenceAgency(Player enemy){
        enemyBases = new TreeMap<>();
        enemyTypes = new TreeMap<>();
        mainEnemy = enemy;
        enemyWorkers = new TreeSet<>();
        enemyBullets = new ArrayList<>();
        allyBullets = new ArrayList<>();
        enemyStrat = EnemyStrats.Unknown;
        startStrat = null;
        exploredMinerals = false;
    }
    public static EnemyStrats getEnemyStrat() {
        return enemyStrat;
    }

    /**
     * Returns the number of bases or resource depots that a {@link org.openbw.bwapi4j.Player} owns
     *
     * @param player Player to check
     * @return Number of bases
     */
    public static int getNumEnemyBases(String player) {
        if (enemyBases.containsKey(player)) return enemyBases.get(player).size();
        return 0;
    }

    /**
     * Updates visible bullets
     */
    static void updateBullets() {
        enemyBullets.clear();
        allyBullets.clear();
        for (Bullet b : getGs().getGame().getBullets()) {
            if (!b.isExists()) continue;
            if (b.getPlayer() != null && b.getPlayer().isEnemy()) enemyBullets.add(b);
            else allyBullets.add(b);
        }
    }

    private static boolean enemyHasType(UnitType type) {
        return enemyTypes.get(mainEnemy.getName()).contains(type);
    }

    public static boolean playerHasType(Player player, UnitType type) {
        Set<UnitType> types = enemyTypes.get(player.getName());
        return types != null && types.contains(type);
    }

    public static boolean enemyHasType(UnitType... types) {
        for (UnitType type : types) {
            if (enemyTypes.values().contains(type)) return true;
        }
        return false;
    }

    public static void printEnemyTypes() {
        for (Entry<String, HashSet<UnitType>> entry : enemyTypes.entrySet()) {
            for (UnitType type : entry.getValue()) {
                System.out.println(entry.getKey() + ": " + type);
            }
        }
    }

    static void onShow(Unit unit, UnitType type) {
        String player = ((PlayerUnit) unit).getPlayer().getName();
        if (unit instanceof Worker && !enemyWorkers.contains(unit)) enemyWorkers.add(unit);
        // If base and player known skip
        if (enemyBases.containsKey(player) && enemyBases.get(player).contains(unit)) return;
        // Bases
        if (type.isResourceDepot()) enemyBases.get(player).add(unit);
        // If player and type known skip
        if (enemyTypes.containsKey(player) && enemyTypes.get(player).contains(type)) return;
        // Normal units
        if (!(unit instanceof Egg)) enemyTypes.get(player).add(type);
        // Eggs
        if (type == UnitType.Zerg_Lurker_Egg) enemyTypes.get(player).add(UnitType.Zerg_Lurker);
            // Buildings
        else if (type.isBuilding()) {
            // Protoss tech
            if (type == UnitType.Protoss_Arbiter_Tribunal) enemyTypes.get(player).add(UnitType.Protoss_Arbiter);
            else if (type == UnitType.Protoss_Templar_Archives) {
                enemyTypes.get(player).add(UnitType.Protoss_Dark_Templar);
                enemyTypes.get(player).add(UnitType.Protoss_High_Templar);
            } else if (type == UnitType.Protoss_Fleet_Beacon) enemyTypes.get(player).add(UnitType.Protoss_Carrier);
            else if (type == UnitType.Protoss_Robotics_Support_Bay) enemyTypes.get(player).add(UnitType.Protoss_Reaver);
                // Zerg tech
            else if (type == UnitType.Zerg_Spawning_Pool) enemyTypes.get(player).add(UnitType.Zerg_Zergling);
            else if (type == UnitType.Zerg_Spire) enemyTypes.get(player).add(UnitType.Zerg_Mutalisk);
            else if (type == UnitType.Zerg_Hydralisk_Den) enemyTypes.get(player).add(UnitType.Zerg_Hydralisk);
            else if (type == UnitType.Zerg_Queens_Nest) enemyTypes.get(player).add(UnitType.Zerg_Queen);
            else if (type == UnitType.Zerg_Defiler_Mound) enemyTypes.get(player).add(UnitType.Zerg_Defiler);
        }
    }

    static void onDestroy(Unit unit, UnitType type) {
        String player = ((PlayerUnit) unit).getPlayer().getName();
        if (type.isResourceDepot() && enemyBases.containsKey(player) && enemyBases.get(player).contains(unit))
            enemyBases.get(player).remove(unit);
        if (getGs().enemyRace == Race.Zerg && unit instanceof Drone && enemyWorkers.contains(unit)) {
            enemyWorkers.remove(unit);
        }
    }

    /**
     * Detects if the enemy its doing a 4 or 5 Pool strat
     */
    private static boolean detectEarlyPool() {
        if (getGs().frameCount < 24 * 150 && getGs().enemyStartBase != null && !getGs().EI.naughty && exploredMinerals) {
            int drones = IntelligenceAgency.getNumEnemyWorkers();
            boolean foundPool = enemyHasType(UnitType.Zerg_Spawning_Pool);
            if (foundPool && drones <= 5) {
                enemyStrat = EnemyStrats.EarlyPool;
                getGs().EI.naughty = true;
                getGs().ih.sendText("Bad zerg!, bad!");
                getGs().playSound("rushed.mp3");
                if (getGs().strat.name.equals("BioGreedyFE") || getGs().strat.name.equals("MechGreedyFE")) {
                    getGs().strat = new FullBio();
                    getGs().defendPosition = getGs().mainChoke.getCenter().toPosition();
                    Ecgberht.transition();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Detects if the enemy its doing a "Zealot Rush" strat
     */
    private static boolean detectZealotRush() {
        if (getGs().frameCount < 24 * 150 && getGs().enemyStartBase != null && exploredMinerals) {
            int countGates = 0;
            int probes = IntelligenceAgency.getNumEnemyWorkers();
            boolean foundGas = enemyHasType(UnitType.Protoss_Assimilator);
            for (EnemyBuilding u : getGs().enemyBuildingMemory.values()) {
                if (u.type == UnitType.Protoss_Gateway) countGates++;
            }
            if (countGates >= 2 && probes <= 12 && !foundGas) {
                enemyStrat = EnemyStrats.ZealotRush;
                getGs().ih.sendText("Nice gates you got there");
                getGs().playSound("rushed.mp3");
                if (getGs().strat.name.equals("BioGreedyFE") || getGs().strat.name.equals("MechGreedyFE")) {
                    getGs().strat = new FullBio();
                    getGs().defendPosition = getGs().mainChoke.getCenter().toPosition();
                    Ecgberht.transition();

                } else if (getGs().strat.name.equals("BioMech") || getGs().strat.name.equals("BioMechFE")) {
                    getGs().strat = new FullBioFE();
                    getGs().defendPosition = getGs().mainChoke.getCenter().toPosition();
                    Ecgberht.transition();
                }
                getGs().strat.armyForExpand += 5;
                return true;
            }
        }
        return false;
    }

    /**
     * Detects if the enemy its doing a "Zealot Rush" strat
     */
    private static boolean detectMechRush() {
        if (getGs().frameCount < 24 * 210 && getGs().enemyStartBase != null && exploredMinerals) {
            int countFactories = 0;
            int countRax = 0;
            boolean foundGas = false;
            for (EnemyBuilding u : getGs().enemyBuildingMemory.values()) {
                if (u.type == UnitType.Terran_Factory) countFactories++;
                if (u.type == UnitType.Terran_Refinery) foundGas = true;
                if (u.type == UnitType.Terran_Barracks) countRax++;
            }
            if (countFactories >= 1 && foundGas && countRax == 1) {
                enemyStrat = EnemyStrats.MechRush;
                getGs().ih.sendText("Nice Mech strat you got there");
                getGs().playSound("rushed.mp3");
                if (getGs().strat.name.equals("BioGreedyFE")) {
                    getGs().strat = new BioMechFE();
                    Ecgberht.transition();
                } else if (getGs().strat.name.equals("FullBio") || getGs().strat.name.equals("FullBioFE")) {
                    getGs().strat = new BioMechFE();
                    Ecgberht.transition();
                }
                return true;
            }
        }
        return false;
    }

    static void onFrame() {
        if (getGs().enemyStartBase == null) return;
        if (enemyStrat != EnemyStrats.Unknown) return;
        if (!exploredMinerals) exploredMinerals = checkExploredEnemyMinerals();
        switch (getGs().enemyRace) {
            case Zerg:
                if (detectEarlyPool()) return;
                break;
            case Terran:
                if (detectMechRush()) return;
                break;
            case Protoss:
                if (detectZealotRush()) return;
                if (detectCannonRush()) return;
                break;
        }
    }

    private static boolean detectCannonRush() {
        if (getGs().frameCount < 24 * 210 && getGs().enemyStartBase != null) {
            boolean foundForge = false;
            if (exploredMinerals) {
                for (EnemyBuilding u : getGs().enemyBuildingMemory.values()) {
                    if (u.type == UnitType.Protoss_Forge && getGs().bwem.getMap().getArea(u.pos).equals(getGs().enemyMainArea)) {
                        foundForge = true;
                        break;
                    }
                }
            }
            boolean somethingInMyBase = false;
            for (EnemyBuilding u : getGs().enemyBuildingMemory.values()) {
                if ((u.type == UnitType.Protoss_Pylon || u.type == UnitType.Protoss_Photon_Cannon) &&
                        (getGs().bwem.getMap().getArea(u.pos).equals(getGs().BLs.get(0).getArea())
                                || getGs().bwem.getMap().getArea(u.pos).equals(getGs().BLs.get(1).getArea()))) {
                    somethingInMyBase = true;
                    break;
                }
            }
            if (foundForge || somethingInMyBase) {
                enemyStrat = EnemyStrats.CannonRush;
                getGs().ih.sendText("Cannon rusher T_T");
                getGs().playSound("rushed.mp3");
                if (getGs().strat.name.equals("BioGreedyFE") || getGs().strat.name.equals("MechGreedyFE")) {
                    getGs().strat = new FullBio();
                    getGs().defendPosition = getGs().mainChoke.getCenter().toPosition();
                    Ecgberht.transition();
                } else if (getGs().strat.name.equals("BioMech") || getGs().strat.name.equals("BioMechFE")) {
                    getGs().strat = new FullBioFE();
                    getGs().defendPosition = getGs().mainChoke.getCenter().toPosition();
                    Ecgberht.transition();
                }
                return true;
            }
        }
        return false;
    }

    private static boolean checkExploredEnemyMinerals() {
        for (Mineral m : getGs().enemyStartBase.getMinerals()) {
            if (!getGs().getGame().getBWMap().isExplored(m.getUnit().getTilePosition())) return false;
        }
        return true;
    }

    public enum EnemyStrats {Unknown, EarlyPool, ZealotRush, CannonRush, MechRush}
}
