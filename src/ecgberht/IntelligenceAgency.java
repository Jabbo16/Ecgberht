package ecgberht;

import bwem.Base;
import bwem.unit.Mineral;
import ecgberht.Strategies.*;
import org.openbw.bwapi4j.Bullet;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

import java.util.*;
import java.util.Map.Entry;

import static ecgberht.Ecgberht.getGs;

public class IntelligenceAgency {

    static Map<Player, TreeSet<EnemyBuilding>> enemyBases;
    static Map<Player, HashSet<UnitType>> enemyTypes;
    private static Player mainEnemy;
    private static Set<Unit> enemyWorkers;
    private static List<Bullet> enemyBullets;
    private static List<Bullet> allyBullets;
    private static EnemyStrats enemyStrat = EnemyStrats.Unknown;
    private static String startStrat = null;
    private static boolean exploredMinerals = false;
    private static Map<UnitType, Integer> mainEnemyUnitTypeAmount;

    private static int getNumEnemyWorkers() {
        return enemyWorkers.size();
    }

    static String getStartStrat() {
        return startStrat;
    }

    static void setStartStrat(String strat) {
        startStrat = strat;
    }

    static void onStartIntelligenceAgency(Player enemy) {
        enemyBases = new HashMap<>();
        enemyTypes = new HashMap<>();
        mainEnemy = enemy;
        enemyWorkers = new TreeSet<>();
        enemyBullets = new ArrayList<>();
        allyBullets = new ArrayList<>();
        enemyStrat = EnemyStrats.Unknown;
        startStrat = null;
        exploredMinerals = false;
        mainEnemyUnitTypeAmount = new HashMap<>();
    }

    public static EnemyStrats getEnemyStrat() {
        return enemyStrat;
    }

    public static void setEnemyStrat(EnemyStrats newEnemyStrat) {
        enemyStrat = newEnemyStrat;
    }

    /**
     * Returns the number of bases or resource depots that a {@link org.openbw.bwapi4j.Player} owns
     *
     * @param player Player to check
     * @return Number of bases
     */
    public static int getNumEnemyBases(Player player) {
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
        return enemyTypes.get(mainEnemy).contains(type);
    }

    public static boolean playerHasType(Player player, UnitType type) {
        Set<UnitType> types = enemyTypes.get(player);
        return types != null && types.contains(type);
    }

    public static boolean enemyHasType(UnitType... types) {
        for (UnitType type : types) {
            if (enemyTypes.values().contains(type)) return true;
        }
        return false;
    }

    public static void printEnemyTypes() {
        for (Entry<Player, HashSet<UnitType>> entry : enemyTypes.entrySet()) {
            for (UnitType type : entry.getValue()) {
                System.out.println(entry.getKey() + ": " + type);
            }
        }
    }

    static void onShow(Unit unit, UnitType type) {
        Integer value = mainEnemyUnitTypeAmount.get(type);
        if (value != null) mainEnemyUnitTypeAmount.put(type, value + 1);
        else mainEnemyUnitTypeAmount.put(type, 0);

        Player player = ((PlayerUnit) unit).getPlayer();
        if (unit instanceof Worker) enemyWorkers.add(unit);
        // Bases
        if (type.isResourceDepot()) {
            // If base and player known skip
            if (enemyBases.containsKey(player) && enemyBases.get(player).contains(new EnemyBuilding(unit))) return;
            for (Base b : getGs().BLs) {
                if (b.getLocation().equals(unit.getTilePosition())) {
                    enemyBases.get(player).add(new EnemyBuilding(unit));
                    break;
                }
            }
        }
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
        Integer value = mainEnemyUnitTypeAmount.get(type);
        if (value != null) mainEnemyUnitTypeAmount.put(type, value - 1);
        else mainEnemyUnitTypeAmount.put(type, 0);

        Player player = ((PlayerUnit) unit).getPlayer();
        if (type.isResourceDepot() && enemyBases.containsKey(player))
            enemyBases.get(player).remove(new EnemyBuilding(unit));
        if (getGs().enemyRace == Race.Zerg && unit instanceof Drone) enemyWorkers.remove(unit);
    }

    /**
     * Detects if the enemy its doing a 4 or 5 Pool strat
     */
    private static boolean detectEarlyPool() {
        if (getGs().frameCount < 24 * 150 && getGs().enemyStartBase != null && !getGs().learningManager.isNaughty() && exploredMinerals) {
            int drones = IntelligenceAgency.getNumEnemyWorkers();
            boolean foundPool = enemyHasType(UnitType.Zerg_Spawning_Pool);
            if (foundPool && drones <= 5) {
                enemyStrat = EnemyStrats.EarlyPool;
                getGs().learningManager.setNaughty(true);
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
                if (getGs().strat.name.equals("BioGreedyFE") || getGs().strat.name.equals("MechGreedyFE") || getGs().strat.name.equals("FullMech")) {
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
     * Detects if the enemy its doing a "Mech Rush" strat
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
                    getGs().strat = new MechGreedyFE();
                    Ecgberht.transition();
                } else if (getGs().strat.name.equals("FullBio") || getGs().strat.name.equals("FullBioFE")) {
                    getGs().strat = new FullMech();
                    Ecgberht.transition();
                }
                return true;
            }
        }
        return false;
    }

    private static void detectEnemyStrategy() {
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
                if (detectProtossFE()) return;
                if (detectCannonRush()) return;
                break;
        }
    }

    private static boolean detectProtossFE() {
        if (getGs().frameCount < 24 * 210 && getGs().enemyStartBase != null) {
            int probes = IntelligenceAgency.getNumEnemyWorkers();
            if (getNumEnemyBases(mainEnemy) > 1 && probes <= 16) {
                enemyStrat = EnemyStrats.ProtossFE;
                getGs().ih.sendText("Nice FE");
                String strat = getGs().strat.name;
                if (strat.equals("FullBio") || strat.equals("FullBioFE")) {
                    getGs().strat = new BioGreedyFE();
                }
                if (strat.equals("BioMech") || strat.equals("BioMechFE")) {
                    getGs().strat = new BioMechGreedyFE();
                }
                if (strat.equals("FullMech")) {
                    getGs().strat = new MechGreedyFE();
                }
                getGs().defendPosition = getGs().naturalChoke.getCenter().toPosition();
                Ecgberht.transition();
                return true;
            }
        }
        return false;
    }

    static void onFrame() {
        if (getGs().enemyStartBase == null) return;
        detectEnemyStrategy();
        updateMaxAmountTypes();
    }

    private static void updateMaxAmountTypes() {
        if (getGs().strat.trainUnits.contains(UnitType.Terran_Goliath)) {
            int goliaths = 0;
            Integer amount;
            switch (getGs().enemyRace) {
                case Zerg:
                    // Mutas
                    Integer spireAmount = mainEnemyUnitTypeAmount.get(UnitType.Zerg_Spire);
                    Integer greaterSpireAmount = mainEnemyUnitTypeAmount.get(UnitType.Zerg_Greater_Spire);
                    if ((spireAmount != null && spireAmount > 0) || (greaterSpireAmount != null && greaterSpireAmount > 0))
                        goliaths += 3;
                    amount = mainEnemyUnitTypeAmount.get(UnitType.Zerg_Mutalisk);
                    goliaths += (amount != null ? (Math.round(amount / 2.0)) : 0);
                    break;
                case Terran:
                    // Wraiths
                    amount = mainEnemyUnitTypeAmount.get(UnitType.Terran_Wraith);
                    goliaths += amount != null ? Math.ceil(amount.doubleValue() / 2.0) : 0;

                    // BattleCruisers
                    amount = mainEnemyUnitTypeAmount.get(UnitType.Terran_Battlecruiser);
                    goliaths += amount != null ? (amount * 3) : 0;
                    break;
                case Protoss:
                    // Scouts!!
                    amount = mainEnemyUnitTypeAmount.get(UnitType.Protoss_Scout);
                    goliaths += amount != null ? amount : 0;

                    // Carriers
                    Integer stargateAmount = mainEnemyUnitTypeAmount.get(UnitType.Protoss_Stargate);
                    if (stargateAmount != null && stargateAmount > 0) goliaths += 3;
                    amount = mainEnemyUnitTypeAmount.get(UnitType.Protoss_Carrier);
                    goliaths += amount != null ? (amount * 3) : 0;
                    break;
            }
            getGs().maxGoliaths = goliaths;
        }
    }

    /**
     * Detects if the enemy its doing a "Cannon Rush" strat
     */
    private static boolean detectCannonRush() {
        if (getGs().frameCount < 24 * 210 && getGs().enemyStartBase != null) {
            boolean foundForge = false;
            boolean foundGas = enemyHasType(UnitType.Protoss_Assimilator);
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
            if ((foundForge && !foundGas) || somethingInMyBase) {
                enemyStrat = EnemyStrats.CannonRush;
                getGs().ih.sendText("Cannon rusher T_T");
                getGs().playSound("rushed.mp3");
                if (getGs().strat.name.equals("BioGreedyFE") || getGs().strat.name.equals("MechGreedyFE")) {
                    getGs().strat = new FullMech();
                    getGs().defendPosition = getGs().mainChoke.getCenter().toPosition();
                    Ecgberht.transition();
                } else if (getGs().strat.name.equals("BioMech") || getGs().strat.name.equals("BioMechFE")) {
                    getGs().strat = new FullMech();
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

    public static boolean enemyIsRushing() {
        boolean timeCheck = getGs().frameCount <= 24 * 500;
        boolean rushStratDetected = enemyStrat == EnemyStrats.ZealotRush || enemyStrat == EnemyStrats.EarlyPool || enemyStrat == EnemyStrats.CannonRush;
        boolean raceCheck = false;
        if (timeCheck && rushStratDetected) return true;
        switch (getGs().enemyRace) {
            case Zerg:
                if (getGs().enemyInBase.stream().filter(u -> u instanceof Zergling).count() >= 4 && getGs().myArmy.size() < 3)
                    raceCheck = true;
                break;
            case Terran:
                if (getGs().enemyInBase.stream().filter(u -> u instanceof SCV).count() >= 3 && getGs().myArmy.size() < 3)
                    raceCheck = true;
                else if (getGs().enemyInBase.stream().filter(u -> u instanceof Marine).count() > getGs().myArmy.size())
                    raceCheck = true;
                break;
            case Protoss:
                if (getGs().enemyInBase.stream().filter(u -> u instanceof Probe).count() >= 3 && getGs().myArmy.size() < 3)
                    raceCheck = true;
                else if (getGs().enemyInBase.stream().filter(u -> u instanceof Zealot).count() >= 3 && getGs().myArmy.size() < 4)
                    raceCheck = true;
                break;
        }
        return timeCheck && raceCheck;
    }

    public enum EnemyStrats {Unknown, EarlyPool, ZealotRush, CannonRush, ProtossFE, MechRush}
}
