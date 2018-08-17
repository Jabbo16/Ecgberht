package ecgberht;

import bwem.unit.Mineral;
import ecgberht.Strategies.BioMechFE;
import ecgberht.Strategies.FullBio;
import ecgberht.Strategies.FullBioFE;
import ecgberht.Util.Util;
import org.openbw.bwapi4j.Bullet;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Drone;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.unit.Worker;

import java.util.*;
import java.util.Map.Entry;

import static ecgberht.Ecgberht.getGs;

public class IntelligenceAgency {

    private static Map<String, TreeSet<Unit>> enemyBases = new TreeMap<>();
    private static Map<String, HashSet<UnitType>> enemyTypes = new TreeMap<>();
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

    public static boolean enemyHasType(String player, UnitType type) {
        if (enemyTypes.containsKey(player)) {
            if (enemyTypes.get(player).contains(type)) return true;
        }
        return false;
    }

    public static boolean enemyHasType(UnitType type) {
        if (enemyTypes.values().contains(type)) return true;
        return false;
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
        if (type.isResourceDepot()) {
            if (!enemyBases.containsKey(player)) {
                TreeSet<Unit> aux = new TreeSet<>();
                aux.add(unit);
                enemyBases.put(player, aux);
            } else enemyBases.get(player).add(unit);
        }
        // If player and type known skip
        if (enemyTypes.containsKey(player) && enemyTypes.get(player).contains(type)) return;
        // Normal units (no enemyWorkers and no real combat or support units)
        if (!type.isBuilding() && !type.isWorker() && (type.canAttack() || type.isSpellcaster() ||
                (type.spaceProvided() > 0 && type.supplyProvided() == 0))) {
            if (!enemyTypes.containsKey(player)) {
                HashSet<UnitType> aux = new HashSet<>();
                aux.add(type);
                enemyTypes.put(player, aux);
            } else enemyTypes.get(player).add(type);
        }
        // Eggs
        else if (type == UnitType.Zerg_Lurker_Egg) {
            if (!enemyTypes.containsKey(player)) {
                HashSet<UnitType> aux = new HashSet<>();
                aux.add(UnitType.Zerg_Lurker);
                enemyTypes.put(player, aux);
            } else enemyTypes.get(player).add(UnitType.Zerg_Lurker);
        }
        // Buildings
        else if (type.isBuilding()) {
            // Protoss tech
            if (type == UnitType.Protoss_Arbiter_Tribunal) {
                if (!enemyTypes.containsKey(player)) {
                    HashSet<UnitType> aux = new HashSet<>();
                    aux.add(UnitType.Protoss_Arbiter);
                    enemyTypes.put(player, aux);
                } else enemyTypes.get(player).add(UnitType.Protoss_Arbiter);
            } else if (type == UnitType.Protoss_Templar_Archives) {
                if (!enemyTypes.containsKey(player)) {
                    HashSet<UnitType> aux = new HashSet<>();
                    aux.add(UnitType.Protoss_Dark_Templar);
                    aux.add(UnitType.Protoss_High_Templar);
                    enemyTypes.put(player, aux);
                } else {
                    enemyTypes.get(player).add(UnitType.Protoss_Dark_Templar);
                    enemyTypes.get(player).add(UnitType.Protoss_High_Templar);
                }
            } else if (type == UnitType.Protoss_Fleet_Beacon) {
                if (!enemyTypes.containsKey(player)) {
                    HashSet<UnitType> aux = new HashSet<>();
                    aux.add(UnitType.Protoss_Carrier);
                    enemyTypes.put(player, aux);
                } else enemyTypes.get(player).add(UnitType.Protoss_Carrier);
            } else if (type == UnitType.Protoss_Robotics_Support_Bay) {
                if (!enemyTypes.containsKey(player)) {
                    HashSet<UnitType> aux = new HashSet<>();
                    aux.add(UnitType.Protoss_Reaver);
                    enemyTypes.put(player, aux);
                } else enemyTypes.get(player).add(UnitType.Protoss_Reaver);
            }
            // Zerg tech
            else if (type == UnitType.Zerg_Spawning_Pool) {
                if (!enemyTypes.containsKey(player)) {
                    HashSet<UnitType> aux = new HashSet<>();
                    aux.add(UnitType.Zerg_Zergling);
                    enemyTypes.put(player, aux);
                } else enemyTypes.get(player).add(UnitType.Zerg_Zergling);
            } else if (type == UnitType.Zerg_Spire) {
                if (!enemyTypes.containsKey(player)) {
                    HashSet<UnitType> aux = new HashSet<>();
                    aux.add(UnitType.Zerg_Mutalisk);
                    enemyTypes.put(player, aux);
                } else enemyTypes.get(player).add(UnitType.Zerg_Mutalisk);
            } else if (type == UnitType.Zerg_Hydralisk_Den) {
                if (!enemyTypes.containsKey(player)) {
                    HashSet<UnitType> aux = new HashSet<>();
                    aux.add(UnitType.Zerg_Hydralisk);
                    enemyTypes.put(player, aux);
                } else enemyTypes.get(player).add(UnitType.Zerg_Hydralisk);
            } else if (type == UnitType.Zerg_Queens_Nest) {
                if (!enemyTypes.containsKey(player)) {
                    HashSet<UnitType> aux = new HashSet<>();
                    aux.add(UnitType.Zerg_Queen);
                    enemyTypes.put(player, aux);
                } else enemyTypes.get(player).add(UnitType.Zerg_Queen);
            } else if (type == UnitType.Zerg_Defiler_Mound) {
                if (!enemyTypes.containsKey(player)) {
                    HashSet<UnitType> aux = new HashSet<>();
                    aux.add(UnitType.Zerg_Defiler);
                    enemyTypes.put(player, aux);
                } else enemyTypes.get(player).add(UnitType.Zerg_Defiler);
            }
        }
    }

    static void onDestroy(Unit unit, UnitType type) {
        String player = ((PlayerUnit) unit).getPlayer().getName();
        if (type.isResourceDepot() && enemyBases.containsKey(player)) {
            if (enemyBases.get(player).contains(unit)) enemyBases.get(player).remove(unit);
        }
        if (getGs().enemyRace == Race.Zerg) {
            if (unit instanceof Drone) {
                if (enemyWorkers.contains(unit)) enemyWorkers.remove(unit);
            }
        }
    }

    /**
     * Detects if the enemy its doing a 4 or 5 Pool strat
     */
    private static boolean detectEarlyPool() {
        if (getGs().frameCount < 24 * 150 && getGs().enemyStartBase != null && !getGs().EI.naughty && exploredMinerals) {
            boolean found_pool = false;
            int drones = IntelligenceAgency.getNumEnemyWorkers();
            for (EnemyBuilding u : getGs().enemyBuildingMemory.values()) {
                if (u.type == UnitType.Zerg_Spawning_Pool) {
                    found_pool = true;
                    break;
                }
            }
            if (found_pool && drones <= 5) {
                enemyStrat = EnemyStrats.EarlyPool;
                getGs().EI.naughty = true;
                getGs().ih.sendText("Bad zerg!, bad!");
                getGs().playSound("rushed.mp3");
                if (getGs().strat.name.equals("BioGreedyFE") || getGs().strat.name.equals("MechGreedyFE")) {
                    startStrat = getGs().strat.name;
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
            boolean foundGas = false;
            for (EnemyBuilding u : getGs().enemyBuildingMemory.values()) {
                if (u.type == UnitType.Protoss_Gateway) countGates++;
                if (u.type == UnitType.Protoss_Assimilator) foundGas = true;
            }
            if (countGates >= 2 && probes <= 12 && !foundGas) {
                enemyStrat = EnemyStrats.ZealotRush;
                getGs().ih.sendText("Nice gates you got there");
                getGs().playSound("rushed.mp3");
                if (getGs().strat.name.equals("BioGreedyFE") || getGs().strat.name.equals("MechGreedyFE")) {
                    startStrat = getGs().strat.name;
                    getGs().strat = new FullBio();
                    getGs().defendPosition = getGs().mainChoke.getCenter().toPosition();
                    Ecgberht.transition();
                } else if (getGs().strat.name.equals("BioMech") || getGs().strat.name.equals("BioMechFE")) {
                    startStrat = getGs().strat.name;
                    getGs().strat = new FullBioFE();
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
                    startStrat = getGs().strat.name;
                    getGs().strat = new BioMechFE();
                    Ecgberht.transition();
                } else if (getGs().strat.name.equals("FullBio") || getGs().strat.name.equals("FullBioFE")) {
                    startStrat = getGs().strat.name;
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
                    startStrat = getGs().strat.name;
                    getGs().strat = new FullBio();
                    getGs().defendPosition = getGs().mainChoke.getCenter().toPosition();
                    Ecgberht.transition();
                } else if (getGs().strat.name.equals("BioMech") || getGs().strat.name.equals("BioMechFE")) {
                    startStrat = getGs().strat.name;
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
