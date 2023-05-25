package ecgberht;

import bwem.Base;
import bwem.Mineral;
import ecgberht.Strategies.*;
import ecgberht.Util.Util;
import org.openbw.bwapi4j.Bullet;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static ecgberht.Ecgberht.getGs;

public class IntelligenceAgency {

    static Map<Player, TreeSet<UnitInfo>> enemyBases;
    static Map<Player, HashSet<UnitType>> enemyTypes;
    private static Player mainEnemy;
    private static Set<Unit> enemyWorkers;
    private static List<Bullet> enemyBullets;
    private static List<Bullet> allyBullets;
    private static EnemyStrats enemyStrat = EnemyStrats.Unknown;
    private static String startStrat = null;
    private static boolean exploredMinerals = false;
    private static Map<UnitType, Integer> mainEnemyUnitTypeAmount;
    private static boolean cloakedThreats = false;

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
        cloakedThreats = false;
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

    public static boolean enemyHasType(UnitType type) {
        return enemyTypes.values().stream().anyMatch(list -> list.contains(type));
    }

    public static boolean playerHasType(Player player, UnitType type) {
        Set<UnitType> types = enemyTypes.get(player);
        return types != null && types.contains(type);
    }

    public static boolean enemyHasType(UnitType... types) {
        return enemyTypes.values().stream().anyMatch(list -> Arrays.stream(types).anyMatch(list::contains));
    }

    public static boolean mainEnemyHasType(UnitType type) {
        return enemyTypes.get(mainEnemy).contains(type);
    }

    public static boolean mainEnemyHasType(UnitType... types) {
        return Arrays.stream(types).anyMatch(enemyTypes.get(mainEnemy)::contains);
    }

    public static void printEnemyTypes() {
        for (Entry<Player, HashSet<UnitType>> entry : enemyTypes.entrySet()) {
            for (UnitType type : entry.getValue()) {
                System.out.println(entry.getKey() + ": " + type);
            }
        }
    }

    public static int getNumberEnemyType(UnitType type) {
        Integer number = mainEnemyUnitTypeAmount.get(type);
        return number == null ? 0 : number;
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
            if (enemyBases.containsKey(player) && enemyBases.get(player).contains(getGs().unitStorage.getEnemyUnits().get(unit)))
                return;
            for (Base b : getGs().BLs) {
                if (b.getLocation().equals(unit.getTilePosition())) {
                    enemyBases.get(player).add(getGs().unitStorage.getEnemyUnits().get(unit));
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
            enemyBases.get(player).remove(getGs().unitStorage.getEnemyUnits().get(unit));
        if (getGs().enemyRace == Race.Zerg && unit instanceof Drone) enemyWorkers.remove(unit);
    }

    /**
     * Detects if the enemy its doing a 4 or 5 Pool getStrat()
     */
    private static boolean detectEarlyPool() {
        if (getGs().frameCount < 24 * 60 * 2.5 && getGs().enemyStartBase != null && !getGs().learningManager.isNaughty() && exploredMinerals) {
            int drones = IntelligenceAgency.getNumEnemyWorkers();
            boolean foundPool = enemyHasType(UnitType.Zerg_Spawning_Pool);
            if (foundPool && drones <= 6) {
                enemyStrat = EnemyStrats.EarlyPool;
                getGs().learningManager.setNaughty(true);
                Util.sendText("Bad zerg!, bad!");
                getGs().playSound("rushed.mp3");
                String strat = getGs().getStrategyFromManager().name;
                if (strat.contains("GreedyFE") || strat.equals("14CC")) { // TODO cancel 14CC??
                    getGs().setStrategyToManager(new FullBio());
                    getGs().defendPosition = getGs().mainChoke.getCenter().toPosition();
                    Ecgberht.transition();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Detects if the enemy its doing a "Zealot Rush" getStrat()
     */
    private static boolean detectZealotRush() {
        if (getGs().frameCount <= 24 * 60 * 2.7 && getGs().enemyStartBase != null && exploredMinerals) {
            int countGates = (int) getGs().unitStorage.getEnemyUnits().values().stream().filter(u -> u.unitType == UnitType.Protoss_Gateway).count();
            int probes = IntelligenceAgency.getNumEnemyWorkers();
            boolean foundGas = enemyHasType(UnitType.Protoss_Assimilator);
            if (countGates >= 2 && probes <= 13 && !foundGas) {
                enemyStrat = EnemyStrats.ZealotRush;
                Util.sendText("Nice gates you got there");
                getGs().playSound("rushed.mp3");
                String strat = getGs().getStrategyFromManager().name;
                if (strat.contains("GreedyFE") || strat.equals("FullMech") || strat.equals("14CC")) {
                    getGs().setStrategyToManager(new FullBio());
                    getGs().defendPosition = getGs().mainChoke.getCenter().toPosition();
                    Ecgberht.transition();

                } else if (getGs().getStrategyFromManager().name.equals("BioMech") || getGs().getStrategyFromManager().name.equals("BioMechFE")) {
                    getGs().setStrategyToManager(new FullBioFE());
                    getGs().defendPosition = getGs().mainChoke.getCenter().toPosition();
                    Ecgberht.transition();
                }
                getGs().getStrategyFromManager().armyForExpand += 13;
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
            for (UnitInfo u : getGs().unitStorage.getEnemyUnits().values().stream().filter(u -> u.unitType.isBuilding()).collect(Collectors.toSet())) {
                if (u.unitType == UnitType.Terran_Factory) countFactories++;
                if (u.unitType == UnitType.Terran_Refinery) foundGas = true;
                if (u.unitType == UnitType.Terran_Barracks) countRax++;
            }
            if (countFactories >= 1 && foundGas && countRax == 1) {
                enemyStrat = EnemyStrats.MechRush;
                Util.sendText("Nice Mech getStrat() you got there");
                getGs().playSound("rushed.mp3");
                if (getGs().getStrategyFromManager().name.equals("BioGreedyFE")) {
                    getGs().setStrategyToManager(new MechGreedyFE());
                    Ecgberht.transition();
                } else if (getGs().getStrategyFromManager().name.equals("FullBio") || getGs().getStrategyFromManager().name.equals("FullBioFE")) {
                    getGs().setStrategyToManager(new FullMech());
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
                if (detectNinePool()) return;
                if (detectFastHatch()) return;
                break;
            case Terran:
                if (detectMechRush()) return;
                if (detectBioPush()) return;
                break;
            case Protoss:
                if (detectZealotRush()) return;
                if (detectProtossFE()) return;
                if (detectCannonRush()) return;
                break;
        }
    }

    private static boolean detectBioPush() {
        if (getGs().frameCount <= 24 * 60 * 3.5 && getGs().enemyStartBase != null) {
            int countFactories = 0;
            int countRax = 0;
            for (UnitInfo u : getGs().unitStorage.getEnemyUnits().values().stream().filter(u -> u.unitType.isBuilding()).collect(Collectors.toSet())) {
                if (u.unitType == UnitType.Terran_Factory) countFactories++;
                if (u.unitType == UnitType.Terran_Barracks) countRax++;
            }
            if (countFactories < 1 && countRax > 1) {
                enemyStrat = EnemyStrats.BioPush;
                getGs().getStrategyFromManager().bunker = true;
                Util.sendText("Nice Bio strat");
                return true;
            }
        }
        return false;
    }

    private static boolean detectFastHatch() {
        if (getGs().frameCount <= 24 * 60 * 3.5 && getGs().enemyStartBase != null && exploredMinerals) {
            int drones = IntelligenceAgency.getNumEnemyWorkers();
            if (getNumEnemyBases(mainEnemy) == 2 && drones >= 10 && drones <= 12) {
                enemyStrat = EnemyStrats.FastHatch;
                Util.sendText("Nice 12 Hatch");
                if (!getGs().getStrategyFromManager().name.contains("GreedyFE") && !getGs().getStrategyFromManager().proxy && !getGs().getStrategyFromManager().trainUnits.contains(UnitType.Terran_Wraith)) {
                    getGs().iReallyWantToExpand = true;
                    getGs().defendPosition = getGs().naturalChoke.getCenter().toPosition();
                    Ecgberht.transition();
                }
                return true;
            }
        }
        return false;
    }

    private static boolean detectNinePool() {
        if (getGs().frameCount <= 24 * 60 * 3 && getGs().enemyStartBase != null && exploredMinerals) {
            int drones = IntelligenceAgency.getNumEnemyWorkers();
            boolean foundPool = enemyHasType(UnitType.Zerg_Spawning_Pool);
            boolean foundExtractor = enemyHasType(UnitType.Zerg_Extractor);
            if (foundPool && getNumEnemyBases(mainEnemy) < 2 && drones >= 7 && drones <= 10) {
                enemyStrat = EnemyStrats.NinePool;
                Util.sendText(drones == 10 || foundExtractor ? "Nice Overpool" : "Nice 9 pool");
                getGs().getStrategyFromManager().bunker = true;
                String strat = getGs().getStrategyFromManager().name;
                if (strat.equals("14CC")) { // TODO cancel 14CC??
                    getGs().setStrategyToManager(new FullBio());
                    getGs().defendPosition = getGs().mainChoke.getCenter().toPosition();
                    Ecgberht.transition();
                }
                if (strat.contains("GreedyFE")) { // TODO cancel FE??
                    getGs().setStrategyToManager(new FullBio());
                    getGs().defendPosition = getGs().mainChoke.getCenter().toPosition();
                    Ecgberht.transition();
                }
                return true;
            }
        }
        return false;
    }

    private static boolean detectProtossFE() {
        if (getGs().frameCount <= 24 * 210 && getGs().enemyStartBase != null) {
            int probes = IntelligenceAgency.getNumEnemyWorkers();
            if (getNumEnemyBases(mainEnemy) > 1 && probes <= 16) {
                enemyStrat = EnemyStrats.ProtossFE;
                Util.sendText("Nice FE");
                String strat = getGs().getStrategyFromManager().name;
                if (strat.equals("FullBio") || strat.equals("FullBioFE")) getGs().setStrategyToManager(new BioGreedyFE());
                if (strat.equals("BioMech") || strat.equals("BioMechFE")) getGs().setStrategyToManager(new BioMechGreedyFE());
                if (strat.equals("FullMech")) getGs().setStrategyToManager(new MechGreedyFE());
                if (strat.contains("GreedyFE")) getGs().getStrategyFromManager().armyForAttack += 10;
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

    private static int updateGoliaths() {
        int goliaths = 0;
        Integer amount;
        switch (getGs().enemyRace) {
            case Zerg:
                // Mutas
                boolean spire = mainEnemyHasType(UnitType.Zerg_Spire, UnitType.Zerg_Greater_Spire);
                if (spire) goliaths += 3;
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
        return Math.min(20, goliaths);
    }

    private static int updateVessels() {
        Strategy strat = getGs().getStrategyFromManager();
        String stratName = strat.name.toLowerCase();
        if (getGs().getArmySize() <= 12) return 0;
        if (getGs().enemyRace == Race.Zerg) {
            if (stratName.contains("bio")) {
                int mm = (int) getGs().myArmy.stream().filter(u -> u.unitType == UnitType.Terran_Marine || u.unitType == UnitType.Terran_Medic).count();
                if (stratName.contains("full") || stratName.contains("greedy")) return Math.max(3, mm % 14);
                return Math.max(3, mm % 18);
            }
        }
        return getGs().enemyRace == Race.Protoss && enemyHasType(UnitType.Protoss_Arbiter) ? 4 : 2;
    }

    private static int updateFirebats() {
        return (int) Math.min(Math.max(3, Math.exp(((double) getNumberEnemyType(UnitType.Zerg_Zergling) - 3) / 20.0)), 15);
    }

    private static boolean canTrainVessels() {
        boolean tower = false;
        boolean science = false;
        for (ResearchingFacility u : getGs().UBs) {
            if (u instanceof ControlTower) tower = true;
            else if (u instanceof ScienceFacility) science = true;
            if (science && tower) break;
        }
        return science && tower;
    }

    private static void updateMaxAmountTypes() {
        if (getGs().getStrategyFromManager().trainUnits.contains(UnitType.Terran_Goliath)) getGs().maxGoliaths = updateGoliaths();
        if (canTrainVessels()) getGs().maxVessels = updateVessels();
        if (getGs().getStrategyFromManager().trainUnits.contains(UnitType.Terran_Firebat)) getGs().maxBats = updateFirebats();
    }

    /**
     * Detects if the enemy its doing a "Cannon Rush" strat
     */
    private static boolean detectCannonRush() {
        if (getGs().frameCount < 24 * 210 && getGs().enemyStartBase != null) {
            boolean foundForge = false;
            boolean foundGas = enemyHasType(UnitType.Protoss_Assimilator);
            if (exploredMinerals) {
                for (UnitInfo u : getGs().unitStorage.getEnemyUnits().values().stream().filter(u -> u.unitType.isBuilding()).collect(Collectors.toSet())) {
                    if (u.unitType == UnitType.Protoss_Forge && getGs().bwem.getMap().getArea(u.tileposition).equals(getGs().enemyMainArea)) {
                        foundForge = true;
                        break;
                    }
                }
            }
            boolean somethingInMyBase = false;
            for (UnitInfo u : getGs().unitStorage.getEnemyUnits().values().stream().filter(u -> u.unitType.isBuilding()).collect(Collectors.toSet())) {
                if ((u.unitType == UnitType.Protoss_Pylon || u.unitType == UnitType.Protoss_Photon_Cannon) &&
                        (getGs().bwem.getMap().getArea(u.tileposition).equals(getGs().BLs.get(0).getArea())
                                || getGs().bwem.getMap().getArea(u.tileposition).equals(getGs().BLs.get(1).getArea()))) {
                    somethingInMyBase = true;
                    break;
                }
            }
            if ((foundForge && !foundGas) || somethingInMyBase) {
                enemyStrat = EnemyStrats.CannonRush;
                Util.sendText("Cannon rusher T_T");
                getGs().playSound("rushed.mp3");
                getGs().setStrategyToManager(new FullMech());
                getGs().getStrategyFromManager().armyForExpand += 10; // TODO add preconditions for expanding
                getGs().defendPosition = getGs().mainChoke.getCenter().toPosition();
                Ecgberht.transition();
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
        boolean timeCheck = getGs().frameCount <= 24 * 60 * 8;
        boolean rushStratDetected = enemyStrat == EnemyStrats.ZealotRush || enemyStrat == EnemyStrats.EarlyPool
                || enemyStrat == EnemyStrats.CannonRush || enemyStrat == EnemyStrats.NinePool || getGs().learningManager.isNaughty();
        boolean raceCheck = false;
        if (timeCheck && rushStratDetected) return true;
        switch (getGs().enemyRace) {
            case Zerg:
                if (getGs().enemyInBase.stream().filter(u -> u instanceof Zergling).count() >= 4 && getGs().myArmy.size() < 4)
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
                else if (getGs().enemyInBase.stream().filter(u -> u instanceof Zealot).count() >= 3 && getGs().myArmy.size() < 6)
                    raceCheck = true;
                break;
        }
        return timeCheck && raceCheck;
    }

    public static boolean enemyHasAirOrCloakedThreats() {
        if (cloakedThreats) return true;
        switch (getGs().enemyRace) {
            case Zerg:
                cloakedThreats = mainEnemyHasType(UnitType.Zerg_Lurker) || mainEnemyHasType(UnitType.Zerg_Mutalisk);
                break;
            case Terran:
                cloakedThreats = mainEnemyHasType(UnitType.Terran_Wraith);
                break;
            case Protoss:
                cloakedThreats = mainEnemyHasType(UnitType.Protoss_Dark_Templar) || mainEnemyHasType(UnitType.Protoss_Carrier);
                break;
        }
        if (cloakedThreats && getGs().getStrategyFromManager().numBays == 0) getGs().getStrategyFromManager().numBays++;
        return cloakedThreats;
    }

    public enum EnemyStrats {Unknown, EarlyPool, ZealotRush, CannonRush, ProtossFE, NinePool, FastHatch, BioPush, MechRush}
}
